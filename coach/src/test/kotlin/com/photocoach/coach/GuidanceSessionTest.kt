package com.photocoach.coach

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GuidanceSessionTest {
    @Test
    fun knownDarkFaceOrSubjectMotionDoesNotClaimConfirmedReady() {
        val good = Signals(faceCount = 1, faceRatio = 0.18f)
        for ((signals, reason) in listOf(
            good.copy(faceDarkerThanScene = true) to ReadinessIssue.FACE_DARK,
            good.copy(subjectMotionHigh = true) to ReadinessIssue.SUBJECT_MOVING,
        )) {
            val session = readySession()
            session.onCandidates(output(emptyList()), signals, 100)
            session.tick(1_500)
            val stage = session.snapshot().stage as GuidanceStage.Ready
            assertFalse(stage.qualityConfirmed)
            assertEquals(reason, stage.readinessIssue)
            assertTrue(session.snapshot().shutterEnabled)
        }
    }

    @Test
    fun readyQualityRefreshesAfterStableImprovementAndDeterioration() {
        val session = readySession()
        val good = Signals(faceCount = 1, faceRatio = 0.18f)
        session.onCandidates(output(emptyList()), good.copy(handheldStable = false), 100)
        session.tick(1_500)
        val first = assertInstanceOf(GuidanceStage.Ready::class.java, session.snapshot().stage)
        assertFalse(first.qualityConfirmed)
        assertEquals(ReadinessIssue.PHONE_MOVING, first.readinessIssue)
        session.onCandidates(output(emptyList()), good, 1_600)
        assertFalse((session.snapshot().stage as GuidanceStage.Ready).qualityConfirmed)
        session.onCandidates(output(emptyList()), good, 1_900)
        session.onCandidates(output(emptyList()), good, 2_200)
        assertTrue((session.snapshot().stage as GuidanceStage.Ready).qualityConfirmed)
        for (time in listOf(2_500L, 2_800L, 3_100L)) {
            session.onCandidates(output(emptyList()), good.copy(focusOnFace = false), time)
        }
        val last = session.snapshot().stage as GuidanceStage.Ready
        assertFalse(last.qualityConfirmed)
        assertEquals(ReadinessIssue.FOCUS_OFF_FACE, last.readinessIssue)
        assertTrue(session.snapshot().shutterEnabled)
    }

    @Test
    fun exhaustedAdviceStillExplainsUnresolvedQualityWithoutAddingAStep() {
        val session = readySession()
        stabilize(session, output(listOf(moveCloser())), Signals(faceCount = 1, faceRatio = 0.04f))
        session.tick(200 + GuidanceSession.SHOOTER_TIMEOUT_MS)
        val stage = session.snapshot().stage as GuidanceStage.Ready
        assertEquals(ReadinessIssue.SUBJECT_TOO_SMALL, stage.readinessIssue)
        assertFalse(stage.optionalAvailable)
        assertFalse(stage.qualityConfirmed)
        assertTrue(session.snapshot().shutterEnabled)
    }

    @Test
    fun oneGoodFrameDoesNotEraseTheReadinessReason() {
        val session = readySession()
        val bad = Signals(faceCount = 1, faceRatio = 0.18f, handheldStable = false)
        session.onCandidates(output(emptyList()), bad, 100)
        session.tick(1_500)
        session.onCandidates(output(emptyList()), bad.copy(handheldStable = true), 1_600)
        session.onCandidates(output(emptyList()), bad, 1_900)
        assertEquals(ReadinessIssue.PHONE_MOVING, (session.snapshot().stage as GuidanceStage.Ready).readinessIssue)
    }

    @Test
    fun threeStableFramesStartOneOfTwoAndSkippingNeverAddsRequiredSteps() {
        val session = readySession()
        val output = output(listOf(moveCloser(priority = 100), focusFace(priority = 80), subjectCue()))

        session.onCandidates(output, Signals(faceCount = 1, faceRatio = 0.04f), 10)
        session.onCandidates(output, Signals(faceCount = 1, faceRatio = 0.04f), 100)
        assertInstanceOf(GuidanceStage.Observing::class.java, session.snapshot().stage)
        session.onCandidates(output, Signals(faceCount = 1, faceRatio = 0.04f), 200)

        assertEquals("1/2", session.snapshot().stepLabel)
        assertEquals(CueId.MOVE_CLOSER, session.snapshot().currentCue?.id)
        assertTrue(session.skip(250))
        assertEquals("2/2", session.snapshot().stepLabel)
        assertTrue(session.skip(300))
        assertInstanceOf(GuidanceStage.Ready::class.java, session.snapshot().stage)
        assertTrue(session.snapshot().canRequestOptional)

        assertTrue(session.requestOptional(350))
        assertInstanceOf(GuidanceStage.Optional::class.java, session.snapshot().stage)
        session.tick(2_850)
        assertInstanceOf(GuidanceStage.Ready::class.java, session.snapshot().stage)
        assertFalse(session.requestOptional(2_851))
    }

    @Test
    fun observationAndShooterTimeoutsNeverBecomeInfiniteThinking() {
        val noCue = readySession()
        noCue.onCandidates(
            output(emptyList()),
            Signals(faceCount = 1, faceRatio = CueSelector.CLOSE_UP_MIN_FACE_RATIO),
            100,
        )
        noCue.tick(GuidanceSession.OBSERVATION_TIMEOUT_MS)
        val ready = assertInstanceOf(GuidanceStage.Ready::class.java, noCue.snapshot().stage)
        assertTrue(ready.qualityConfirmed)

        val action = readySession()
        stabilize(action, output(listOf(moveCloser())), Signals(faceCount = 1, faceRatio = 0.04f))
        action.tick(200 + GuidanceSession.SHOOTER_TIMEOUT_MS)
        assertInstanceOf(GuidanceStage.Ready::class.java, action.snapshot().stage)
    }

    @Test
    fun observationTimeoutWithNoReliablePersonShowsRecoveryInsteadOfFalseReady() {
        val session = readySession()
        session.onCandidates(output(emptyList()), Signals(), 100)

        session.tick(GuidanceSession.OBSERVATION_TIMEOUT_MS)

        assertInstanceOf(GuidanceStage.Action::class.java, session.snapshot().stage)
        assertEquals(CueId.FIND_PERSON, session.snapshot().currentCue?.id)
        assertTrue(session.snapshot().shutterEnabled)
    }

    @Test
    fun userCanSkipPersonRecoveryWithoutLockingShutterOrClaimingQuality() {
        val session = readySession()
        val recovery = Cue(
            CueId.FIND_PERSON,
            "请露出脸，或靠近一点",
            Audience.SHOOTER,
            Channel.COMPOSITION,
            priority = 118,
            critical = true,
        )
        stabilize(session, output(listOf(recovery)), Signals())

        assertTrue(session.skip(300))

        val ready = assertInstanceOf(GuidanceStage.Ready::class.java, session.snapshot().stage)
        assertFalse(ready.qualityConfirmed)
        assertTrue(session.snapshot().shutterEnabled)
    }

    @Test
    fun unresolvedQualityCanEndAdviceBudgetWithoutClaimingConfirmedReady() {
        val session = readySession()
        stabilize(session, output(listOf(moveCloser())), Signals(faceCount = 1, faceRatio = 0.04f))

        session.tick(200 + GuidanceSession.SHOOTER_TIMEOUT_MS)

        val ready = assertInstanceOf(GuidanceStage.Ready::class.java, session.snapshot().stage)
        assertFalse(ready.qualityConfirmed)
        assertTrue(session.snapshot().shutterEnabled)
    }

    @Test
    fun machineReadableImprovementNeedsFiveHundredMillisecondsAndMinimumDisplay() {
        val session = readySession()
        val output = output(listOf(moveCloser()))
        stabilize(session, output, Signals(faceCount = 1, faceRatio = 0.04f))

        session.onCandidates(output, Signals(faceCount = 1, faceRatio = 0.07f), 300)
        session.tick(799)
        assertInstanceOf(GuidanceStage.Action::class.java, session.snapshot().stage)
        session.tick(1_700)
        assertInstanceOf(GuidanceStage.Ready::class.java, session.snapshot().stage)
    }

    @Test
    fun upperThirdCueCompletesWhenTheFaceMovesOutOfTheLowRegion() {
        val session = readySession()
        val cue = Cue(
            CueId.PLACE_FACE_ON_UPPER_THIRD,
            "把脸放到上方三分线",
            Audience.SHOOTER,
            Channel.COMPOSITION,
        )
        val output = output(listOf(cue))
        stabilize(session, output, Signals(faceCount = 1, faceTooLowInFrame = true))

        session.onCandidates(output, Signals(faceCount = 1, faceTooLowInFrame = false), 300)
        session.tick(799)
        assertInstanceOf(GuidanceStage.Action::class.java, session.snapshot().stage)
        session.tick(1_700)

        assertInstanceOf(GuidanceStage.Ready::class.java, session.snapshot().stage)
    }

    @Test
    fun shutterWorksDuringObservationAndBothGuidanceSteps() {
        val observing = readySession()
        assertTrue(observing.snapshot().shutterEnabled)
        assertTrue(observing.onShutter())
        assertInstanceOf(GuidanceStage.Capturing::class.java, observing.snapshot().stage)

        val first = readySession()
        stabilize(first, output(listOf(moveCloser(), subjectCue())), Signals(faceCount = 1, faceRatio = 0.04f))
        assertTrue(first.onShutter())

        val second = readySession()
        stabilize(second, output(listOf(moveCloser(), subjectCue())), Signals(faceCount = 1, faceRatio = 0.04f))
        second.skip(250)
        assertEquals("2/2", second.snapshot().stepLabel)
        assertTrue(second.onShutter())
    }

    @Test
    fun mutedSubjectFallsBackToReadyWithoutWaitingForPoseMatch() {
        val session = readySession()
        stabilize(session, output(listOf(moveCloser(), subjectCue())), Signals(faceCount = 1, faceRatio = 0.04f))
        session.skip(250)
        session.onPlaybackUnavailable()
        session.tick(2_749)
        assertInstanceOf(GuidanceStage.Action::class.java, session.snapshot().stage)
        session.tick(2_750)
        assertInstanceOf(GuidanceStage.Ready::class.java, session.snapshot().stage)
    }

    @Test
    fun speechAndOppositeDirectionAreRateLimited() {
        val session = readySession()
        val retreat = Cue(
            CueId.KEEP_SUBJECT_IN_FRAME,
            "退半步，人物留全",
            Audience.SHOOTER,
            Channel.LIGHT,
            priority = 80,
            directionGroup = "distance",
            direction = -1,
        )
        stabilize(
            session,
            output(listOf(moveCloser(priority = 100), retreat, subjectCue())),
            Signals(faceCount = 1, faceRatio = 0.04f),
        )
        session.skip(250)
        assertEquals(subjectCue().text, session.takeCueForSpeech(300)?.text)
        assertEquals(null, session.takeCueForSpeech(301))
        assertEquals(subjectCue().text, session.takeCueForSpeech(8_300)?.text)
        session.skip(350)
        assertFalse(session.requestOptional(4_999))
        assertTrue(session.requestOptional(5_200))
    }

    @Test
    fun optionalAvailabilityDoesNotFlickerOnTransientCandidateLoss() {
        val session = readySession()
        val retreat = Cue(
            CueId.KEEP_SUBJECT_IN_FRAME,
            "退半步，人物留全",
            Audience.SHOOTER,
            Channel.COMPOSITION,
            priority = 80,
        )
        val initial = output(listOf(moveCloser(priority = 100), retreat, subjectCue()))
        stabilize(session, initial, Signals(faceCount = 1, faceRatio = 0.04f))
        session.skip(250)
        session.skip(350)
        assertTrue(session.snapshot().canRequestOptional)

        session.onCandidates(output(emptyList()), Signals(faceCount = 1, poseAvailable = true), 400)
        assertTrue(session.snapshot().canRequestOptional)
        session.onCandidates(output(emptyList()), Signals(faceCount = 1, poseAvailable = true), 500)
        assertTrue(session.snapshot().canRequestOptional)
        session.onCandidates(initial, Signals(faceCount = 1, poseAvailable = true), 600)
        assertTrue(session.snapshot().canRequestOptional)

        session.onCandidates(output(emptyList()), Signals(faceCount = 1, poseAvailable = true), 700)
        session.onCandidates(output(emptyList()), Signals(faceCount = 1, poseAvailable = true), 800)
        session.onCandidates(output(emptyList()), Signals(faceCount = 1, poseAvailable = true), 900)
        assertFalse(session.snapshot().canRequestOptional)
    }

    @Test
    fun intentLockAndSaveResultsResetPerPhoto() {
        val session = readySession()
        session.selectIntent(ShotIntent.PERSON_WITH_SCENERY, 10)
        assertFalse(session.autoSelectIntent(ShotIntent.CLOSE_UP))
        assertEquals(ShotIntent.PERSON_WITH_SCENERY, session.snapshot().intent)
        assertTrue(session.onShutter())
        session.onSaveFailed("写入失败", retryAvailable = true)
        assertInstanceOf(GuidanceStage.SaveFailed::class.java, session.snapshot().stage)
        assertTrue(session.onRetrySave())
        session.onSaved("content://photo/1", 100)
        assertInstanceOf(GuidanceStage.Saved::class.java, session.snapshot().stage)
        session.tick(100 + GuidanceSession.SAVE_SUCCESS_DURATION_MS)
        assertInstanceOf(GuidanceStage.Observing::class.java, session.snapshot().stage)
        assertFalse(session.snapshot().intentLocked)
        assertFalse(session.snapshot().optionalUsed)
        assertTrue(session.autoSelectIntent(ShotIntent.CLOSE_UP))
    }

    @Test
    fun subjectCueUsesStableRealtimeReplacementInsteadOfFreezing() {
        val session = readySession()
        stabilize(
            session,
            output(listOf(moveCloser(), subjectCue())),
            Signals(faceCount = 1, faceRatio = 0.04f),
        )
        session.skip(250)

        val changed = output(listOf(angleBodyCue()))
        session.onCandidates(changed, Signals(faceCount = 1, poseAvailable = true), 400)
        session.onCandidates(changed, Signals(faceCount = 1, poseAvailable = true), 700)
        session.onCandidates(changed, Signals(faceCount = 1, poseAvailable = true), 1_000)
        assertEquals(CueId.CHIN_DOWN, session.snapshot().currentCue?.id)

        session.onCandidates(changed, Signals(faceCount = 1, poseAvailable = true), 1_800)
        assertEquals(CueId.ANGLE_BODY, session.snapshot().currentCue?.id)
        assertEquals("2/2", session.snapshot().stepLabel)
    }

    @Test
    fun ordinaryShooterCueUsesStableRealtimeReplacementInTheSameStep() {
        val session = readySession()
        stabilize(
            session,
            output(listOf(moveCloser(), subjectCue())),
            Signals(faceCount = 1, faceRatio = 0.04f),
        )

        val changed = output(listOf(focusFace(), subjectCue()))
        session.onCandidates(changed, Signals(faceCount = 1, focusOnFace = false), 400)
        session.onCandidates(changed, Signals(faceCount = 1, focusOnFace = false), 700)
        session.onCandidates(changed, Signals(faceCount = 1, focusOnFace = false), 1_000)
        assertEquals(CueId.MOVE_CLOSER, session.snapshot().currentCue?.id)

        session.onCandidates(changed, Signals(faceCount = 1, focusOnFace = false), 1_800)
        assertEquals(CueId.FOCUS_FACE, session.snapshot().currentCue?.id)
        assertEquals("1/2", session.snapshot().stepLabel)
    }

    @Test
    fun resolvedSubjectCueClearsAfterStableSignalChange() {
        val session = readySession()
        stabilize(
            session,
            output(listOf(moveCloser(), subjectCue())),
            Signals(faceCount = 1, faceRatio = 0.04f),
        )
        session.skip(250)

        val noSubjectCue = output(emptyList())
        session.onCandidates(noSubjectCue, Signals(faceCount = 1), 400)
        session.onCandidates(noSubjectCue, Signals(faceCount = 1), 700)
        session.onCandidates(noSubjectCue, Signals(faceCount = 1), 1_000)
        session.onCandidates(noSubjectCue, Signals(faceCount = 1), 1_800)

        assertInstanceOf(GuidanceStage.Ready::class.java, session.snapshot().stage)
        assertEquals(null, session.snapshot().currentCue)
    }

    @Test
    fun completedSpeechRetainsValidCaptionAndLatestPoseBecomesOptional() {
        val session = readySession()
        stabilize(
            session,
            output(listOf(moveCloser(), subjectCue())),
            Signals(faceCount = 1, faceRatio = 0.04f),
        )
        session.skip(250)
        session.onPlaybackFinished(300)
        session.tick(1_300)

        assertInstanceOf(GuidanceStage.Ready::class.java, session.snapshot().stage)
        assertEquals(CueId.CHIN_DOWN, session.snapshot().currentCue?.id)

        val changed = output(listOf(angleBodyCue()))
        session.onCandidates(changed, Signals(faceCount = 1, poseAvailable = true), 1_400)
        session.onCandidates(changed, Signals(faceCount = 1, poseAvailable = true), 1_700)
        session.onCandidates(changed, Signals(faceCount = 1, poseAvailable = true), 2_000)

        assertEquals(null, session.snapshot().currentCue)
        assertTrue(session.snapshot().canRequestOptional)
        assertTrue(session.requestOptional(2_100))
        assertEquals(CueId.ANGLE_BODY, session.snapshot().currentCue?.id)
    }

    @Test
    fun disablingBothSubjectChannelsSkipsEmptyWaitImmediately() {
        val session = readySession()
        stabilize(
            session,
            output(listOf(moveCloser(), subjectCue())),
            Signals(faceCount = 1, faceRatio = 0.04f),
        )
        session.skip(250)

        session.onSubjectChannelsUnavailable()

        assertInstanceOf(GuidanceStage.Ready::class.java, session.snapshot().stage)
        assertEquals(null, session.snapshot().currentCue)
    }

    @Test
    fun subjectOnlyCandidateStartsFirstVisibleAndSpeakableStep() {
        val session = readySession()

        stabilize(
            session,
            output(listOf(subjectCue())),
            Signals(faceCount = 1, poseAvailable = true, headTiltedBack = true),
        )

        assertInstanceOf(GuidanceStage.Action::class.java, session.snapshot().stage)
        assertEquals("1/2", session.snapshot().stepLabel)
        assertEquals(CueId.CHIN_DOWN, session.snapshot().currentCue?.id)
        assertEquals(subjectCue().text, session.takeCueForSpeech(300)?.text)
    }

    @Test
    fun stablePoseThatAppearsAfterObservationTimeoutBecomesOptionalWithoutAddingRequiredStep() {
        val session = readySession()
        session.onCandidates(
            output(emptyList()),
            Signals(faceCount = 1, faceRatio = CueSelector.CLOSE_UP_MIN_FACE_RATIO),
            100,
        )
        session.tick(GuidanceSession.OBSERVATION_TIMEOUT_MS)
        assertInstanceOf(GuidanceStage.Ready::class.java, session.snapshot().stage)

        stabilizeAfterReady(session, output(listOf(angleBodyCue())))

        assertInstanceOf(GuidanceStage.Ready::class.java, session.snapshot().stage)
        assertTrue(session.snapshot().canRequestOptional)
        assertTrue(session.requestOptional(2_100))
        assertEquals(CueId.ANGLE_BODY, session.snapshot().currentCue?.id)
        assertEquals(angleBodyCue().text, session.takeCueForSpeech(2_100)?.text)
    }

    @Test
    fun latePoseAfterShooterStepStaysOptional() {
        val session = readySession()
        stabilize(session, output(listOf(moveCloser())), Signals(faceCount = 1, faceRatio = 0.04f))
        session.skip(250)
        assertInstanceOf(GuidanceStage.Ready::class.java, session.snapshot().stage)

        stabilizeAfterReady(session, output(listOf(subjectCue())))

        assertInstanceOf(GuidanceStage.Ready::class.java, session.snapshot().stage)
        assertTrue(session.snapshot().canRequestOptional)
        assertTrue(session.requestOptional(2_100))
        assertEquals(CueId.CHIN_DOWN, session.snapshot().currentCue?.id)
    }

    @Test
    fun currentShooterCueIsSpeakableAndRateLimited() {
        val session = readySession()
        stabilize(session, output(listOf(moveCloser())), Signals(faceCount = 1, faceRatio = 0.04f))

        assertEquals("走近一步", session.takeCueForSpeech(300)?.text)
        assertEquals(null, session.takeCueForSpeech(301))
    }

    @Test
    fun criticalDetectionRecoveryCanReplaceReadyButOrdinaryPoseCannot() {
        val session = readySession()
        session.tick(GuidanceSession.OBSERVATION_TIMEOUT_MS)
        val recovery = Cue(
            CueId.FIND_PERSON,
            "请露出脸，或靠近一点",
            Audience.SHOOTER,
            Channel.COMPOSITION,
            priority = 118,
            critical = true,
        )

        stabilizeAfterReady(session, output(listOf(recovery)), Signals())

        assertInstanceOf(GuidanceStage.Action::class.java, session.snapshot().stage)
        assertEquals(CueId.FIND_PERSON, session.snapshot().currentCue?.id)
    }

    @Test
    fun missingPersonRecoveryDoesNotTimeoutToFalseReady() {
        val session = readySession()
        val recovery = Cue(
            CueId.FIND_PERSON,
            "请露出脸，或靠近一点",
            Audience.SHOOTER,
            Channel.COMPOSITION,
            priority = 118,
            critical = true,
        )
        stabilize(session, output(listOf(recovery)), Signals())

        session.tick(200 + GuidanceSession.SHOOTER_TIMEOUT_MS + 1)

        assertInstanceOf(GuidanceStage.Action::class.java, session.snapshot().stage)
        assertEquals(CueId.FIND_PERSON, session.snapshot().currentCue?.id)
    }

    @Test
    fun missingPersonRecoveryCanReappearAfterItWasResolvedAndReady() {
        val session = readySession()
        val recovery = Cue(
            CueId.FIND_PERSON,
            "请露出脸，或靠近一点",
            Audience.SHOOTER,
            Channel.COMPOSITION,
            priority = 118,
            critical = true,
        )
        stabilize(session, output(listOf(recovery)), Signals())
        session.onCandidates(
            output(emptyList()),
            Signals(faceCount = 1, poseAvailable = true),
            1_800,
        )
        session.onCandidates(
            output(emptyList()),
            Signals(faceCount = 1, poseAvailable = true),
            2_100,
        )
        session.onCandidates(
            output(emptyList()),
            Signals(faceCount = 1, poseAvailable = true),
            2_400,
        )
        session.tick(3_000)
        assertInstanceOf(GuidanceStage.Ready::class.java, session.snapshot().stage)

        session.onCandidates(output(listOf(recovery)), Signals(), 3_100)
        session.onCandidates(output(listOf(recovery)), Signals(), 3_400)
        session.onCandidates(output(listOf(recovery)), Signals(), 3_700)

        assertInstanceOf(GuidanceStage.Action::class.java, session.snapshot().stage)
        assertEquals(CueId.FIND_PERSON, session.snapshot().currentCue?.id)
    }

    private fun readySession(): GuidanceSession = GuidanceSession().apply { onCameraReady(0) }

    private fun stabilize(session: GuidanceSession, output: CoachOutput, signals: Signals) {
        session.onCandidates(output, signals, 10)
        session.onCandidates(output, signals, 100)
        session.onCandidates(output, signals, 200)
    }

    private fun stabilizeAfterReady(
        session: GuidanceSession,
        output: CoachOutput,
        signals: Signals = Signals(faceCount = 1, poseAvailable = true),
    ) {
        session.onCandidates(output, signals, 1_600)
        session.onCandidates(output, signals, 1_800)
        session.onCandidates(output, signals, 2_000)
    }

    private fun output(cues: List<Cue>): CoachOutput = CoachOutput(
        intent = ShotIntent.CLOSE_UP,
        sceneId = null,
        cues = cues,
        startParams = SceneStartParams(SuggestedMode.PHOTO, false, FocusTarget.FACE),
        overlay = OverlayHint(showSilhouette = true),
    )

    private fun moveCloser(priority: Int = 90): Cue = Cue(
        CueId.MOVE_CLOSER,
        "走近一步",
        Audience.SHOOTER,
        Channel.COMPOSITION,
        priority = priority,
        directionGroup = "distance",
        direction = 1,
    )

    private fun focusFace(priority: Int = 90): Cue = Cue(
        CueId.FOCUS_FACE,
        "点一下脸",
        Audience.SHOOTER,
        Channel.LIGHT,
        priority = priority,
    )

    private fun subjectCue(): Cue = Cue(
        CueId.CHIN_DOWN,
        "下巴微收，头略往前",
        Audience.SUBJECT,
        Channel.POSE,
        priority = 80,
    )

    private fun angleBodyCue(): Cue = Cue(
        CueId.ANGLE_BODY,
        "身体侧一点，脸转回镜头",
        Audience.SUBJECT,
        Channel.POSE,
        priority = 78,
    )
}
