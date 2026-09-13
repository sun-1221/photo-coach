package com.photocoach.coach

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class NewRequirementsGuidanceTest {
    @Test fun actualGuidanceRetractsMissingShoulderEvidenceBeforeMinimumDisplayTime() {
        val session = GuidanceSession().apply { onCameraReady(0); onAnalysisSession(1) }
        val engine = CoachEngine.loadDefault()
        val base = Signals(faceCount = 1, faceReliable = true, poseReliable = true, faceRatio = .18f,
            shouldersRaised = true, knownPoseSignals = setOf("shoulders"), analysisSessionId = 1)
        val cue = Cue(CueId.RELAX_SHOULDERS, "肩膀放松一点", Audience.SUBJECT, Channel.POSE, 80)
        for (time in listOf(10L, 100L, 200L)) {
            val signals = base.copy(captureTimestampNs = time * 1_000_000, observedAtMs = time)
            session.onCandidates(engine.evaluate(signals, ShotIntent.CLOSE_UP).copy(cues = listOf(cue)), signals, time)
        }
        assertEquals(cue, session.snapshot().currentCue)
        val missing = base.copy(knownPoseSignals = emptySet(), shouldersRaised = false,
            captureTimestampNs = 210_000_000, observedAtMs = 210)
        session.onCandidates(engine.evaluate(missing, ShotIntent.CLOSE_UP).copy(cues = emptyList()), missing, 210)
        assertTrue(session.snapshot().stage is GuidanceStage.Ready)
        assertNull(session.snapshot().currentCue)
    }
    @Test fun actualGuidanceDoesNotAccumulateRepeatedOrRetiredSessionFrames() {
        val session = GuidanceSession().apply { onCameraReady(0); onAnalysisSession(2) }
        val engine = CoachEngine.loadDefault()
        fun feed(binding: Long, captured: Long, delivered: Long) {
            val signals = Signals(faceCount = 1, faceReliable = true, faceRatio = .02f,
                analysisSessionId = binding, captureTimestampNs = captured * 1_000_000,
                observedAtMs = captured)
            session.onCandidates(engine.evaluate(signals, ShotIntent.CLOSE_UP), signals, delivered)
        }
        feed(2, 10, 10)
        feed(2, 10, 100); feed(2, 10, 200)
        feed(1, 300, 300); feed(1, 400, 400)
        assertTrue(session.snapshot().stage is GuidanceStage.Observing)
        feed(2, 500, 500); feed(2, 600, 600)
        assertEquals(CueId.MOVE_CLOSER, session.snapshot().currentCue?.id)
    }
    @Test fun frameGateRejectsRepeatedOldAndExpiredFramesAndResetsAcrossGaps() {
        val gate = FrameSequenceGate(1500)
        assertTrue(gate.accept(2, 1_000_000, 10, 10))
        assertFalse(gate.accept(2, 1_000_000, 10, 20))
        assertFalse(gate.accept(2, 500_000, 10, 30))
        assertFalse(gate.accept(1, 2_000_000, 30, 30))
        assertTrue(gate.accept(2, 2_000_000_000, 2000, 2000)); assertTrue(gate.restarted)
        gate.activate(3)
        assertFalse(gate.accept(2, 3_000_000_000, 3000, 3000))
        assertFalse(gate.accept(3, 4_000_000_000, 3000, 5000))
    }
    @Test fun staticResearchCardsIgnoreQualityAndHaveSameSkipAndCapturePath() {
        val protocol = ResearchProtocol(ResearchCondition.STATIC, ResearchScene.WINDOW, "test-v1")
        val s = GuidanceSession(staticResearchCues = protocol.staticCues())
        s.onCameraReady(0)
        assertEquals("靠近窗边，让柔光照到脸", s.snapshot().currentCue?.text)
        val before = s.snapshot().currentCue
        val engine = CoachEngine.loadDefault()
        val input = Signals(faceCount = 3, lensObscured = true)
        s.onCandidates(engine.evaluate(input, ShotIntent.CLOSE_UP), input, 700)
        assertEquals(before, s.snapshot().currentCue)
        s.tick(1600); assertEquals(before, s.snapshot().currentCue)
        assertTrue(s.skip(1700)); assertEquals("身体转向窗户", s.snapshot().currentCue?.text)
        assertTrue(s.skip(1800)); assertFalse((s.snapshot().stage as GuidanceStage.Ready).qualityConfirmed)
        assertTrue(s.onShutter())
    }
    @Test fun missingPosePointsAreUnknownNotImprovement() {
        val cue = PoseCueCatalog.cues.first { it.id == "half-shoulders" }
        val start = Signals(faceCount = 1, poseReliable = true, shouldersRaised = true, knownPoseSignals = setOf("shoulders"))
        assertTrue(cue.isEligible(start))
        val missing = start.copy(shouldersRaised = false, knownPoseSignals = emptySet())
        assertFalse(cue.isSatisfied(missing)); assertFalse(cue.hasEvidence(missing))
        assertTrue(cue.isSatisfied(start.copy(shouldersRaised = false)))
    }
    @Test fun sceneryNeverSuggestsTelephotoAndPositionIsExplicitInspiration() {
        val input = Signals(faceCount = 1, faceRatio = .02f)
        assertNull(PhotoTechniqueEngine.suggest(input, TechniqueCapabilities("3x", intent = ShotIntent.PERSON_WITH_SCENERY)))
        assertEquals(TechniqueCategory.FOCAL_DISTANCE, PhotoTechniqueEngine.suggest(input, TechniqueCapabilities("3x"))?.category)
        assertEquals(TechniqueCategory.CAMERA_POSITION, PhotoTechniqueEngine.cameraPositionInspiration().category)
    }
}
