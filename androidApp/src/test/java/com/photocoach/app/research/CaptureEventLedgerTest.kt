package com.photocoach.app.research

import com.photocoach.app.camera.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CaptureEventLedgerTest {
    @Test fun delayedImmutableProgressKeepsOldIdentityAndPublishesEachCaptureOnce() {
        val ledger = CaptureEventLedger()
        val coordinator = SaveCoordinator(SavePlan(false, false))
        ledger.register("first", 1, "close_up")
        coordinator.complete(SaveStage.SPACE_CHECK)
        coordinator.complete(SaveStage.ORIGINAL_PUBLISH)
        val queued = CaptureSaveProgress("first", coordinator.snapshot)
        coordinator.complete(SaveStage.RECIPE_WRITE)
        ledger.register("second", 2, "person_with_scenery")
        assertEquals(SaveStage.RECIPE_WRITE, queued.snapshot.nextStage)
        val first = ledger.published(queued.captureId)
        assertEquals(1, first?.roundId)
        assertEquals("close_up", first?.intent)
        assertEquals(null, ledger.published("first")) // onPhotoCaptured fallback is duplicate
        assertEquals(2, ledger.published("second")?.roundId) // fallback without any progress
        assertEquals(null, ledger.published("second"))
    }
}
