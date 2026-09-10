package com.photocoach.coach

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ExposureGuidancePolicyTest {
    @Test fun brightWallWithoutClippingDoesNotRequestDarkerExposure() {
        assertFalse(ExposureGuidancePolicy.shouldLower(0f, false))
        assertFalse(ExposureGuidancePolicy.shouldLower(.005f, false))
        assertFalse(ExposureGuidancePolicy.shouldLower(null, false))
        assertFalse(ExposureGuidancePolicy.shouldLower(Float.NaN, false))
        assertTrue(ExposureGuidancePolicy.shouldLower(.1f, false))
        assertFalse(ExposureGuidancePolicy.shouldLower(.1f, true))
        assertFalse(ExposureGuidancePolicy.shouldLower(.1f, null))
    }
    @Test fun scenesNeverAutomaticallyUnderexposeFaces() {
        val engine = CoachEngine.loadDefault()
        for (intent in ShotIntent.entries) for (dark in listOf(false, true)) {
            val output = engine.evaluate(Signals(faceCount = 1, faceRatio = .18f,
                coarseScene = CoarseScene.INDOOR, oneSideBrighter = true, faceDarkerThanScene = dark), intent)
            assertTrue(output.startParams.evBias == null || output.startParams.evBias!! >= 0f)
        }
    }
}
