package com.photocoach.app.camera

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
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
        val jpeg = byteArrayOf(0xff.toByte(), 0xd8.toByte(), 1, 2, 3, 0xff.toByte(), 0xd9.toByte())
        val mp4 = byteArrayOf(0, 0, 0, 12, 'f'.code.toByte(), 't'.code.toByte(), 'y'.code.toByte(), 'p'.code.toByte(), 'i'.code.toByte(), 's'.code.toByte(), 'o'.code.toByte(), 'm'.code.toByte())
        val output = MotionPhotoAssembler.assemble(jpeg, mp4, 1_500_000)

        assertArrayEquals(mp4, output.copyOfRange(output.size - mp4.size, output.size))
        val jpegTail = output.copyOfRange(output.size - mp4.size - (jpeg.size - 2), output.size - mp4.size)
        assertArrayEquals(jpeg.copyOfRange(2, jpeg.size), jpegTail)
        assertEquals(0xff.toByte(), output[0])
        assertEquals(0xd8.toByte(), output[1])
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
}
