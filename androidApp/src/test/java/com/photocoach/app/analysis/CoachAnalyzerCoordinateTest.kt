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
    fun fitCenterKeepsAspectAndCentersLetterbox() {
        val mapping = fitCenterMapping(960, 1280, 1440, 2400)

        assertEquals(1.5f, mapping.scale)
        assertEquals(0f, mapping.offsetX)
        assertEquals(240f, mapping.offsetY)
    }
}
