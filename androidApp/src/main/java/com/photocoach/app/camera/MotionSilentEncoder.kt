package com.photocoach.app.camera

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.SystemClock
import android.view.Surface
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

internal data class FinalizedMotionVideo(val file:File,val timeline:VerifiedMotionTimeline)

/** Surface-input AVC encoder. Its output buffers, not Recorder callbacks, populate the ledger.
 * Construction and finishInput belong to its GL input owner. Draining has a separate owner.
 */
internal class MotionSilentEncoder(
    val width:Int,val height:Int,
    private val file:File,
    val timeline:MotionFrameTimeline,
    private val sessionBudget:MotionSessionBudget?=null,
    private val completed:(Result<FinalizedMotionVideo>)->Unit,
) {
    private val reported=AtomicBoolean(false)
    private val finishing=AtomicBoolean(false)
    private val abandoned=AtomicBoolean(false)
    private val inputReleased=java.util.concurrent.CountDownLatch(1)
    private val budget=sessionBudget ?: MotionSessionBudget(SystemClock.elapsedRealtime())
    private val codec:MediaCodec
    private val muxer:MediaMuxer
    val surface:Surface
    @Volatile var inspection:InspectedMotionMp4?=null;private set
    private fun stage(value:String) {android.util.Log.i("MotionStage","own=${file.name}; t=${SystemClock.elapsedRealtime()}; $value")}
    init {
        stage("construct begin ${width}x$height")
        require(width>0 && height>0 && width.toLong()*height<=1280L*720)
        check(permits.tryAcquire()){ "previous encoder owners have not released" }
        var createdCodec:MediaCodec?=null;var createdMuxer:MediaMuxer?=null;var createdSurface:Surface?=null
        try {
            createdCodec=MediaCodec.createEncoderByType("video/avc")
            val format=MediaFormat.createVideoFormat("video/avc",width,height).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT,MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE,2_000_000)
                setInteger(MediaFormat.KEY_FRAME_RATE,30)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,1)
                setInteger(MediaFormat.KEY_MAX_B_FRAMES,0)
            }
            createdCodec.configure(format,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE)
            createdSurface=createdCodec.createInputSurface()
            createdMuxer=MediaMuxer(file.absolutePath,MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            createdCodec.start()
            codec=createdCodec;muxer=createdMuxer;surface=createdSurface
            if(sessionBudget==null)check(budget.ready(SystemClock.elapsedRealtime()))
            stage("codec started")
            workers.execute(::drain)
        } catch(error:Throwable) {
            createdSurface?.release();runCatching {createdCodec?.release()};runCatching {createdMuxer?.release()}
            file.delete();permits.release();throw error
        }
    }
    /** Must be called only after the GL owner has stopped writes and destroyed its EGL window. */
    fun finishInput() {
        if(finishing.compareAndSet(false,true)) {
            budget.finishing(SystemClock.elapsedRealtime())
            stage("signal EOS begin")
            try {runCatching {codec.signalEndOfInputStream()}.onFailure(::abandon)}
            finally {inputReleased.countDown();stage("signal EOS returned; input released")}
        }
    }
    fun abandon(error:Throwable) {
        if(!reported.compareAndSet(false,true))return
        abandoned.set(true);timeline.fail(error.message ?: "encoder abandoned")
        stage("abandon ${error.message}")
        runCatching {completed(Result.failure(error))}
        // Native owners retain their resources until they return; a watchdog never releases a
        // surface/muxer concurrently with a blocked write and never frees another worker permit.
    }
    private fun report(result:Result<FinalizedMotionVideo>):Boolean {
        if(!reported.compareAndSet(false,true))return false
        runCatching {completed(result)}
        return true
    }
    private fun drain() {
        stage("drain begin")
        var muxStarted=false;var track=-1;var eos=false
        val watchdog=watch.scheduleAtFixedRate({if(budget.expired(SystemClock.elapsedRealtime()))abandon(IllegalStateException("Live encoder deadline exceeded"))},100,100,TimeUnit.MILLISECONDS)
        var failure:Throwable?=null
        try {
            val info=MediaCodec.BufferInfo()
            while(!eos && !abandoned.get()) {
                check(!budget.expired(SystemClock.elapsedRealtime())){"Live encoder deadline exceeded"}
                val index=codec.dequeueOutputBuffer(info,10_000)
                if(index==MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    check(!muxStarted){"encoder format changed twice"}
                    track=muxer.addTrack(codec.outputFormat);muxer.start();muxStarted=true
                    stage("mux started")
                } else if(index>=0) {
                    try {
                        if(info.size>0 && info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG==0) {
                            check(muxStarted)
                            val pts=timeline.encoded(info.presentationTimeUs,info.flags and MediaCodec.BUFFER_FLAG_KEY_FRAME!=0,info.size)
                            check(timeline.failure==null){timeline.failure!!}
                            if(pts!=null) {
                                val buffer=requireNotNull(codec.getOutputBuffer(index))
                                buffer.position(info.offset);buffer.limit(info.offset+info.size)
                                val sample=MediaCodec.BufferInfo().apply {set(info.offset,info.size,pts,info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM.inv())}
                                muxer.writeSampleData(track,buffer,sample)
                            }
                        }
                        eos=info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM!=0
                        if(eos)stage("drain EOS; retained=${timeline.writtenSamples().size}")
                    } finally {codec.releaseOutputBuffer(index,false)}
                }
            }
            check(eos && finishing.get() && !abandoned.get()){ "Live encoder did not finalize after input shutdown" }
        } catch(error:Throwable) {failure=error;abandon(error)}
        finally {
            // A slow GL swap can outlive the watchdog. Keep this bounded worker/permit and
            // native resources until that GL owner acknowledges that its EGL window is gone.
            inputReleased.await()
            stage("release codec begin")
            runCatching {codec.stop()}.onFailure {if(failure==null)failure=it}
            runCatching {codec.release()}.onFailure {if(failure==null)failure=it}
            runCatching {surface.release()}.onFailure {if(failure==null)failure=it}
            stage("release codec done; mux stop begin")
            if(muxStarted)runCatching {muxer.stop()}.onFailure {if(failure==null)failure=it}
            runCatching {muxer.release()}.onFailure {if(failure==null)failure=it}
            stage("mux release done; inspect begin; failure=${failure?.message}")
            try {
                if(failure==null && !abandoned.get()) {
                    val actual=MotionMp4Inspection.read(file)
                    inspection=actual
                    val verified=requireNotNull(timeline.verifyMux(actual.samples,actual.trackTimescale,actual.tables)){timeline.failure ?: "unverified retained mux timeline"}
                    check(!abandoned.get() && !budget.expired(SystemClock.elapsedRealtime()))
                    stage("verified; complete success")
                    if(!report(Result.success(FinalizedMotionVideo(file,verified))))file.delete()
                } else throw failure ?: IllegalStateException("encoder abandoned")
            } catch(error:Throwable) {file.delete();report(Result.failure(error))}
            finally {watchdog.cancel(false);permits.release();stage("owner finished")}
        }
    }
    companion object {
        // Process-wide permits remain occupied if native calls stall, including across rebinds.
        private val permits=Semaphore(2)
        private val workers=Executors.newFixedThreadPool(2){r->Thread(r,"motion-codec-output").apply {isDaemon=true}}
        private val watch=Executors.newSingleThreadScheduledExecutor {r->Thread(r,"motion-codec-watch").apply {isDaemon=true}}
    }
}
