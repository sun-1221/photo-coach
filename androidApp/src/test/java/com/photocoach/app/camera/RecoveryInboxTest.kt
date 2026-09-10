package com.photocoach.app.camera

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class RecoveryInboxTest {
    @Test fun deferOldRecoveryThenRetryNewCaptureHasIndependentIdentity() {
        val old = SaveJournal(captureId = "oldcapture0001", sequence = 1, takenAtMillis = 1,
            sourcePath = "old.jpg", displayName = "old.JPG", style = "ORIGINAL", error = "disk full")
        val inbox = RecoveryInbox()
        inbox.refresh(listOf(old)); inbox.show(true); inbox.show(false)
        assertEquals(listOf(old), inbox.records)
        assertNull(inbox.retryingKey)
        val current = SaveCoordinator(SavePlan(false, false))
        current.fail(SaveStage.SPACE_CHECK, "current disk full")
        assertEquals(SaveStage.SPACE_CHECK, current.retryFailed())
        assertNull(inbox.retryingKey)
        inbox.show(true)
        assertTrue(inbox.beginRetry(old.key))
        assertFalse(inbox.beginRetry(old.key))
        inbox.refresh(emptyList())
        assertNull(inbox.retryingKey)
        assertTrue(inbox.records.isEmpty())
    }
}
