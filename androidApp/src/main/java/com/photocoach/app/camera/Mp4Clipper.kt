package com.photocoach.app.camera

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.math.max

data class Mp4ClipResult(
    val file: File,
    val presentationTimestampUs: Long,
    val durationUs: Long,
)

object Mp4Clipper {
    fun clip(source: File, output: File, window: MotionClipWindow): Mp4ClipResult {
        require(source.isFile && source.length() > 0L)
        output.parentFile?.let { check(it.exists() || it.mkdirs()) }
        val extractor = MediaExtractor()
        var muxer: MediaMuxer? = null
        try {
            extractor.setDataSource(source.absolutePath)
            val trackMap = mutableMapOf<Int, Int>()
            var maximumSampleSize = DEFAULT_SAMPLE_BUFFER
            val createdMuxer = MediaMuxer(output.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            muxer = createdMuxer
            rotation(source)?.let(createdMuxer::setOrientationHint)
            for (track in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(track)
                val mime = format.getString(MediaFormat.KEY_MIME).orEmpty()
                if (!mime.startsWith("video/")) continue
                trackMap[track] = createdMuxer.addTrack(format)
                maximumSampleSize = max(maximumSampleSize, format.integerOrNull(MediaFormat.KEY_MAX_INPUT_SIZE) ?: 0)
            }
            if (trackMap.isEmpty()) throw IOException("Motion Photo 临时视频没有视频轨")
            if (trackMap.size != 1) throw IOException("Motion Photo 临时视频必须只有一个视频轨")
            createdMuxer.start()

            var firstWrittenUs = Long.MAX_VALUE
            var lastWrittenUs = Long.MIN_VALUE
            val buffer = ByteBuffer.allocateDirect(maximumSampleSize.coerceAtMost(MAX_SAMPLE_BUFFER))
            val info = MediaCodec.BufferInfo()
            trackMap.forEach { (inputTrack, outputTrack) ->
                extractor.selectTrack(inputTrack)
                extractor.seekTo(window.startUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
                var trackBaseUs = -1L
                while (true) {
                    val sampleTime = extractor.sampleTime
                    if (sampleTime < 0L || sampleTime > window.endUs) break
                    if (extractor.sampleTrackIndex != inputTrack) {
                        if (!extractor.advance()) break
                        continue
                    }
                    buffer.clear()
                    val size = extractor.readSampleData(buffer, 0)
                    if (size < 0) break
                    if (trackBaseUs < 0L) trackBaseUs = sampleTime
                    info.set(
                        0,
                        size,
                        sampleTime - trackBaseUs,
                        mediaCodecFlags(extractor.sampleFlags),
                    )
                    createdMuxer.writeSampleData(outputTrack, buffer, info)
                    firstWrittenUs = minOf(firstWrittenUs, sampleTime)
                    lastWrittenUs = maxOf(lastWrittenUs, sampleTime)
                    if (!extractor.advance()) break
                }
                extractor.unselectTrack(inputTrack)
            }
            if (firstWrittenUs == Long.MAX_VALUE || lastWrittenUs < firstWrittenUs) throw IOException("无法裁剪 Motion Photo 视频窗口")
            val coverSourceUs = Math.addExact(window.presentationTimestampUs, window.startUs)
            if (coverSourceUs !in firstWrittenUs..lastWrittenUs) throw IOException("封面时刻不在实际保留的视频范围内")
            createdMuxer.stop()
            muxer = null
            createdMuxer.release()
            if (output.length() <= 0L || output.length() > MotionTemporaryPolicy.MAX_RECORDING_BYTES) {
                throw IOException("Motion Photo 视频大小超出限制")
            }
            return Mp4ClipResult(
                file = output,
                presentationTimestampUs = Math.subtractExact(coverSourceUs, firstWrittenUs),
                durationUs = (lastWrittenUs - firstWrittenUs).coerceAtLeast(1L),
            )
        } catch (error: Throwable) {
            output.delete()
            throw error
        } finally {
            runCatching { muxer?.stop() }
            runCatching { muxer?.release() }
            extractor.release()
        }
    }

    private fun rotation(source: File): Int? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(source.absolutePath)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull()
        } finally {
            retriever.release()
        }
    }

    private fun MediaFormat.integerOrNull(key: String): Int? = if (containsKey(key)) getInteger(key) else null

    private fun mediaCodecFlags(extractorFlags: Int): Int =
        if (extractorFlags and MediaExtractor.SAMPLE_FLAG_SYNC != 0) MediaCodec.BUFFER_FLAG_KEY_FRAME else 0

    private const val DEFAULT_SAMPLE_BUFFER = 1 * 1024 * 1024
    private const val MAX_SAMPLE_BUFFER = 8 * 1024 * 1024
}
