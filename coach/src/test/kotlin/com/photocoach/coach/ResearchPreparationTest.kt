package com.photocoach.coach

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ResearchPreparationTest {
    @Test fun slowPreparationStartsStaticAndDynamicTimersOnlyAtAcknowledgement() {
        for (condition in listOf(ResearchCondition.STATIC, ResearchCondition.DYNAMIC)) {
            val protocol = ResearchProtocol(condition, ResearchScene.WINDOW, "slow-test")
            val session = GuidanceSession(staticResearchCues = protocol.staticCues(), researchPreparationRequired = true)
            session.onCameraReady(0)
            val initial = session.snapshot().stage
            session.tick(10_000)
            assertEquals(initial, session.snapshot().stage)
            assertFalse(session.onShutter(10_000)); assertNull(session.takeCueForSpeech(10_000))
            session.completeResearchPreparation(12_000)
            val stage = session.snapshot().stage
            when (stage) {
                is GuidanceStage.Action -> assertEquals(12_000L, stage.shownAtMs)
                is GuidanceStage.Observing -> assertEquals(12_000L, stage.sinceMs)
                else -> fail<Unit>("unexpected stage")
            }
            session.tick(12_100)
            assertEquals(stage, session.snapshot().stage)
            assertTrue(session.snapshot().shutterEnabled)
        }
    }

    @Test fun fastSavedShutterCannotCaptureBeforeNextRoundPreparation() {
        for (delay in listOf(1L, 100L, 1499L)) {
            val session = GuidanceSession(researchPreparationRequired = true)
            session.onCameraReady(0); session.completeResearchPreparation(100)
            assertTrue(session.onShutter(200)); session.onSaved("original", 300)
            val round = session.snapshot().roundId
            assertFalse(session.onShutter(300 + delay))
            assertEquals(round + 1, session.snapshot().roundId)
            assertFalse(session.onShutter(5000)); assertFalse(session.snapshot().shutterEnabled)
            session.completeResearchPreparation(10_000)
            assertTrue(session.onShutter(10_001))
        }
    }

    @Test fun disappearingEyeClassificationOrBodyEvidenceImmediatelyWithdrawsRequiredCue() {
        for (id in listOf(CueId.OPEN_EYES, CueId.TURN_TO_WINDOW)) {
            val session = GuidanceSession().apply { onCameraReady(0); onAnalysisSession(1) }
            val engine = CoachEngine.loadDefault()
            val cue = Cue(id, "required", Audience.SUBJECT, Channel.POSE, 80)
            val base = Signals(faceCount = 1, faceReliable = true, poseReliable = true, faceRatio = .18f,
                eyesLikelyClosed = true, knownFaceSignals = setOf("eyes"), knownPoseSignals = setOf("body-angle"), analysisSessionId = 1)
            for (time in listOf(10L, 100L, 200L)) {
                val signals = base.copy(captureTimestampNs = time * 1_000_000, observedAtMs = time)
                session.onCandidates(engine.evaluate(signals, ShotIntent.CLOSE_UP).copy(cues = listOf(cue)), signals, time)
            }
            assertEquals(cue, session.snapshot().currentCue)
            val lost = base.copy(knownFaceSignals = emptySet(), knownPoseSignals = emptySet(), eyesLikelyClosed = false,
                captureTimestampNs = 210_000_000, observedAtMs = 210)
            session.onCandidates(engine.evaluate(lost, ShotIntent.CLOSE_UP).copy(cues = listOf(cue)), lost, 210)
            assertNotEquals(cue, session.snapshot().currentCue)
        }
    }
}
