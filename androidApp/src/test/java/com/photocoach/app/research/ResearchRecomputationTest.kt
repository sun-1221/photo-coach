package com.photocoach.app.research

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ResearchRecomputationTest {
    @Test fun duplicatesFailuresMissingAndLateRowsRemainVisible() {
        fun event(round: Int, type: String, time: Long, capture: String? = null) = ResearchEvent(type, time, time,
            "close_up", round, "ready", sessionId = "session", condition = "DYNAMIC", scene = "WINDOW",
            configurationId = "replay-example-v1", buildVersion = "test", captureId = capture)
        val publication = event(1, "original_published", 1200, "a")
        val rows = listOf(event(1, "round_operable", 0), event(1, "capture_accepted", 500, "a"), publication, publication,
            event(2, "round_operable", 2000), event(2, "round_timeout", 32000),
            event(4, "round_operable", 4000), event(4, "capture_accepted", 4500, "d"), event(4, "original_published", 35000, "d"))
        val results = ResearchRecomputation.replay(rows, (1..4).map { ResearchRoundKey("session", it) }.toSet(), 30000)
        assertEquals(listOf(ResearchRoundOutcome.SUCCESS, ResearchRoundOutcome.FAILED, ResearchRoundOutcome.INCOMPLETE,
            ResearchRoundOutcome.FAILED), results.map { it.outcome })
        assertEquals(1, results.first().duplicateEvents)
        assertEquals(1200L, results.first().captureLatencyMs)
        assertEquals(4, results.size)
    }
}
