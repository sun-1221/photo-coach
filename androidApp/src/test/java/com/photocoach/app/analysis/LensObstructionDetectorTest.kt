package com.photocoach.app.analysis

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LensObstructionDetectorTest {
    @Test
    fun requiresSeveralVeryDarkFlatFramesAndClearsConservatively() {
        val detector = LensObstructionDetector(requiredObscuredFrames = 3, requiredClearFrames = 2)

        assertFalse(detector.update(stats(mean = 10f, deviation = 2f)))
        assertFalse(detector.update(stats(mean = 10f, deviation = 2f)))
        assertTrue(detector.update(stats(mean = 10f, deviation = 2f)))
        assertTrue(detector.update(stats(mean = 60f, deviation = 20f)))
        assertFalse(detector.update(stats(mean = 60f, deviation = 20f)))
    }

    @Test
    fun ordinaryDarkOrLowDetailFrameDoesNotTrigger() {
        val detector = LensObstructionDetector(requiredObscuredFrames = 2)

        assertFalse(detector.update(stats(mean = 12f, deviation = 10f)))
        assertFalse(detector.update(stats(mean = 80f, deviation = 2f)))
    }

    private fun stats(mean: Float, deviation: Float) = FrameStats(
        meanY = mean,
        leftMean = mean,
        rightMean = mean,
        topMean = mean,
        stdY = deviation,
        warmScore = 0f,
        verticalEnergy = 0f,
        horizontalEnergy = 0f,
    )
}
