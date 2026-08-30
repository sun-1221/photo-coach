package com.photocoach.app.tts

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class TtsReadinessGateTest {
    @Test
    fun cueOfferedDuringInitializationPlaysWhenEngineBecomesReady() {
        val gate = TtsReadinessGate<String>()

        assertEquals(TtsOfferResult.QUEUED, gate.offer("下巴微收"))
        assertEquals("下巴微收", gate.markReady())
        assertEquals(TtsOfferResult.PLAY_NOW, gate.offer("肩放松"))
    }

    @Test
    fun initializationFailureReturnsQueuedCueAndRejectsLaterCues() {
        val gate = TtsReadinessGate<String>()

        gate.offer("下巴微收")
        assertEquals("下巴微收", gate.markFailed())
        assertEquals(TtsOfferResult.UNAVAILABLE, gate.offer("肩放松"))
    }

    @Test
    fun mutingBeforeInitializationCompletesCancelsQueuedCue() {
        val gate = TtsReadinessGate<String>()

        gate.offer("下巴微收")
        assertEquals("下巴微收", gate.cancelPending())
        assertNull(gate.markReady())
    }

    @Test
    fun latestRealtimeCueReplacesStaleCueWhileEngineInitializes() {
        val gate = TtsReadinessGate<String>()

        gate.offer("身体侧一点")
        gate.offer("下巴微收")

        assertEquals("下巴微收", gate.markReady())
    }
}
