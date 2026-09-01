package com.photocoach.app.analysis

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LumaGridBackgroundDetailTest {
    private val subject = LumaRegion(6f, 6f, 10f, 10f)

    @Test fun `busy detail around subject has high edge density`() {
        val samples = ByteArray(16 * 16) { index ->
            val row = index / 16
            val column = index % 16
            if ((row + column) % 2 == 0) 20 else 220.toByte()
        }
        val density = LumaGrid(16, 16, 1, 0, samples).edgeDensityAround(subject)
        assertTrue(checkNotNull(density) > 0.8f)
    }

    @Test fun `flat background stays quiet`() {
        val density = LumaGrid(16, 16, 1, 0, ByteArray(16 * 16) { 100 }).edgeDensityAround(subject)
        assertEquals(0f, checkNotNull(density))
    }
}
