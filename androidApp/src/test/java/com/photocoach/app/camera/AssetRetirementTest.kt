package com.photocoach.app.camera

import java.io.IOException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AssetRetirementTest {
    @Test fun deletionRefusalBlocksReplacement() {
        var replacementPublished = false
        assertThrows(IOException::class.java) {
            retireAsset({ true }, { 0 })
            replacementPublished = true
        }
        assertFalse(replacementPublished)
    }
    @Test fun successfulDeleteStillRequiresConfirmedAbsence() {
        assertThrows(IOException::class.java) { retireAsset({ true }, { 1 }) }
    }
    @Test fun unknownRowStateBlocksReplacement() {
        var deletes = 0
        assertThrows(IOException::class.java) { retireAsset({ throw IOException("query failed") }, { deletes++; 1 }) }
        assertEquals(0, deletes)
    }
    @Test fun absentRowOrConfirmedDeletionAllowsReplacement() {
        retireAsset({ false }, { error("absent row must not be deleted") })
        var exists = true
        retireAsset({ exists }, { exists = false; 1 })
        assertFalse(exists)
    }
    @Test fun motionPublishFailureCanFallBackButPublishedOriginalCannot() {
        val coordinator = SaveCoordinator(SavePlan(true, false))
        coordinator.complete(SaveStage.SPACE_CHECK)
        coordinator.complete(SaveStage.MOTION_PACKAGE)
        coordinator.fail(SaveStage.ORIGINAL_PUBLISH, "provider failure")
        coordinator.fallbackFromMotionPhoto("save jpeg")
        assertEquals(SaveStage.ORIGINAL_PUBLISH, coordinator.snapshot.nextStage)
        assertTrue(coordinator.snapshot.motionPhotoFallback)
        coordinator.complete(SaveStage.ORIGINAL_PUBLISH)
        assertThrows(IllegalArgumentException::class.java) { coordinator.fallbackFromMotionPhoto("must retain original") }
    }
}
