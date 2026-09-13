package com.photocoach.app

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CaptureSaveCallbacksTest {
    @Test fun actualRetryWiringKeepsOriginalIdentityAfterAnotherCaptureStarts() {
        var pending = "A"
        var deferredError: ((Throwable) -> Unit)? = null
        val reports = mutableListOf<Pair<String?, Boolean>>()
        var refreshed = 0
        var settled = 0
        retryCapturedSave<Unit>(captureId = pending, begin = { true },
            retry = { id, _, error ->
                assertEquals("A", id)
                deferredError = error
                true
            }, saved = {}, failureFor = { id -> CaptureSaveFailureHandler(id, { true },
                { _, retry, captured -> reports += captured to retry }, { refreshed++ }, { settled++ }) })
        pending = "B" // A published its original and detached; a new capture now owns the UI.
        deferredError!!(IllegalStateException("A effect failed"))
        assertEquals("B", pending)
        assertEquals(listOf("A" to true), reports)
        assertEquals(1, refreshed)
        assertEquals(1, settled)
    }

    @Test fun rejectedRetryAndLateFailureAfterDestroyUseOwnedIdentityAndSafeRefresh() {
        val reports = mutableListOf<Pair<String?, Boolean>>()
        var active = true
        var refreshed = 0
        fun failure(id: String?) = CaptureSaveFailureHandler(id, { active },
            { _, retry, captured -> reports += captured to retry }, { refreshed++ }, {})
        retryCapturedSave<Unit>(null, { false }, { _, _, _ -> error("stale retry must not start") }, {}, ::failure)
        assertTrue(reports.isEmpty())
        assertEquals(0, refreshed)
        retryCapturedSave<Unit>("A", { true }, { _, _, _ -> false }, {}, ::failure)
        assertEquals(listOf("A" to false), reports)
        assertEquals(1, refreshed)
        var deferred: ((Throwable) -> Unit)? = null
        retryCapturedSave<Unit>("A", { true }, { _, _, error -> deferred = error; true }, {}, ::failure)
        active = false
        deferred!!(IllegalStateException("late failure"))
        assertEquals(listOf("A" to false, "A" to false), reports)
        assertEquals(1, refreshed)
    }
}
