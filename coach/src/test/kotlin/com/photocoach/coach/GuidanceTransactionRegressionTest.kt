package com.photocoach.coach

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class GuidanceTransactionRegressionTest {
    @Test fun panelDwellCannotCompletePrePauseImprovementAndPlaybackCallbacksAreIgnored() {
        val cue = Cue(CueId.LEVEL_PHONE, "放平手机", Audience.SHOOTER, Channel.COMPOSITION)
        val candidates = output.copy(cues = listOf(cue))
        val session = GuidanceSession().apply { onCameraReady(0) }
        listOf(10L, 100L, 200L).forEach { session.onCandidates(candidates, known.copy(tiltDegrees = 10f), it) }
        session.onCandidates(candidates, known, 1800)
        session.setOutputPaused(true, 1900)
        val before = session.snapshot()
        session.onPlaybackFinished(5000); session.onPlaybackUnavailable(); session.onSubjectChannelsUnavailable()
        session.tick(9000)
        assertEquals(before, session.snapshot())
        listOf(9400L, 9700L, 10000L).forEach { session.onCandidates(candidates, known, it) }
        session.setOutputPaused(false, 10100)
        session.tick(10200)
        assertTrue(session.snapshot().stage is GuidanceStage.Action)
        session.tick(10600)
        assertTrue(session.snapshot().stage is GuidanceStage.Ready)
    }

    @Test fun optionalCandidateChangesCannotAdvanceWhilePausedAndExpiryResumesRecovery() {
        val session = GuidanceSession().apply { onCameraReady(0) }
        session.onCandidates(output, known, 1000); session.offerOptionalPose("肩放松一点", "first")
        session.tick(1500); assertTrue(session.requestOptional(1550))
        session.setOutputPaused(true, 1600)
        val before = session.snapshot()
        session.offerOptionalPose("身体侧一点", "second")
        session.onPlaybackFinished(1700); session.tick(9000)
        assertEquals(before, session.snapshot())
        assertFalse(session.skip(9000)); assertFalse(session.requestOptional(9000))
        session.setOutputPaused(false, 9100)
        assertEquals(CueId.FIND_PERSON, session.snapshot().currentCue?.id)
        assertTrue(session.snapshot().optionalUsed)
    }

    @Test fun closingPanelRestoresLatestStableReplacementWithoutRestartingBudget() {
        val old = Cue(CueId.LEVEL_PHONE, "放平手机", Audience.SHOOTER, Channel.COMPOSITION)
        val next = Cue(CueId.FOCUS_FACE, "点一下脸", Audience.SHOOTER, Channel.LIGHT)
        val session = GuidanceSession().apply { onCameraReady(0) }
        listOf(10L, 100L, 200L).forEach { session.onCandidates(output.copy(cues = listOf(old)), known, it) }
        session.setOutputPaused(true, 250)
        listOf(900L, 1200L, 1500L).forEach { session.onCandidates(output.copy(cues = listOf(next)), known.copy(focusOnFace = false), it) }
        assertEquals(old, session.snapshot().currentCue)
        session.setOutputPaused(false, 1600)
        assertEquals(next, session.snapshot().currentCue)
        assertEquals("1/2", session.snapshot().stepLabel)
    }
    @Test fun panelPausesSpeechAndBudgetAndResumesLatestStableCueWithoutReplay() {
        val session = GuidanceSession().apply { onCameraReady(0) }
        val cue = Cue(CueId.LEVEL_PHONE, "放平手机", Audience.SHOOTER, Channel.COMPOSITION)
        val candidates = output.copy(cues = listOf(cue))
        listOf(10L, 100L, 200L).forEach { session.onCandidates(candidates, known.copy(tiltDegrees = 10f), it) }
        val round = session.snapshot().roundId
        assertEquals(cue, session.takeCueForSpeech(210))
        session.setOutputPaused(true, 250)
        session.tick(20_000)
        assertTrue(session.snapshot().stage is GuidanceStage.Action)
        assertNull(session.takeCueForSpeech(20_000))
        session.setOutputPaused(false, 20_100)
        assertEquals(round, session.snapshot().roundId)
        assertNull(session.takeCueForSpeech(20_200))
    }

    @Test fun closingChannelsUsesReliablePoseAndMultiPersonSafetyRules() {
        val subject = Cue(CueId.CHIN_DOWN, "下巴微收", Audience.SUBJECT, Channel.POSE)
        for (signal in listOf(Signals(poseReliable = true), Signals(faceCount = 2))) {
            val session = GuidanceSession().apply { onCameraReady(0) }
            listOf(10L, 100L, 200L).forEach { session.onCandidates(output.copy(cues = listOf(subject)), signal, it) }
            session.onSubjectChannelsUnavailable()
            assertFalse((session.snapshot().stage as GuidanceStage.Ready).qualityConfirmed)
            assertTrue(session.snapshot().shutterEnabled)
        }
    }
    private val output = CoachOutput(ShotIntent.CLOSE_UP, null, emptyList(),
        SceneStartParams(SuggestedMode.PHOTO, false, FocusTarget.FACE), OverlayHint(false))
    private val known = Signals(faceCount = 1, faceReliable = true, faceRatio = .18f,
        qualityEvidenceComplete = true)

    @Test fun intentChangesCannotEraseCaptureOrRetryTransaction() {
        val session = GuidanceSession().apply { onCameraReady(0) }
        assertTrue(session.onShutter(10))
        session.selectIntent(ShotIntent.PERSON_WITH_SCENERY, 20)
        assertEquals(GuidanceStage.Capturing, session.snapshot().stage)
        session.onSaveFailed("disk full", true)
        session.selectIntent(ShotIntent.PERSON_WITH_SCENERY, 30)
        assertTrue(session.snapshot().stage is GuidanceStage.SaveFailed)
        session.onCameraUnavailable()
        assertTrue(session.onRetrySave())
    }

    @Test fun readyExpiresWithoutAnotherAnalysisCallback() {
        val session = GuidanceSession().apply { onCameraReady(0) }
        session.onCandidates(output, known, 1000)
        session.tick(1500)
        assertTrue((session.snapshot().stage as GuidanceStage.Ready).qualityConfirmed)
        session.tick(2501)
        assertFalse(session.snapshot().stage is GuidanceStage.Ready)
        assertTrue(session.snapshot().shutterEnabled)
    }

    @Test fun unknownEvidenceAndPoseOnlyNeverConfirmQuality() {
        for (signal in listOf(known.copy(qualityEvidenceComplete = false),
            Signals(poseReliable = true))) {
            val session = GuidanceSession().apply { onCameraReady(0) }
            session.onCandidates(output, signal, 100)
            session.tick(1500)
            assertFalse((session.snapshot().stage as GuidanceStage.Ready).qualityConfirmed)
            assertTrue(session.snapshot().shutterEnabled)
        }
    }

    @Test fun optionalPoseRequiresExplicitActionAndConsumesSingleBudget() {
        val session = GuidanceSession().apply { onCameraReady(0) }
        session.onCandidates(output, known, 1000)
        session.offerOptionalPose("肩放松一点")
        session.tick(1500)
        assertNull(session.takeCueForSpeech(1500))
        assertTrue(session.requestOptional(1600))
        assertEquals("肩放松一点", session.takeCueForSpeech(1600)?.text)
        session.skip(1700)
        session.offerOptionalPose("身体侧一点")
        assertFalse(session.requestOptional(1800))
    }
}
