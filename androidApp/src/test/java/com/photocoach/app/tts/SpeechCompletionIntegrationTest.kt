package com.photocoach.app.tts

import com.photocoach.coach.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SpeechCompletionIntegrationTest {
    @Test fun lateDoneErrorAndStopCannotAdvanceReplacementGuidance() {
        for (failed in listOf(false, true)) {
            val protocol = ResearchProtocol(ResearchCondition.STATIC, ResearchScene.WINDOW, "test")
            val session = GuidanceSession(staticResearchCues = protocol.staticCues()).apply { onCameraReady(0) }
            val gate = SpeechCompletionGate()
            fun listen(id: String) = gate.begin(id) { error ->
                if (error) session.onPlaybackUnavailable() else session.onPlaybackFinished(2000)
            }
            listen("old")
            gate.cancel() // stop before the engine has emitted onStart
            session.skip(100)
            listen("new")
            session.onPlaybackStarting()
            val before = session.snapshot().stage
            gate.complete("old", failed)
            gate.complete("old", !failed)
            assertEquals(before, session.snapshot().stage)
            gate.complete("new", failed)
            assertNotEquals(before, session.snapshot().stage)
            val completed = session.snapshot().stage
            gate.complete("new", !failed)
            assertEquals(completed, session.snapshot().stage)
        }
    }
}
