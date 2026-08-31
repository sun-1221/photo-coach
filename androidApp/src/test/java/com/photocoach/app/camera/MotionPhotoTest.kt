package com.photocoach.app.camera

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.io.File

class MotionPhotoTest {
    @TempDir
    lateinit var temporaryDirectory: Path
    @Test
    fun `v1 xmp declares camera and exactly one primary plus terminal motion item`() {
        val xmp = MotionPhotoAssembler.xmpPacket(videoLength = 12_345, presentationTimestampUs = 1_500_000)
        assertTrue(xmp.contains("Camera:MotionPhoto=\"1\""))
        assertTrue(xmp.contains("Camera:MotionPhotoVersion=\"1\""))
        assertTrue(xmp.contains("Camera:MotionPhotoPresentationTimestampUs=\"1500000\""))
        assertEquals(1, Regex("Item:Semantic=\"Primary\"").findAll(xmp).count())
        assertEquals(1, Regex("Item:Semantic=\"MotionPhoto\"").findAll(xmp).count())
        assertTrue(xmp.contains("Item:Length=\"12345\""))
    }

    @Test
    fun `container preserves jpeg bytes and appends mp4 as final bytes`() {
        val jpeg = minimalJpeg()
        val mp4 = byteArrayOf(0, 0, 0, 12, 'f'.code.toByte(), 't'.code.toByte(), 'y'.code.toByte(), 'p'.code.toByte(), 'i'.code.toByte(), 's'.code.toByte(), 'o'.code.toByte(), 'm'.code.toByte())
        val output = MotionPhotoAssembler.assemble(jpeg, mp4, 1_500_000)

        assertArrayEquals(mp4, output.copyOfRange(output.size - mp4.size, output.size))
        val jpegTail = output.copyOfRange(output.size - mp4.size - (jpeg.size - 2), output.size - mp4.size)
        assertArrayEquals(jpeg.copyOfRange(2, jpeg.size), jpegTail)
        assertEquals(0xff.toByte(), output[0])
        assertEquals(0xd8.toByte(), output[1])
    }

    @Test
    fun `existing Motion Photo xmp is replaced instead of duplicated`() {
        val jpeg = minimalJpeg()
        val firstMp4 = mp4("isom")
        val firstContainer = MotionPhotoAssembler.assemble(jpeg, firstMp4, 1_000_000)
        val jpegWithMotionXmp = firstContainer.copyOf(firstContainer.size - firstMp4.size)
        val secondMp4 = mp4("mp42")

        val output = MotionPhotoAssembler.assemble(jpegWithMotionXmp, secondMp4, 1_500_000)
        val text = output.toString(StandardCharsets.ISO_8859_1)

        assertEquals(1, Regex("Camera:MotionPhoto=\\\"").findAll(text).count())
        assertArrayEquals(secondMp4, output.copyOfRange(output.size - secondMp4.size, output.size))
    }

    @Test
    fun `unrelated xmp and gain map xmp fall back instead of creating ambiguous jpeg`() {
        val unrelated = jpegWithXmp("<x:xmpmeta xmlns:x=\"adobe:ns:meta/\"><custom:value>keep</custom:value></x:xmpmeta>")
        val gainMap = jpegWithXmp("<rdf:li Item:Semantic=\"GainMap\" hdrgm:Version=\"1.0\"/>")
        val video = mp4("isom")

        assertThrows(IOException::class.java) {
            MotionPhotoAssembler.assemble(unrelated, video, 1_500_000)
        }
        assertThrows(IOException::class.java) {
            MotionPhotoAssembler.assemble(gainMap, video, 1_500_000)
        }
    }

    @Test
    fun `motion xmp mixed with unrelated metadata is preserved by rejecting rewrite`() {
        val mixedPacket = MotionPhotoAssembler.xmpPacket(128, 1_000_000).replace(
            "<rdf:Description rdf:about=\"\"",
            "<rdf:Description rdf:about=\"\" xmlns:xmp=\"http://ns.adobe.com/xap/1.0/\" xmp:CreatorTool=\"OtherCamera\"",
        )
        val jpeg = jpegWithXmp(mixedPacket)
        val original = jpeg.copyOf()

        assertThrows(IOException::class.java) {
            MotionPhotoAssembler.assemble(jpeg, mp4("isom"), 1_500_000)
        }
        assertArrayEquals(original, jpeg)
    }

    @Test
    fun `non xmp jpeg metadata is preserved byte for byte`() {
        val app0 = jpegSegment(0xe0, "JFIF-metadata".toByteArray(StandardCharsets.US_ASCII))
        val exif = jpegSegment(0xe1, "Exif\u0000\u0000safe-metadata".toByteArray(StandardCharsets.US_ASCII))
        val jpeg = byteArrayOf(0xff.toByte(), 0xd8.toByte()) + app0 + exif +
            byteArrayOf(0xff.toByte(), 0xd9.toByte())

        val output = MotionPhotoAssembler.assemble(jpeg, mp4("isom"), 1_500_000)

        assertTrue(output.containsSubsequence(app0))
        assertTrue(output.containsSubsequence(exif))
    }

    @Test
    fun `file assembly rejects source overwrite and leaves jpeg unchanged`() {
        val jpeg = File(temporaryDirectory.toFile(), "source.jpg").apply { writeBytes(minimalJpeg()) }
        val video = File(temporaryDirectory.toFile(), "source.mp4").apply { writeBytes(mp4("isom")) }
        val original = jpeg.readBytes()

        assertThrows(IllegalArgumentException::class.java) {
            MotionPhotoAssembler.assemble(jpeg, video, jpeg, 1_500_000)
        }
        assertArrayEquals(original, jpeg.readBytes())
    }

    @Test
    fun `file assembly reports real terminal video offset`() {
        val jpeg = File(temporaryDirectory.toFile(), "cover.jpg").apply { writeBytes(minimalJpeg()) }
        val video = File(temporaryDirectory.toFile(), "clip.mp4").apply { writeBytes(mp4("mp42")) }
        val output = File(temporaryDirectory.toFile(), "container.jpg")

        val info = MotionPhotoAssembler.assemble(jpeg, video, output, 1_500_000)
        val bytes = output.readBytes()

        assertEquals(video.length(), info.videoLength)
        assertEquals(output.length() - video.length(), info.videoOffset)
        assertArrayEquals(video.readBytes(), bytes.copyOfRange(info.videoOffset.toInt(), bytes.size))
    }

    @Test
    fun `file validation failure removes empty output but never overwrites a nonempty target`() {
        val invalidJpeg = File(temporaryDirectory.toFile(), "invalid.jpg").apply {
            writeBytes(byteArrayOf(1, 2, 3, 4))
        }
        val video = File(temporaryDirectory.toFile(), "fallback.mp4").apply { writeBytes(mp4("isom")) }
        val emptyOutput = File(temporaryDirectory.toFile(), "empty-output.jpg").apply { createNewFile() }

        assertThrows(IllegalArgumentException::class.java) {
            MotionPhotoAssembler.assemble(invalidJpeg, video, emptyOutput, 1_500_000)
        }
        assertFalse(emptyOutput.exists())

        val validJpeg = File(temporaryDirectory.toFile(), "valid.jpg").apply { writeBytes(minimalJpeg()) }
        val occupiedOutput = File(temporaryDirectory.toFile(), "occupied.jpg").apply { writeText("keep-me") }
        assertThrows(IllegalArgumentException::class.java) {
            MotionPhotoAssembler.assemble(validJpeg, video, occupiedOutput, 1_500_000)
        }
        assertEquals("keep-me", occupiedOutput.readText())
    }

    @Test
    fun `truncated jpeg segment is rejected`() {
        val truncated = byteArrayOf(
            0xff.toByte(), 0xd8.toByte(),
            0xff.toByte(), 0xe1.toByte(), 0, 16, 1, 2,
            0xff.toByte(), 0xd9.toByte(),
        )

        assertThrows(IOException::class.java) {
            MotionPhotoAssembler.assemble(truncated, mp4("isom"), 1_500_000)
        }
    }

    @Test
    fun `clip window keeps about one point five seconds around shutter and clamps boundaries`() {
        val centered = MotionClipWindow.aroundShutter(0, 4_000_000, 8_000_000)
        assertEquals(2_500_000, centered.startUs)
        assertEquals(5_500_000, centered.endUs)
        assertEquals(1_500_000, centered.presentationTimestampUs)

        val nearStart = MotionClipWindow.aroundShutter(0, 500_000, 8_000_000)
        assertEquals(0, nearStart.startUs)
        assertEquals(3_000_000, nearStart.endUs)
        assertEquals(500_000, nearStart.presentationTimestampUs)
        assertTrue(MotionTemporaryPolicy.hasFullShutterWindow(1_500L))
        assertTrue(MotionTemporaryPolicy.hasFullShutterWindow(6_500L))
        assertEquals(false, MotionTemporaryPolicy.hasFullShutterWindow(1_499L))
        assertEquals(false, MotionTemporaryPolicy.hasFullShutterWindow(6_501L))
    }

    @Test
    fun `invalid jpeg or mp4 is rejected for reliable ordinary photo fallback`() {
        val jpeg = byteArrayOf(0xff.toByte(), 0xd8.toByte(), 0xff.toByte(), 0xd9.toByte())
        assertThrows(IllegalArgumentException::class.java) {
            MotionPhotoAssembler.assemble(jpeg, ByteArray(12), 0)
        }
    }

    @Test
    fun `temporary policy removes oversized stale and missing entries but keeps bounded fresh video`() {
        val now = 2_000_000L
        val fresh = File(temporaryDirectory.toFile(), "fresh.mp4").apply {
            writeBytes(ByteArray(32))
            setLastModified(now)
        }
        val stale = File(temporaryDirectory.toFile(), "stale.mp4").apply {
            writeBytes(ByteArray(32))
            setLastModified(now - MotionTemporaryPolicy.MAX_TEMP_AGE_MS - 1)
        }
        val missing = File(temporaryDirectory.toFile(), "missing.mp4")
        val expired = MotionTemporaryPolicy.expired(listOf(fresh, stale, missing), now)
        assertEquals(listOf(stale, missing), expired)
    }

    private fun mp4(brand: String): ByteArray =
        byteArrayOf(
            0, 0, 0, 12,
            'f'.code.toByte(), 't'.code.toByte(), 'y'.code.toByte(), 'p'.code.toByte(),
            *brand.toByteArray(StandardCharsets.US_ASCII),
        )

    private fun minimalJpeg(): ByteArray =
        byteArrayOf(
            0xff.toByte(), 0xd8.toByte(),
            0xff.toByte(), 0xda.toByte(), 0, 2,
            1, 2, 3,
            0xff.toByte(), 0xd9.toByte(),
        )

    private fun jpegWithXmp(xml: String): ByteArray {
        val header = "http://ns.adobe.com/xap/1.0/\u0000".toByteArray(StandardCharsets.US_ASCII)
        val payload = header + xml.toByteArray(StandardCharsets.UTF_8)
        return byteArrayOf(0xff.toByte(), 0xd8.toByte()) +
            jpegSegment(0xe1, payload) +
            byteArrayOf(0xff.toByte(), 0xd9.toByte())
    }

    private fun jpegSegment(marker: Int, payload: ByteArray): ByteArray {
        val segmentLength = payload.size + 2
        return byteArrayOf(
            0xff.toByte(),
            marker.toByte(),
            (segmentLength ushr 8).toByte(),
            segmentLength.toByte(),
        ) + payload
    }

    private fun ByteArray.containsSubsequence(expected: ByteArray): Boolean =
        indices.any { start ->
            start + expected.size <= size && expected.indices.all { offset -> this[start + offset] == expected[offset] }
        }
}
