package com.photocoach.app.analysis

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CoachAnalyzerCoordinateTest {
    @Test
    fun portraitRotationSwapsAnalysisDimensions() {
        assertEquals(960 to 1280, rotatedAnalysisDimensions(1280, 960, 90))
        assertEquals(960 to 1280, rotatedAnalysisDimensions(1280, 960, 270))
    }

    @Test
    fun fillCenterMatchesPreviewCropForFaceTapHitTesting() {
        val mapping = fillCenterMapping(960, 1280, 1440, 2400)

        assertEquals(1.875f, mapping.scale)
        assertEquals(-180f, mapping.offsetX)
        assertEquals(0f, mapping.offsetY)
        // A face at the center maps to the actual visible center, without letterbox offset.
        assertEquals(720f, 480f * mapping.scale + mapping.offsetX)
        assertEquals(1200f, 640f * mapping.scale + mapping.offsetY)
    }
}
