package com.photocoach.app.analysis

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FaceLuminanceClassifierTest {
    @Test
    fun actualFaceSamplesMustBeDarkerThanSamplesOutsideTheFace() {
        val samples = ByteArray(100) { 140.toByte() }
        for (y in 2 until 8) {
            for (x in 3 until 7) samples[y * 10 + x] = 90.toByte()
        }
        val grid = LumaGrid(10, 10, 1, 0, samples)

        assertTrue(
            FaceLuminanceClassifier.isDarkerThanBackground(
                grid,
                LumaRegion(3f, 2f, 7f, 8f),
            ),
        )
    }

    @Test
    fun facePositionAloneCannotInventBacklight() {
        val grid = LumaGrid(10, 10, 1, 0, ByteArray(100) { 110.toByte() })

        assertFalse(
            FaceLuminanceClassifier.isDarkerThanBackground(
                grid,
                LumaRegion(3f, 0f, 7f, 6f),
            ),
        )
    }

    @Test
    fun gridCoordinatesFollowCameraRotation() {
        val samples = byteArrayOf(10, 20, 30, 40, 50, 60)
        val expected = mapOf(
            0 to LumaRegion(0f, 0f, 1f, 1f),
            90 to LumaRegion(1f, 0f, 2f, 1f),
            180 to LumaRegion(2f, 1f, 3f, 2f),
            270 to LumaRegion(0f, 2f, 1f, 3f),
        )

        expected.forEach { (rotation, region) ->
            val grid = LumaGrid(3, 2, 1, rotation, samples)
            assertEquals(10f, grid.mean(region), "rotation=$rotation")
        }
    }

    @Test
    fun partiallyOutOfFrameFaceIsClippedBeforeComparingItsCore() {
        val samples = ByteArray(12 * 12) { 150.toByte() }
        for (y in 2 until 10) {
            for (x in 0 until 6) samples[y * 12 + x] = 80.toByte()
        }
        val grid = LumaGrid(12, 12, 1, 0, samples)

        assertTrue(
            FaceLuminanceClassifier.isDarkerThanBackground(
                grid,
                LumaRegion(-2f, 2f, 6f, 10f),
            ),
        )
    }

    @Test
    fun invalidOrUndersampledFaceRegionStaysUnknown() {
        val grid = LumaGrid(8, 8, 1, 0, ByteArray(64) { 140.toByte() })

        assertFalse(FaceLuminanceClassifier.isDarkerThanBackground(grid, LumaRegion(2f, 2f, 4f, 4f)))
        assertFalse(
            FaceLuminanceClassifier.isDarkerThanBackground(
                grid,
                LumaRegion(Float.NaN, 1f, 6f, 6f),
            ),
        )
    }
}
