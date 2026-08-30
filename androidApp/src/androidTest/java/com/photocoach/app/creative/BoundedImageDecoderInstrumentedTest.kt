package com.photocoach.app.creative

import android.graphics.Bitmap
import android.net.Uri
import androidx.core.graphics.createBitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.io.IOException
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BoundedImageDecoderInstrumentedTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val files = mutableListOf<File>()

    @After
    fun cleanUp() {
        files.forEach { it.delete() }
    }

    @Test
    fun decoderDownsamplesWithinTheExplicitPixelLimit() {
        val source = jpeg("bounded-source", width = 1_200, height = 900)
        val decoded = BoundedImageDecoder(context.contentResolver).decode(Uri.fromFile(source), 100_000L)
        try {
            assertTrue(decoded.bitmap.width.toLong() * decoded.bitmap.height <= 100_000L)
            assertTrue(decoded.wasDownsampled)
        } finally {
            decoded.bitmap.recycle()
        }
    }

    @Test
    fun temporaryFileCreationFailureLeavesOriginalBytesUntouched() {
        val source = jpeg("owned-source", width = 320, height = 240)
        val original = source.readBytes()
        val invalidCacheRoot = File(context.cacheDir, "creative-cache-file-${System.nanoTime()}")
            .also {
                it.writeText("not a directory")
                files += it
            }

        assertThrows(IllegalStateException::class.java) {
            CreativeImageProcessor(invalidCacheRoot).process(source, CreativeStyle.NATURAL_PORTRAIT)
        }

        assertTrue(source.isFile)
        assertArrayEquals(original, source.readBytes())
    }

    @Test
    fun unreadableUriFailsWithoutAllocatingAnUnboundedPlaceholder() {
        val missing = Uri.fromFile(File(context.cacheDir, "missing-${System.nanoTime()}.jpg"))

        assertThrows(IOException::class.java) {
            BoundedImageDecoder(context.contentResolver).decode(
                missing,
                ImageDecodePolicy.RESULT_PREVIEW_MAX_PIXELS,
            )
        }
    }

    private fun jpeg(name: String, width: Int, height: Int): File {
        val file = File(context.cacheDir, "$name-${System.nanoTime()}.jpg").also(files::add)
        val bitmap = createBitmap(width, height)
        try {
            file.outputStream().use { stream ->
                check(bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream))
            }
        } finally {
            bitmap.recycle()
        }
        return file
    }
}
