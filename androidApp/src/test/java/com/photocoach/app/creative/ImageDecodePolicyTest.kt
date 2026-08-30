package com.photocoach.app.creative

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ImageDecodePolicyTest {
    @Test
    fun `export preview and thumbnail limits share a descending bounded policy`() {
        assertEquals(2, ImageDecodePolicy.inSampleSize(8_000, 6_000, ImageDecodePolicy.EXPORT_MAX_PIXELS))
        assertEquals(8, ImageDecodePolicy.inSampleSize(8_000, 6_000, ImageDecodePolicy.RESULT_PREVIEW_MAX_PIXELS))
        assertEquals(16, ImageDecodePolicy.inSampleSize(8_000, 6_000, ImageDecodePolicy.THUMBNAIL_MAX_PIXELS))

        assertTrue(ImageDecodePolicy.RESULT_PREVIEW_MAX_PIXELS < ImageDecodePolicy.EXPORT_MAX_PIXELS)
        assertTrue(ImageDecodePolicy.THUMBNAIL_MAX_PIXELS < ImageDecodePolicy.RESULT_PREVIEW_MAX_PIXELS)
    }

    @Test
    fun `power of two sample never exceeds requested pixel ceiling`() {
        val sample = ImageDecodePolicy.inSampleSize(4_032, 3_024, 2_000_000L)

        assertEquals(4, sample)
        assertTrue(ImageDecodePolicy.decodedPixels(4_032, 3_024, sample) <= 2_000_000L)
    }
}
