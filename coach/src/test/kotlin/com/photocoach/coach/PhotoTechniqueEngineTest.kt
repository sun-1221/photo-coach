package com.photocoach.coach

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PhotoTechniqueEngineTest {
    @Test fun `unverified focal length is never invented`() {
        val signals = Signals(faceCount = 1, faceRatio = 0.03f, faceReliable = true)
        assertNull(PhotoTechniqueEngine.suggest(signals, TechniqueCapabilities()))
        val verified = PhotoTechniqueEngine.suggest(signals, TechniqueCapabilities(calibratedTelephotoLabel = "已验收长焦"))
        assertEquals(TechniqueCategory.FOCAL_DISTANCE, verified?.category)
        assertTrue(verified?.text?.contains("2x") == false)
    }

    @Test fun `multiple people only receive safe observable technique`() {
        val suggestion = PhotoTechniqueEngine.suggest(
            Signals(faceCount = 2, poseReliable = true, shouldersRaised = true, tiltDegrees = 5f),
            TechniqueCapabilities(calibratedTelephotoLabel = "长焦"),
        )
        assertEquals(TechniqueCategory.MULTI_PERSON_SAFE, suggestion?.category)
        assertEquals("沿网格放平手机", suggestion?.text)
    }

    @Test fun `unknown evidence stays quiet`() {
        assertNull(PhotoTechniqueEngine.suggest(Signals(faceCount = 1), TechniqueCapabilities()))
    }
}
