package com.photocoach.app.camera

import android.graphics.SurfaceTexture
import android.opengl.EGLSurface
import android.view.Surface
import androidx.camera.core.CameraEffect
import androidx.camera.core.SurfaceOutput
import androidx.camera.core.SurfaceProcessor
import androidx.camera.core.SurfaceRequest
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

internal interface MotionSurfaceInput {
    val resolution:android.util.Size
    fun decline()
    fun provide(surface:Surface,executor:java.util.concurrent.Executor,returned:()->Unit)
    fun invalidate()
}

/** VIDEO_CAPTURE alone is transformed. Preview, analysis and still capture retain their own paths. */
internal class MotionCameraEffect(val processor:MotionSurfaceProcessor):CameraEffect(
    VIDEO_CAPTURE,processor.executor,processor,{processor.fail(it)}),AutoCloseable {
    override fun close()=processor.close()
}

/** Three GL contexts communicate only through completed immutable pixel copies. */
internal class MotionSurfaceProcessor(private val failure:(Throwable)->Unit):SurfaceProcessor,AutoCloseable {
    private val failed=AtomicBoolean()
    private val closing=AtomicBoolean()
    private var inputGl:MotionGlRenderer?=null
    private var recorderGl:MotionGlRenderer?=null
    private var encoderGl:MotionGlRenderer?=null
    private var input:Input?=null
    private var output:SurfaceOutput?=null
    private var pool:MotionPixelPool?=null
    private var copySurface:EGLSurface?=null
    private var recorderWindow:EGLSurface?=null
    private var recorderOutput:SurfaceOutput?=null
    private var recorderTexture=0
    private var encoderWindow:EGLSurface?=null
    private var encoderTexture=0
    private var encoder:MotionSilentEncoder?=null
    private var durationStopRequested=false
    @Volatile private var encoderAcceptingFrames=false
    @Volatile private var timeline:MotionFrameTimeline?=null
    @Volatile private var budget:MotionSessionBudget?=null
    private var pendingStart:(()->Unit)?=null
    private var cancelPendingStart:(()->Unit)?=null
    @Volatile private var activeDone:((Result<FinalizedMotionVideo>)->Unit)?=null
    private var hadOutput=false
    private val inputCount=java.util.concurrent.atomic.AtomicLong()
    private val outputCount=java.util.concurrent.atomic.AtomicLong()
    private val frames=java.util.concurrent.atomic.AtomicLong()
    private val copied=java.util.concurrent.atomic.AtomicLong()
    private val recorderFrames=java.util.concurrent.atomic.AtomicLong()
    private val encoderFrames=java.util.concurrent.atomic.AtomicLong()
    fun diagnostic()="inputs=$inputCount; outputs=$outputCount; frames=$frames; copied=$copied; recorderFrames=$recorderFrames; encoderFrames=$encoderFrames; retained=${timeline?.writtenSamples()?.size}; timelineFailure=${timeline?.failure}"
    private val recorderWorker=MotionGlWorker("motion-recorder-gl",::fail,::renderRecorder)
    private val encoderWorker=try {MotionGlWorker("motion-own-gl",::fail,::renderEncoder)}
        catch(error:Throwable){recorderWorker.close {};throw error}
    private val inputWorker=try {MotionGlWorker("motion-input-gl",::fail,{})}
        catch(error:Throwable){recorderWorker.close {};encoderWorker.close {};throw error}
    // Late CameraX callbacks still resolve rejected requests/output ownership after shutdown.
    // Closing guards below ensure this fallback never executes GL on a foreign thread.
    val executor=java.util.concurrent.Executor {task ->
        try {inputWorker.executor.execute(task)}
        catch(error:java.util.concurrent.RejectedExecutionException) {
            if(closing.get() && inputWorker.terminated.count==0L)task.run() else throw error
        }
    }
    private val returnExecutor=java.util.concurrent.Executor {task ->
        try {inputWorker.ownershipExecutor.execute(task)}
        catch(error:java.util.concurrent.RejectedExecutionException) {
            // Shutdown is admitted only once input is null. These callbacks then do no GL work.
            if(closing.get() && input==null)task.run() else throw error
        }
    }
    private data class Input(val request:MotionSurfaceInput,val texture:SurfaceTexture,val surface:Surface,val id:Int)
    internal val ownerTerminations get()=listOf(inputWorker.terminated,recorderWorker.terminated,encoderWorker.terminated)

    fun fail(error:Throwable) {
        android.util.Log.w("MotionPipeline","Surface owner failed",error)
        timeline?.fail(error.message ?: "Live surface failure")
        activeDone?.invoke(Result.failure(error))
        if(failed.compareAndSet(false,true))failure(error)
    }
    fun observe(source:MotionSensorSource,timestampNs:Long,frameNumber:Long) {
        correlationTimeline?.takeIf {it.source==source}?.sensorResult(source,timestampNs,frameNumber)
    }
    // Input stops before the queued CaptureResults necessarily arrive. Keep the same
    // bounded ledger reachable through mux verification; sealing rejects later results.
    @Volatile private var correlationTimeline:MotionFrameTimeline?=null
    fun hasPrebuffer()=timeline?.hasPrebuffer()==true && !failed.get() && !closing.get()
    fun hasPostbuffer(source:MotionSensorSource,sensorNs:Long)=timeline?.hasPostbuffer(source,sensorNs)==true
    fun start(file:File,ledger:MotionFrameTimeline,budget:MotionSessionBudget?=null,onReady:()->Unit={},done:(Result<FinalizedMotionVideo>)->Unit) {
        val delivered=AtomicBoolean()
        val once:(Result<FinalizedMotionVideo>)->Unit={result ->if(delivered.compareAndSet(false,true))done(result)}
        try {executor.execute {begin(file,ledger,budget,onReady,once)}}
        catch(error:Throwable){once(Result.failure(error));fail(error)}
    }
    private fun begin(file:File,ledger:MotionFrameTimeline,budget:MotionSessionBudget?,onReady:()->Unit,done:(Result<FinalizedMotionVideo>)->Unit) {
            try {
                check(!closing.get() && !failed.get())
                activeDone=done
                if(output==null) {
                    check(pendingStart==null)
                    pendingStart={begin(file,ledger,budget,onReady,done)}
                    cancelPendingStart={file.delete();done(Result.failure(IllegalStateException("Live output was not ready")))}
                    return
                }
                check(timeline==null){"previous recording still owns input"}
                val size=requireNotNull(output){"video output has not negotiated a format"}.size
                timeline=ledger
                correlationTimeline=ledger
                this.budget=budget
                encoderWorker.executor.execute {
                    try {
                    val gl=encoderGl ?: MotionGlRenderer().also {encoderGl=it}
                    check(encoder==null)
                    val created=MotionSilentEncoder(size.width,size.height,file,ledger,budget,done)
                    encoder=created
                    durationStopRequested=false
                    try {
                        encoderWindow=gl.window(created.surface)
                        encoderTexture=gl.texture(false)
                        if(budget!=null)check(budget.ready(android.os.SystemClock.elapsedRealtime())){"Live 编码启动超时"}
                        encoderAcceptingFrames=true
                        onReady()
                    } catch(error:Throwable) {created.abandon(error);stopEncoder();throw error}
                    } catch(error:Throwable){done(Result.failure(error));fail(error)}
                }
            } catch(error:Throwable){done(Result.failure(error));fail(error)}
    }
    fun finish() {
        encoderAcceptingFrames=false
        budget?.finishing(android.os.SystemClock.elapsedRealtime())
        android.util.Log.i("MotionStage","processor finish requested t=${android.os.SystemClock.elapsedRealtime()}")
        if(closing.get())return
        executor.execute {
            if(closing.get())return@execute
            cancelPendingStart?.invoke();cancelPendingStart=null
            pendingStart=null
            timeline=null
            android.util.Log.i("MotionStage","processor finish on input owner; queue own stop t=${android.os.SystemClock.elapsedRealtime()}")
            encoderWorker.executor.execute(::stopEncoder)
        }
    }
    private fun stopEncoder() {
        android.util.Log.i("MotionStage","own GL stop entered t=${android.os.SystemClock.elapsedRealtime()}; encoder=${encoder!=null}")
        // No codec input release before its owner has destroyed the window.
        val current=encoder ?: return
        try {encoderWindow?.let {encoderGl!!.destroy(it)};encoderWindow=null}
        catch(error:Throwable){current.abandon(error);throw error}
        try {
            if(encoderTexture!=0)encoderGl!!.deleteTexture(encoderTexture)
            encoderTexture=0
        } finally {encoder=null;current.finishInput()}
    }
    override fun onInputSurface(request:SurfaceRequest) {
        acceptInput(object:MotionSurfaceInput {
            override val resolution get()=request.resolution
            override fun decline(){request.willNotProvideSurface()}
            override fun provide(surface:Surface,executor:java.util.concurrent.Executor,returned:()->Unit) {
                request.provideSurface(surface,executor){returned()}
            }
            override fun invalidate(){request.invalidate()}
        })
    }
    internal fun acceptInput(request:MotionSurfaceInput) {
        inputCount.incrementAndGet()
        if(closing.get() || failed.get()){request.decline();return}
        if(input!=null){request.decline();fail(IllegalStateException("overlapping Live input request"));return}
        var allocated:Input?=null
        try {
            val gl=inputGl ?: MotionGlRenderer().also {inputGl=it}
            gl.current()
            val id=gl.texture(true)
            val texture=SurfaceTexture(id)
            texture.setDefaultBufferSize(request.resolution.width,request.resolution.height)
            val owned=Input(request,texture,Surface(texture),id);allocated=owned;input=owned
            texture.setOnFrameAvailableListener {if(!closing.get())inputWorker.runOnOwner {drawInput(owned)}}
            request.provide(owned.surface,returnExecutor) {
                // CameraX has acknowledged the provided surface. Only now release it.
                if(input!==owned)return@provide
                releaseInput(owned);if(input===owned)input=null;finishClose()
            }
        } catch(error:Throwable) {
            allocated?.let {releaseInput(it);input=null}
            request.decline();fail(error)
        }
    }
    override fun onOutputSurface(next:SurfaceOutput) {
        outputCount.incrementAndGet()
        if(closing.get() || failed.get()){next.close();return}
        if(output!=null){next.close();fail(IllegalStateException("overlapping Live output"));return}
        if(hadOutput){next.close();fail(IllegalStateException("Live output renegotiated; old recording cannot reuse pixels"));finish();return}
        hadOutput=true
        try {
            val size=next.size
            val pixels=MotionPixelPool(size.width,size.height)
            val gl=inputGl ?: MotionGlRenderer().also {inputGl=it}
            copySurface=gl.pbuffer(size.width,size.height);pool=pixels;output=next
            val surface=next.getSurface(returnExecutor) {
                if(closing.get())return@getSurface // The recorder owner already has ordered cleanup.
                if(output===next){
                    output=null;pool?.close();pool=null
                    copySurface?.let {inputGl!!.destroy(it)};copySurface=null
                    if(timeline!=null){fail(IllegalStateException("Live output closed during recording"));finish()}
                }
                recorderWorker.executor.execute {closeRecorder(next)}
            }
            recorderWorker.executor.execute {
                val renderer=recorderGl ?: MotionGlRenderer().also {recorderGl=it}
                recorderOutput=next
                recorderWindow=renderer.window(surface);recorderTexture=renderer.texture(false)
            }
            pendingStart?.also {pendingStart=null;cancelPendingStart=null;it()}
        } catch(error:Throwable){next.close();fail(error)}
    }
    private fun drawInput(owned:Input) {
        if(closing.get() || failed.get() || input!==owned)return
        try {
            val gl=requireNotNull(inputGl);gl.current();owned.texture.updateTexImage()
            frames.incrementAndGet()
            val target=output ?: return
            val pixels=pool ?: return
            val frame=pixels.acquire() ?: return
            frame.use {
                val original=FloatArray(16);val transformed=FloatArray(16)
                owned.texture.getTransformMatrix(original);target.updateTransformMatrix(transformed,original)
                gl.draw(owned.id,true,transformed,requireNotNull(copySurface),pixels.width,pixels.height)
                gl.read(pixels.width,pixels.height,it.writable())
                copied.incrementAndGet()
                val published=it.publish(owned.texture.timestamp)
                recorderWorker.submit(published.retain())
                if(timeline!=null && encoderAcceptingFrames)encoderWorker.submit(published.retain())
            }
        } catch(error:Throwable){fail(error)}
    }
    private fun renderRecorder(frame:MotionPixelPool.Lease) {
        val gl=recorderGl ?: return
        val target=recorderOutput ?: return
        val window=recorderWindow ?: return
        gl.current();gl.upload(recorderTexture,target.size.width,target.size.height,frame.pixels)
        gl.draw(recorderTexture,false,MotionGlRenderer.identity(),window,target.size.width,target.size.height)
        gl.present(window,frame.sensorNs)
        recorderFrames.incrementAndGet()
    }
    private fun renderEncoder(frame:MotionPixelPool.Lease) {
        val current=encoder ?: return
        val window=encoderWindow ?: return
        val gl=requireNotNull(encoderGl)
        if(current.timeline.exceedsInputDuration(frame.sensorNs)) {
            if(!durationStopRequested){durationStopRequested=true;android.util.Log.i("MotionStage","media span reached; stop before over-limit input");finish()}
            return
        }
        val pts=current.timeline.submit(frame.sensorNs) ?: return
        // Size is owned by this codec generation, never read from a later CameraX output.
        gl.current();gl.upload(encoderTexture,current.width,current.height,frame.pixels)
        gl.draw(encoderTexture,false,MotionGlRenderer.identity(),window,current.width,current.height)
        gl.present(window,pts*1000L)
        encoderFrames.incrementAndGet()
    }
    private fun closeRecorder(expected:SurfaceOutput?=recorderOutput) {
        if(expected!==recorderOutput){expected?.close();return}
        try {
            recorderWindow?.let {recorderGl!!.destroy(it)};recorderWindow=null
            if(recorderTexture!=0)recorderGl!!.deleteTexture(recorderTexture)
            recorderTexture=0
        } finally {recorderOutput=null;expected?.close()}
    }
    private fun releaseInput(owned:Input) {
        runCatching {owned.texture.setOnFrameAvailableListener(null)}.onFailure(::fail)
        runCatching {owned.surface.release()}.onFailure(::fail)
        runCatching {owned.texture.release()}.onFailure(::fail)
        runCatching {inputGl?.let {it.current();it.deleteTexture(owned.id)}}.onFailure(::fail)
    }
    override fun close() {
        encoderAcceptingFrames=false
        budget?.finishing(android.os.SystemClock.elapsedRealtime())
        if(!closing.compareAndSet(false,true))return
        returnExecutor.execute {
            input?.let {it.texture.setOnFrameAvailableListener(null);it.request.invalidate()}
            cancelPendingStart?.invoke();cancelPendingStart=null
            pool?.close();pool=null;output=null;timeline=null;pendingStart=null
            recorderWorker.close {try {closeRecorder()} finally {recorderGl?.close();recorderGl=null}}
            encoderWorker.close {try {stopEncoder()} finally {encoderGl?.close();encoderGl=null}}
            finishClose()
        }
    }
    private fun finishClose() {
        if(closing.get() && input==null)inputWorker.close {
            try {copySurface?.let {inputGl?.destroy(it)};copySurface=null}
            finally {inputGl?.close();inputGl=null}
        }
    }
}
