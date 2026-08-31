package com.photocoach.app.camera

import androidx.camera.core.ImageCapture
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class LiveSessionFallbackTest {
    @Test
    fun qualityOrderIsHdThenSd() {
        assertEquals(listOf(LiveVideoTier.HD, LiveVideoTier.SD), LIVE_VIDEO_TIER_FALLBACK_ORDER)
    }

    @Test
    fun staticCaptureUsesOrdinarySdrJpegSemantics() {
        assertEquals(ImageCapture.OUTPUT_FORMAT_JPEG, stillCaptureOutputFormat())
        assertNotEquals(ImageCapture.OUTPUT_FORMAT_JPEG_ULTRA_HDR, stillCaptureOutputFormat())
    }

    @Test
    fun unsupportedHdFallsBackToSdWithoutTryingToBindHd() {
        val attempted = mutableListOf<String>()

        val selected = firstBindableLiveCandidate(
            candidates = listOf("HD", "SD"),
            isSupported = { it == "SD" },
            tryBind = { attempted += it; true },
        )

        assertEquals("SD", selected)
        assertEquals(listOf("SD"), attempted)
    }

    @Test
    fun hdBindingFailureFallsBackToSdAndStopsAfterSuccess() {
        val attempted = mutableListOf<String>()

        val selected = firstBindableLiveCandidate(
            candidates = listOf("HD", "SD", "LOWEST"),
            isSupported = { true },
            tryBind = {
                attempted += it
                it == "SD"
            },
        )

        assertEquals("SD", selected)
        assertEquals(listOf("HD", "SD"), attempted)
    }

    @Test
    fun noSupportedCandidateReturnsNullWithoutBinding() {
        var bindCount = 0

        val selected = firstBindableLiveCandidate(
            candidates = listOf("HD", "SD"),
            isSupported = { false },
            tryBind = { bindCount += 1; true },
        )

        assertNull(selected)
        assertEquals(0, bindCount)
    }
}
