package com.photocoach.app.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MotionPhotoFileInstrumentedTest {
    @Test
    fun fileAssemblerPreservesJpegPayloadWritesV1XmpAndCleansFailureOutput() {
        val cache = InstrumentationRegistry.getInstrumentation().targetContext.cacheDir
        val directory = File(cache, "motion-photo-instrumented").apply { mkdirs() }
        val jpeg = File(directory, "cover.jpg")
        Bitmap.createBitmap(4, 3, Bitmap.Config.ARGB_8888).let { bitmap ->
            try {
                bitmap.eraseColor(Color.rgb(32, 96, 160))
                jpeg.outputStream().use { assertTrue(bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it)) }
            } finally {
                bitmap.recycle()
            }
        }
        val jpegBytes = jpeg.readBytes()
        val mp4Bytes = byteArrayOf(0, 0, 0, 12, 'f'.code.toByte(), 't'.code.toByte(), 'y'.code.toByte(), 'p'.code.toByte(), 'i'.code.toByte(), 's'.code.toByte(), 'o'.code.toByte(), 'm'.code.toByte())
        val mp4 = File(directory, "motion.mp4").apply { writeBytes(mp4Bytes) }
        val output = File(directory, "resultMP.JPG")

        try {
            val info = MotionPhotoAssembler.assemble(jpeg, mp4, output, 1_500_000)
            val result = output.readBytes()
            assertEquals(mp4.length(), info.videoLength)
            assertArrayEquals(mp4Bytes, result.copyOfRange(result.size - mp4Bytes.size, result.size))
            assertTrue(String(result, Charsets.UTF_8).contains("Camera:MotionPhotoVersion=\"1\""))
            assertTrue(CaptureNameChecks.isMotionPhotoName(output.name))
            BitmapFactory.decodeFile(output.absolutePath).let { decoded ->
                assertNotNull("Motion Photo 主图必须仍可被 Android JPEG 解码器读取", decoded)
                requireNotNull(decoded)
                try {
                    assertEquals(4, decoded.width)
                    assertEquals(3, decoded.height)
                } finally {
                    decoded.recycle()
                }
            }

            val broken = File(directory, "brokenMP.JPG")
            runCatching { MotionPhotoAssembler.assemble(File(directory, "missing.jpg"), mp4, broken, 0) }
            assertFalse(broken.exists())
        } finally {
            listOf(output, jpeg, mp4).forEach(File::delete)
            directory.delete()
        }
    }
}

private object CaptureNameChecks {
    fun isMotionPhotoName(name: String): Boolean = name.endsWith("MP.JPG")
}
