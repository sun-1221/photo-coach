package com.photocoach.app.camera

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
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
    internal fun verifySourceCompatibility(jpeg:File) {
        require(jpeg.isFile && jpeg.length()>=4L)
        RandomAccessFile(jpeg,"r").use {input ->
            inspectJpeg(input.length(),byteAt={offset ->input.seek(offset);input.read()},
                readBytes={offset,count ->ByteArray(count).also {input.seek(offset);input.readFully(it)}})
        }
    }
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
        val rewrite = inspectJpeg(
            length = jpeg.size.toLong(),
            byteAt = { offset -> jpeg[offset.toInt()].toInt() and 0xff },
            readBytes = { offset, count -> jpeg.copyOfRange(offset.toInt(), offset.toInt() + count) },
        )
        val app1 = app1Segment(mp4.size.toLong(), presentationTimestampUs)
        val cleanJpegSize = jpeg.size - rewrite.motionXmpRanges.sumOf { (it.endExclusive - it.start).toInt() }
        val output = java.io.ByteArrayOutputStream(cleanJpegSize + app1.size + mp4.size)
        output.write(jpeg, 0, 2)
        output.write(app1)
        copyByteArrayWithoutRanges(jpeg, output, rewrite.motionXmpRanges)
        output.write(mp4)
        return output.toByteArray()
    }

    fun assemble(
        jpeg: File,
        mp4: File,
        output: File,
        presentationTimestampUs: Long,
    ): MotionPhotoContainerInfo {
        val jpegPath = jpeg.canonicalFile
        val mp4Path = mp4.canonicalFile
        val outputPath = output.canonicalFile
        require(outputPath != jpegPath && outputPath != mp4Path) {
            "Motion Photo output must not overwrite either source"
        }
        require(!output.exists() || output.length() == 0L) {
            "Motion Photo output must be new or empty"
        }
        try {
            require(jpeg.isFile && jpeg.length() >= 4L) { "JPEG source is missing" }
            require(mp4.isFile && mp4.length() >= 12L) { "MP4 source is missing" }
            validateJpegPrefix(jpeg.inputStream().use { readPrefix(it, 2) })
            val rewrite = RandomAccessFile(jpeg, "r").use { input ->
                input.seek(input.length() - 2L)
                require(input.read() == 0xff && input.read() == 0xd9) { "JPEG has no EOI" }
                inspectJpeg(
                    length = input.length(),
                    byteAt = { offset ->
                        input.seek(offset)
                        input.read()
                    },
                    readBytes = { offset, count ->
                        ByteArray(count).also { bytes ->
                            input.seek(offset)
                            input.readFully(bytes)
                        }
                    },
                )
            }
            validateMp4Prefix(mp4.inputStream().use { readPrefix(it, 12) })
            val app1 = app1Segment(mp4.length(), presentationTimestampUs)
            output.parentFile?.let { check(it.exists() || it.mkdirs()) { "cannot create Motion Photo directory" } }
            output.outputStream().buffered().use { sink ->
                RandomAccessFile(jpeg, "r").use { source ->
                    sink.write(byteArrayOf(0xff.toByte(), 0xd8.toByte()))
                    sink.write(app1)
                    copyFileWithoutRanges(source, sink, rewrite.motionXmpRanges)
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
        return """<x:xmpmeta xmlns:x="adobe:ns:meta/">
  <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <rdf:Description rdf:about=""
      xmlns:GCamera="http://ns.google.com/photos/1.0/camera/"
      xmlns:Container="http://ns.google.com/photos/1.0/container/"
      xmlns:Item="http://ns.google.com/photos/1.0/container/item/"
      GCamera:MotionPhoto="1"
      GCamera:MotionPhotoVersion="1"
      GCamera:MotionPhotoPresentationTimestampUs="$presentationTimestampUs">
      <Container:Directory>
        <rdf:Seq>
          <rdf:li rdf:parseType="Resource">
            <Container:Item Item:Mime="image/jpeg" Item:Semantic="Primary"/>
          </rdf:li>
          <rdf:li rdf:parseType="Resource">
            <Container:Item Item:Mime="video/mp4" Item:Semantic="MotionPhoto" Item:Length="$videoLength" Item:Padding="0"/>
          </rdf:li>
        </rdf:Seq>
      </Container:Directory>
    </rdf:Description>
  </rdf:RDF>
</x:xmpmeta>"""
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

    private fun inspectJpeg(
        length: Long,
        byteAt: (Long) -> Int,
        readBytes: (Long, Int) -> ByteArray,
    ): JpegRewrite {
        val motionXmpRanges = mutableListOf<ByteRange>()
        var offset = 2L
        while (offset + 1L < length) {
            val markerStart = offset
            if (byteAt(offset) != 0xff) throw IOException("invalid JPEG marker before scan data")
            while (offset < length && byteAt(offset) == 0xff) offset += 1L
            if (offset >= length) throw IOException("truncated JPEG marker")
            val marker = byteAt(offset)
            offset += 1L
            if (marker == 0xda || marker == 0xd9) break
            if (marker == 0x01 || marker in 0xd0..0xd7) continue
            if (offset + 2L > length) throw IOException("truncated JPEG segment")
            val segmentLength = (byteAt(offset) shl 8) or byteAt(offset + 1L)
            if (segmentLength < 2 || offset + segmentLength > length) {
                throw IOException("invalid JPEG segment length")
            }
            val payloadOffset = offset + 2L
            val payloadLength = segmentLength - 2
            val segmentEnd = offset + segmentLength
            if (marker == 0xe1 && payloadLength > 0) {
                val payload = readBytes(payloadOffset, payloadLength)
                if (isXmpPayload(payload)) {
                    val xmp = payload.toString(StandardCharsets.UTF_8)
                    when {
                        isGainMapXmp(xmp) ->
                            throw IOException("Ultra HDR/GainMap JPEG cannot be packaged as Motion Photo")
                        isReplaceableMotionPhotoXmp(xmp) ->
                            motionXmpRanges += ByteRange(markerStart, segmentEnd)
                        isMotionPhotoXmp(xmp) ->
                            throw IOException("existing Motion XMP contains metadata that cannot be safely replaced")
                        else ->
                            throw IOException("existing non-Motion XMP cannot be safely merged")
                    }
                }
            }
            offset = segmentEnd
        }
        return JpegRewrite(motionXmpRanges)
    }

    private fun isXmpPayload(payload: ByteArray): Boolean =
        payload.startsWith(XMP_HEADER) ||
            payload.toString(StandardCharsets.US_ASCII).startsWith("http://ns.adobe.com/xmp/extension/")

    private fun isMotionPhotoXmp(xmp: String): Boolean =
        xmp.contains("Camera:MotionPhoto=") ||
            xmp.contains("GCamera:MotionPhoto=") ||
            xmp.contains("GCamera:MicroVideo=")

    private fun isReplaceableMotionPhotoXmp(xmp: String): Boolean {
        val expected = XMP_HEADER.toString(StandardCharsets.UTF_8) + xmpPacket(1L, -1L)
        return normalizeOwnedMotionXmp(xmp) == normalizeOwnedMotionXmp(expected)
    }

    private fun normalizeOwnedMotionXmp(xmp: String): String = xmp
        .replace(
            Regex("""GCamera:MotionPhotoPresentationTimestampUs="-?\d+""""),
            """GCamera:MotionPhotoPresentationTimestampUs="{timestamp}"""",
        )
        .replace(
            Regex("""Item:Length="\d+""""),
            """Item:Length="{length}"""",
        )

    private fun isGainMapXmp(xmp: String): Boolean =
        xmp.contains("Item:Semantic=\"GainMap\"") ||
            xmp.contains("hdrgm:") ||
            xmp.contains("GainMapMin") ||
            xmp.contains("GainMapMax")

    private fun ByteArray.startsWith(prefix: ByteArray): Boolean =
        size >= prefix.size && prefix.indices.all { index -> this[index] == prefix[index] }

    private fun copyByteArrayWithoutRanges(
        jpeg: ByteArray,
        sink: OutputStream,
        ranges: List<ByteRange>,
    ) {
        var cursor = 2
        for (range in ranges.sortedBy(ByteRange::start)) {
            val start = range.start.toInt()
            if (start > cursor) sink.write(jpeg, cursor, start - cursor)
            cursor = range.endExclusive.toInt()
        }
        if (cursor < jpeg.size) sink.write(jpeg, cursor, jpeg.size - cursor)
    }

    private fun copyFileWithoutRanges(
        source: RandomAccessFile,
        sink: OutputStream,
        ranges: List<ByteRange>,
    ) {
        var cursor = 2L
        for (range in ranges.sortedBy(ByteRange::start)) {
            copyFileRange(source, sink, cursor, range.start)
            cursor = range.endExclusive
        }
        copyFileRange(source, sink, cursor, source.length())
    }

    private fun copyFileRange(source: RandomAccessFile, sink: OutputStream, start: Long, endExclusive: Long) {
        if (endExclusive <= start) return
        source.seek(start)
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var remaining = endExclusive - start
        while (remaining > 0L) {
            val read = source.read(buffer, 0, minOf(buffer.size.toLong(), remaining).toInt())
            if (read < 0) throw IOException("truncated JPEG while rewriting XMP")
            sink.write(buffer, 0, read)
            remaining -= read
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

    private data class ByteRange(val start: Long, val endExclusive: Long)
    private data class JpegRewrite(val motionXmpRanges: List<ByteRange>)
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
