package com.photocoach.app.camera

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SaveWorkflowTest {
    @Test
    fun `partial failure preserves original and retries only failed recipe stage`() {
        val coordinator = SaveCoordinator(SavePlan(motionPhotoRequested = false, derivativeRequested = true))
        coordinator.complete(SaveStage.SPACE_CHECK)
        coordinator.complete(SaveStage.ORIGINAL_PUBLISH)
        val failed = coordinator.fail(SaveStage.RECIPE_WRITE, "disk full")

        assertTrue(failed.isPartialSuccess)
        assertEquals(setOf(SaveStage.SPACE_CHECK, SaveStage.ORIGINAL_PUBLISH), failed.completed)
        assertEquals(SaveStage.RECIPE_WRITE, coordinator.retryFailed())
        assertEquals(SaveStage.RECIPE_WRITE, coordinator.snapshot.nextStage)
        assertFalse(SaveStage.ORIGINAL_PUBLISH == coordinator.snapshot.nextStage)
    }

    @Test
    fun `motion packaging failure changes plan to ordinary jpeg without duplicate publish`() {
        val coordinator = SaveCoordinator(SavePlan(motionPhotoRequested = true, derivativeRequested = false))
        coordinator.complete(SaveStage.SPACE_CHECK)
        val fallback = coordinator.fallbackFromMotionPhoto("encoder unavailable")

        assertTrue(fallback.motionPhotoFallback)
        assertEquals(SaveStage.ORIGINAL_PUBLISH, fallback.nextStage)
        assertFalse(fallback.plan.motionPhotoRequested)
        assertNull(fallback.failedStage)
    }

    @Test
    fun `space preflight includes processing derivative and reserve`() {
        val estimate = SpaceEstimate(10, 20, 30, 40)
        assertEquals(100, estimate.requiredBytes)
        assertTrue(estimate.fits(100))
        assertFalse(estimate.fits(99))
    }

    @Test
    fun `safe EXIF policy excludes location maker note and unknown tags`() {
        assertTrue(SafeExifPolicy.shouldCopy("DateTimeOriginal"))
        assertTrue(SafeExifPolicy.shouldCopy("Orientation"))
        listOf("GPSLatitude", "MakerNote", "ThumbnailImage", "UserComment", "UnknownVendorTag").forEach {
            assertFalse(SafeExifPolicy.shouldCopy(it), it)
        }
    }
}
