package com.photocoach.app.camera

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LiveRecordingSessionTest {
    @Test fun retiredFinalizeCannotClearNewRecording() {
        val session = LiveRecordingSession()
        val old = session.begin()
        session.invalidate()
        val current = session.begin()
        session.started(current, 8_000L)
        assertFalse(session.finish(old))
        assertTrue(session.owns(current))
        assertEquals(8_000L, session.startedElapsedMs)
    }

    @Test fun warmupStartsAtEncoderStartNotPrepareRequest() {
        val session = LiveRecordingSession()
        val token = session.begin()
        assertNull(session.startedElapsedMs)
        session.started(token, 2_000L)
        assertEquals(2_000L, session.startedElapsedMs)
        assertTrue(session.finish(token))
        assertNull(session.startedElapsedMs)
        assertFalse(session.finish(token))
    }

    @Test fun lateStartAfterReleaseCannotReviveRecording() {
        val session = LiveRecordingSession()
        val token = session.begin()
        session.invalidate()
        session.started(token, 1_000L)
        assertFalse(session.owns(token))
        assertNull(session.startedElapsedMs)
    }
}
