package com.photocoach.app.camera

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.RandomAccessFile
import java.nio.charset.StandardCharsets
import kotlin.math.min

data class MotionClipWindow(val startUs: Long, val endUs: Long, val presentationTimestampUs: Long) {
    init {
        require(startUs >= 0L && endUs > startUs)
        require(presentationTimestampUs in 0L..(endUs - startUs))
    }

    val durationUs: Long get() = endUs - startUs

    companion object {
        const val SIDE_US = 1_500_000L
        const val TARGET_DURATION_US = SIDE_US * 2L

        fun aroundShutter(segmentStartUs: Long, shutterUs: Long, segmentEndUs: Long): MotionClipWindow {
            require(segmentStartUs >= 0L && shutterUs in segmentStartUs..segmentEndUs && segmentEndUs > segmentStartUs)
            val availableDuration = segmentEndUs - segmentStartUs
            val duration = min(TARGET_DURATION_US, availableDuration)
            var start = shutterUs - SIDE_US
            start = start.coerceIn(segmentStartUs, segmentEndUs - duration)
            val end = start + duration
            val presentation = (shutterUs - start).coerceIn(0L, duration)
            return MotionClipWindow(start, end, presentation)
        }
    }
}

data class MotionPhotoContainerInfo(
    val videoLength: Long,
    val presentationTimestampUs: Long,
    val videoOffset: Long,
)

object MotionPhotoAssembler {
    private val XMP_HEADER = "http://ns.adobe.com/xap/1.0/\u0000".toByteArray(StandardCharsets.US_ASCII)
    private val FTYPE = byteArrayOf('f'.code.toByte(), 't'.code.toByte(), 'y'.code.toByte(), 'p'.code.toByte())

    fun assemble(
        jpeg: ByteArray,
        mp4: ByteArray,
        presentationTimestampUs: Long,
    ): ByteArray {
        validateJpeg(jpeg)
        validateMp4(mp4)
        require(presentationTimestampUs >= -1L)
        val app1 = app1Segment(mp4.size.toLong(), presentationTimestampUs)
        return ByteArray(jpeg.size + app1.size + mp4.size).also { output ->
            jpeg.copyInto(output, 0, 0, 2)
            app1.copyInto(output, 2)
            jpeg.copyInto(output, 2 + app1.size, 2)
            mp4.copyInto(output, jpeg.size + app1.size)
        }
    }

    fun assemble(
        jpeg: File,
        mp4: File,
        output: File,
        presentationTimestampUs: Long,
    ): MotionPhotoContainerInfo {
        require(jpeg.isFile && jpeg.length() >= 4L) { "JPEG source is missing" }
        require(mp4.isFile && mp4.length() >= 12L) { "MP4 source is missing" }
        validateJpegPrefix(jpeg.inputStream().use { readPrefix(it, 2) })
        RandomAccessFile(jpeg, "r").use { input ->
            input.seek(input.length() - 2L)
            require(input.read() == 0xff && input.read() == 0xd9) { "JPEG has no EOI" }
        }
        validateMp4Prefix(mp4.inputStream().use { readPrefix(it, 12) })
        val app1 = app1Segment(mp4.length(), presentationTimestampUs)
        output.parentFile?.let { check(it.exists() || it.mkdirs()) { "cannot create Motion Photo directory" } }
        try {
            output.outputStream().buffered().use { sink ->
                jpeg.inputStream().buffered().use { source ->
                    val soi = readPrefix(source, 2)
                    validateJpegPrefix(soi)
                    sink.write(soi)
                    sink.write(app1)
                    source.copyTo(sink)
                }
                mp4.inputStream().buffered().use { it.copyTo(sink) }
            }
            val videoOffset = output.length() - mp4.length()
            check(videoOffset > 0L && output.length() == videoOffset + mp4.length())
            return MotionPhotoContainerInfo(mp4.length(), presentationTimestampUs, videoOffset)
        } catch (error: Throwable) {
            output.delete()
            throw error
        }
    }

    fun xmpPacket(videoLength: Long, presentationTimestampUs: Long): String {
        require(videoLength > 0L)
        require(presentationTimestampUs >= -1L)
        val byteOrderMark = 0xfeff.toChar()
        return """<?xpacket begin="$byteOrderMark" id="W5M0MpCehiHzreSzNTczkc9d"?>
<x:xmpmeta xmlns:x="adobe:ns:meta/">
  <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <rdf:Description rdf:about=""
      xmlns:Camera="http://ns.google.com/photos/1.0/camera/"
      xmlns:Container="http://ns.google.com/photos/1.0/container/"
      xmlns:Item="http://ns.google.com/photos/1.0/container/item/"
      Camera:MotionPhoto="1"
      Camera:MotionPhotoVersion="1"
      Camera:MotionPhotoPresentationTimestampUs="$presentationTimestampUs">
      <Container:Directory>
        <rdf:Seq>
          <rdf:li rdf:parseType="Resource" Item:Mime="image/jpeg" Item:Semantic="Primary"/>
          <rdf:li rdf:parseType="Resource" Item:Mime="video/mp4" Item:Semantic="MotionPhoto" Item:Length="$videoLength"/>
        </rdf:Seq>
      </Container:Directory>
    </rdf:Description>
  </rdf:RDF>
</x:xmpmeta>
<?xpacket end="w"?>"""
    }

    private fun app1Segment(videoLength: Long, presentationTimestampUs: Long): ByteArray {
        val xml = xmpPacket(videoLength, presentationTimestampUs).toByteArray(StandardCharsets.UTF_8)
        val payloadLength = XMP_HEADER.size + xml.size
        val segmentLength = payloadLength + 2
        if (segmentLength > 0xffff) throw IOException("Motion Photo XMP exceeds JPEG APP1 limit")
        return ByteArray(payloadLength + 4).also { segment ->
            segment[0] = 0xff.toByte()
            segment[1] = 0xe1.toByte()
            segment[2] = (segmentLength ushr 8).toByte()
            segment[3] = segmentLength.toByte()
            XMP_HEADER.copyInto(segment, 4)
            xml.copyInto(segment, 4 + XMP_HEADER.size)
        }
    }

    private fun validateJpeg(bytes: ByteArray) {
        require(bytes.size >= 4) { "JPEG is too short" }
        validateJpegPrefix(bytes.copyOfRange(0, 2))
        require(bytes[bytes.lastIndex - 1] == 0xff.toByte() && bytes.last() == 0xd9.toByte()) { "JPEG has no EOI" }
    }

    private fun validateJpegPrefix(prefix: ByteArray) {
        require(prefix.size >= 2 && prefix[0] == 0xff.toByte() && prefix[1] == 0xd8.toByte()) { "invalid JPEG SOI" }
    }

    private fun validateMp4(bytes: ByteArray) {
        require(bytes.size >= 12) { "MP4 is too short" }
        validateMp4Prefix(bytes.copyOfRange(0, 12))
    }

    private fun validateMp4Prefix(prefix: ByteArray) {
        require(prefix.size >= 8 && prefix.sliceArray(4..7).contentEquals(FTYPE)) { "MP4 has no ftyp box" }
    }

    private fun readPrefix(source: InputStream, byteCount: Int): ByteArray {
        val prefix = ByteArray(byteCount)
        var total = 0
        while (total < byteCount) {
            val read = source.read(prefix, total, byteCount - total)
            if (read < 0) break
            total += read
        }
        return if (total == byteCount) prefix else prefix.copyOf(total)
    }
}

object MotionTemporaryPolicy {
    const val MAX_RECORDING_DURATION_MS = 8_000L
    const val MAX_RECORDING_BYTES = 24L * 1024L * 1024L
    const val MAX_TEMP_AGE_MS = 10L * 60L * 1000L

    fun hasFullShutterWindow(elapsedSinceRecordingStartMs: Long): Boolean =
        elapsedSinceRecordingStartMs in MotionClipWindow.SIDE_US / 1_000L..MAX_RECORDING_DURATION_MS - MotionClipWindow.SIDE_US / 1_000L

    fun expired(files: List<File>, nowMillis: Long): List<File> = files.filter { file ->
        !file.isFile || file.length() > MAX_RECORDING_BYTES || nowMillis - file.lastModified() > MAX_TEMP_AGE_MS
    }
}
