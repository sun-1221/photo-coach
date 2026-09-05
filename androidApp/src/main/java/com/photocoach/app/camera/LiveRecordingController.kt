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
)

/** Owns the silent rolling MP4 segment and its CameraX recording lifecycle. */
internal class LiveRecordingController(
    private val context: Context,
    private val mainExecutor: Executor,
    private val directory: File,
    private val elapsedRealtime: () -> Long = SystemClock::elapsedRealtime,
    private val wallClockMillis: () -> Long = System::currentTimeMillis,
    private val onStartFailure: (String) -> Unit,
    private val onFinalized: (file: File, durationUs: Long, error: Throwable?) -> Unit,
) {
    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private val session = LiveRecordingSession()
    private var recorder: Recorder? = null
    private var activeRecording: Recording? = null
    private var activeVideoFile: File? = null

    val isRecording: Boolean get() = activeRecording != null

    fun createCandidate(tier: LiveVideoTier): Pair<Recorder, VideoCapture<Recorder>> {
        val candidateRecorder = Recorder.Builder()
            .setExecutor(executor)
            .setQualitySelector(QualitySelector.from(tier.toCameraXQuality()))
            .build()
        return candidateRecorder to VideoCapture.withOutput(candidateRecorder)
    }

    fun install(recorder: Recorder?) {
        this.recorder = recorder
    }

    fun start(blocked: Boolean) {
        val activeRecorder = recorder ?: return
        if (blocked || activeRecording != null) return
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
        try {
            val options = FileOutputOptions.Builder(file)
                .setFileSizeLimit(MotionTemporaryPolicy.MAX_RECORDING_BYTES)
                .setDurationLimitMillis(MotionTemporaryPolicy.MAX_RECORDING_DURATION_MS)
                .build()
            // Deliberately no withAudioEnabled(): the Motion Photo segment has no audio track.
            activeRecording = activeRecorder.prepareRecording(context, options).start(mainExecutor) { event ->
                onVideoRecordEvent(token, file, event)
            }
        } catch (_: Throwable) {
            file.delete()
            activeVideoFile = null
            activeRecording = null
            onStartFailure("视频编码器启动失败，已回退普通照片")
        }
    }

    fun marker(): LiveRecordingMarker? = activeRecording?.let { LiveRecordingMarker(it, session.startedElapsedMs) }

    fun isCurrent(marker: LiveRecordingMarker?): Boolean =
        marker != null && activeRecording === marker.recording

    fun stopAfter(marker: LiveRecordingMarker, delayMillis: Long) {
        executor.schedule(
            { runCatching { marker.recording.stop() } },
            delayMillis.coerceAtLeast(0L),
            TimeUnit.MILLISECONDS,
        )
    }

    fun stop(deleteFile: Boolean) {
        session.invalidate()
        val recording = activeRecording
        activeRecording = null
        runCatching { recording?.stop() }
        if (recording == null && deleteFile) activeVideoFile?.delete()
        activeVideoFile = null
    }

    fun shutdown() {
        stop(deleteFile = true)
        executor.shutdown()
    }

    private fun onVideoRecordEvent(token: Long, file: File, event: VideoRecordEvent) {
        if (!session.owns(token)) {
            if (event is VideoRecordEvent.Finalize) file.delete()
            return
        }
        if (event is VideoRecordEvent.Start) {
            session.started(token, elapsedRealtime())
            return
        }
        if (event !is VideoRecordEvent.Finalize || !session.finish(token)) return
        val durationUs = event.recordingStats.recordedDurationNanos / 1_000L
        activeRecording = null
        activeVideoFile = null
        val limitReached = event.error == VideoRecordEvent.Finalize.ERROR_DURATION_LIMIT_REACHED ||
            event.error == VideoRecordEvent.Finalize.ERROR_FILE_SIZE_LIMIT_REACHED
        val error = if (event.hasError() && !limitReached) {
            event.cause ?: IOException("Live 编码结束错误：${event.error}")
        } else {
            null
        }
        onFinalized(file, durationUs, error)
    }
}

private fun LiveVideoTier.toCameraXQuality(): Quality = when (this) {
    LiveVideoTier.HD -> Quality.HD
    LiveVideoTier.SD -> Quality.SD
}
