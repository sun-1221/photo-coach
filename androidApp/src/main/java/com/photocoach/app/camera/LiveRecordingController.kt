package com.photocoach.app.camera

import android.content.Context
import android.os.SystemClock
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import java.io.File
import java.io.IOException
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

internal enum class LiveVideoTier { HD, SD }

internal val LIVE_VIDEO_TIER_FALLBACK_ORDER: List<LiveVideoTier> =
    listOf(LiveVideoTier.HD, LiveVideoTier.SD)

internal data class LiveRecordingMarker(
    internal val recording: Recording,
    val startedElapsedMs: Long?,
    val generation:Long,
    val source:MotionSensorSource,
)

/** Owns the silent rolling MP4 segment and its CameraX recording lifecycle. */
internal class LiveRecordingController(
    private val context: Context,
    private val mainExecutor: Executor,
    private val directory: File,
    private val elapsedRealtime: () -> Long = SystemClock::elapsedRealtime,
    private val wallClockMillis: () -> Long = System::currentTimeMillis,
    private val onStartFailure: (String) -> Unit,
    private val onRecorderReady:()->Unit,
    private val onFinalized: (file: File, durationUs: Long, error: Throwable?, timeline:VerifiedMotionTimeline?, generation:Long) -> Unit,
) {
    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private val recorderExecutor=sharedRecorderExecutor
    private val session = LiveRecordingSession()
    private var recorder: Recorder? = null
    private var activeRecording: Recording? = null
    private var activeVideoFile: File? = null
    private val effects=mutableMapOf<Recorder,MotionCameraEffect>()
    private var activeToken=0L
    private var activeSource:MotionSensorSource?=null
    private var sensorSource:MotionSensorSource?=null
    private var ownFile:File?=null
    private val recordingOwnership=LiveRecorderOwnership()
    private var activeBudget:MotionSessionBudget?=null
    @Volatile private var awaitingCoverage=false
    private fun trace(value:String){android.util.Log.i("MotionStage","controller token=$activeToken; t=${elapsedRealtime()}; $value")}

    val isRecording: Boolean get() = activeRecording != null

    fun effectFor(recorder:Recorder)=effects.getOrPut(recorder) {
        MotionCameraEffect(MotionSurfaceProcessor {error ->mainExecutor.execute { if(this.recorder===recorder)failActive(error) }})
    }
    fun bindSource(source:MotionSensorSource?){sensorSource=source}
    @Volatile private var sensorProcessor:MotionSurfaceProcessor?=null
    fun observe(source:MotionSensorSource,timestamp:Long,frameNumber:Long) {
        sensorProcessor?.observe(source,timestamp,frameNumber)
    }
    fun hasPrebuffer()=recorder?.let {effects[it]?.processor?.hasPrebuffer()}==true
    fun holdForCoverage(marker:LiveRecordingMarker) {
        if(isCurrent(marker))awaitingCoverage=true
    }
    fun discardCandidate(candidate:Recorder){effects.remove(candidate)?.close()}

    fun createCandidate(tier: LiveVideoTier): Pair<Recorder, VideoCapture<Recorder>> {
        check(LiveRecorderOwnership.canCreateCandidate()){"retired Recorder owners have not finalized"}
        val candidateRecorder = Recorder.Builder()
            .setExecutor(recorderExecutor)
            .setQualitySelector(QualitySelector.from(tier.toCameraXQuality()))
            .build()
        android.util.Log.i("MotionStage","candidate=${System.identityHashCode(candidateRecorder)}; requestedTier=$tier")
        return candidateRecorder to VideoCapture.withOutput(candidateRecorder)
    }

    fun install(recorder: Recorder?) {
        sensorProcessor=recorder?.let {effectFor(it).processor}
        if(recorder==null){effects.values.toList().forEach {it.close()};effects.clear()}
        this.recorder = recorder
    }

    fun start(blocked: Boolean) {
        val activeRecorder = recorder ?: return
        if (blocked || activeRecording != null || !recordingOwnership.canStart) return
        awaitingCoverage=false
        val source=sensorSource ?: run {onStartFailure("Live 相机时间域未确认，已回退普通照片");return}
        val file = runCatching {
            check(directory.exists() || directory.mkdirs()) { "cannot create Motion Photo directory" }
            MotionTemporaryPolicy.expired(directory.listFiles()?.toList().orEmpty(), wallClockMillis())
                .forEach(File::delete)
            File.createTempFile("motion-", ".mp4", directory)
        }.getOrElse {
            onStartFailure("无法创建 Live 临时文件，已回退普通照片")
            return
        }
        activeVideoFile = file
        val token = session.begin()
        activeToken=token;activeSource=source
        trace("start begin")
        var ownSubmitted=false
        try {
            check(recordingOwnership.acquire(token)){ "retired Recorder quota reached" }
            val measured=File.createTempFile("motion-owned-",".mp4",directory)
            ownFile=measured
            val options = FileOutputOptions.Builder(file)
                .setFileSizeLimit(MotionTemporaryPolicy.MAX_RECORDING_BYTES)
                .build()
            // Deliberately no withAudioEnabled(): the Motion Photo segment has no audio track.
            activeRecording = activeRecorder.prepareRecording(context, options).start(mainExecutor) { event ->
                onVideoRecordEvent(token, file, event)
            }
            trace("Recorder.start returned; submit own start")
            val processor=effectFor(activeRecorder).processor
            ownSubmitted=true
            val startupAt=elapsedRealtime()
            val budget=MotionSessionBudget(startupAt).also {activeBudget=it}
            val startupTimeout=executor.schedule({mainExecutor.execute {
                if(session.owns(token) && !budget.hasReady())failActive(IOException("Live 编码启动超时"))
            }},12,TimeUnit.SECONDS)
            trace("schedule bounded startup +12000")
            processor.start(measured,MotionFrameTimeline(token,source),budget=budget,onReady={
                startupTimeout.cancel(false)
                mainExecutor.execute {
                if(session.owns(token)) {
                    trace("own ready; recording stop delay=${budget.stopDelay(elapsedRealtime())}; absolute completion=${budget.deadlineMs()}")
                    executor.schedule({trace("8s scheduler fired");mainExecutor.execute {
                        trace("8s main fired; awaitingCoverage=$awaitingCoverage")
                        if(session.owns(token) && !awaitingCoverage)stopMeasuredInput()
                    }},budget.stopDelay(elapsedRealtime()),TimeUnit.MILLISECONDS)
                    executor.schedule({mainExecutor.execute {if(session.owns(token))failActive(IOException("Live 完成超时"))}},
                        (budget.deadlineMs()-elapsedRealtime()).coerceAtLeast(0L),TimeUnit.MILLISECONDS)
                }
            }}) {result ->
                startupTimeout.cancel(false)
                dispatchMotionCompletion(mainExecutor,budget,elapsedRealtime,{session.owns(token)},result) {completedResult ->
                    trace("own completion main success=${completedResult.isSuccess}; matches=${session.owns(token)}")
                    completedResult.exceptionOrNull()?.let {android.util.Log.w("MotionPipeline","Owned encoder failed",it)}
                    runCatching {activeRecording?.stop()}
                    session.finish(token);activeRecording=null;activeVideoFile=null;ownFile=null
                    val video=completedResult.getOrNull()
                    onFinalized(video?.file ?: File(directory,"failed-$token.mp4"),video?.timeline?.durationUs ?: 0L,
                        completedResult.exceptionOrNull(),video?.timeline,token)
                }
            }
        } catch (_: Throwable) {
            session.invalidate()
            val started=activeRecording
            runCatching {started?.stop()}
            if(started==null){file.delete();releaseRecordingLease(token)} // Otherwise only Finalize owns cleanup.
            if(ownSubmitted)runCatching {effects[activeRecorder]?.processor?.finish()}
            else ownFile?.delete()
            activeVideoFile = null
            activeRecording = null
            ownFile=null
            onStartFailure("视频编码器启动失败，已回退普通照片")
        }
    }

    fun marker(): LiveRecordingMarker? = activeRecording?.let { recording ->
        activeSource?.let {LiveRecordingMarker(recording,session.startedElapsedMs,activeToken,it)}
    }

    fun isCurrent(marker: LiveRecordingMarker?): Boolean =
        marker != null && activeRecording === marker.recording

    fun stopAfter(marker: LiveRecordingMarker, delayMillis: Long) {
        executor.schedule(
            { mainExecutor.execute {if(isCurrent(marker))stopMeasuredInput()} },
            delayMillis.coerceAtLeast(0L),
            TimeUnit.MILLISECONDS,
        )
    }
    fun stopWhenCovered(marker:LiveRecordingMarker,captureSensorNs:Long?) {
        awaitingCoverage=true
        fun checkCoverage() {
            if(!isCurrent(marker)){awaitingCoverage=false;return}
            val processor=recorder?.let {effects[it]?.processor}
            if(captureSensorNs==null || processor?.hasPostbuffer(marker.source,captureSensorNs)==true) {
                awaitingCoverage=false
                stopMeasuredInput()
            } else executor.schedule({mainExecutor.execute {checkCoverage()}},50,TimeUnit.MILLISECONDS)
        }
        // Idle 8s elapsed stop must not cut an in-flight JPEG window. Media span still hard-stops input.
        checkCoverage()
    }

    fun stop(deleteFile: Boolean) {
        awaitingCoverage=false
        activeBudget?.finishing(elapsedRealtime())
        session.invalidate()
        recorder?.let {effects[it]?.processor?.finish()}
        val recording = activeRecording
        activeRecording = null
        runCatching { recording?.stop() }
        if (recording == null && deleteFile) activeVideoFile?.delete()
        activeVideoFile = null
    }

    fun shutdown() {
        stop(deleteFile = true)
        install(null)
        executor.shutdown()
    }

    private fun stopMeasuredInput() {
        awaitingCoverage=false
        trace("stop measured requested")
        activeBudget?.finishing(elapsedRealtime())
        recorder?.let {effects[it]?.processor?.finish()}
        trace("Recorder.stop begin")
        runCatching {activeRecording?.stop()}
        trace("Recorder.stop returned")
    }
    private fun failActive(error:Throwable) {
        android.util.Log.w("MotionPipeline","Recording failed",error)
        android.util.Log.w("MotionPipeline",recorder?.let {effects[it]?.processor?.diagnostic()} ?: "no installed processor")
        if(!session.owns(activeToken))return
        val token=activeToken;val file=ownFile ?: return
        session.invalidate();stopMeasuredInput();activeRecording=null;ownFile=null
        // The native encoder retains and deletes its file after its input owner acknowledges.
        onFinalized(File(directory,"failed-$token.mp4"),0L,error,null,token)
    }
    private fun releaseRecordingLease(token:Long) {
        recordingOwnership.finalized(token)
    }

    private fun onVideoRecordEvent(token: Long, file: File, event: VideoRecordEvent) {
        if(event is VideoRecordEvent.Start || event is VideoRecordEvent.Finalize)trace("Recorder event=${event.javaClass.simpleName}; eventToken=$token")
        if(event is VideoRecordEvent.Finalize) {
            file.delete();releaseRecordingLease(token)
        }
        if (!session.owns(token)) {
            if (event is VideoRecordEvent.Finalize) onRecorderReady()
            return
        }
        if (event is VideoRecordEvent.Start) {
            session.started(token, elapsedRealtime())
            return
        }
        if (event !is VideoRecordEvent.Finalize) return
        file.delete() // Recorder is only the negotiated CameraX sink, never the published video.
        activeVideoFile = null
        val limitReached = event.error == VideoRecordEvent.Finalize.ERROR_DURATION_LIMIT_REACHED ||
            event.error == VideoRecordEvent.Finalize.ERROR_FILE_SIZE_LIMIT_REACHED
        val error = if (event.hasError() && !limitReached) {
            event.cause ?: IOException("Live 编码结束错误：${event.error}")
        } else {
            null
        }
        if(error!=null)failActive(error) else stopMeasuredInput()
    }
    companion object {
        private val sharedRecorderExecutor=Executors.newFixedThreadPool(2){r->Thread(r,"motion-recorder-owner").apply {isDaemon=true}}
    }
}

private fun LiveVideoTier.toCameraXQuality(): Quality = when (this) {
    LiveVideoTier.HD -> Quality.HD
    LiveVideoTier.SD -> Quality.SD
}
