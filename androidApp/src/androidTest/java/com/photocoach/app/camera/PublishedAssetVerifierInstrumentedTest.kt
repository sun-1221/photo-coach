package com.photocoach.app.camera

import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PublishedAssetVerifierInstrumentedTest {
    @Test
    fun publishAcceptsBoundsOnlyDecodeAndLeavesNoPendingRow() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val source = File(context.cacheDir, "published-verifier-${System.nanoTime()}.jpg")
        Bitmap.createBitmap(8, 6, Bitmap.Config.ARGB_8888).let { bitmap ->
            try {
                bitmap.eraseColor(Color.rgb(48, 96, 144))
                source.outputStream().use { assertTrue(bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it)) }
            } finally {
                bitmap.recycle()
            }
        }
        var published: android.net.Uri? = null
        try {
            published = CaptureSaver.publish(context.contentResolver, source)
            val verified = PublishedAssetVerifier.verifyPublished(
                context.contentResolver,
                published,
                requireNotNull(published).lastPathSegment?.let { _ -> queryDisplayName(context, published) }.orEmpty(),
                CaptureSaver.RELATIVE_DIR,
                motionPhoto = false,
            )
            assertEquals(8, verified.width)
            assertEquals(6, verified.height)
            assertTrue(verified.length > 0L)
        } finally {
            CaptureSaver.deleteQuietly(context.contentResolver, published)
            source.delete()
        }
    }

    private fun queryDisplayName(context: android.content.Context, uri: android.net.Uri?): String {
        requireNotNull(uri)
        return context.contentResolver.query(
            uri,
            arrayOf(android.provider.MediaStore.Images.Media.DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { cursor ->
            check(cursor.moveToFirst())
            cursor.getString(0)
        }.orEmpty()
    }
}
