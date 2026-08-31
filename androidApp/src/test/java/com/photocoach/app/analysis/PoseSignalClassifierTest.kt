package com.photocoach.app.analysis

import com.photocoach.coach.Channel
import com.photocoach.coach.CoachEngine
import com.photocoach.coach.CueId
import com.photocoach.coach.ShotIntent
import com.photocoach.coach.Signals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PoseSignalClassifierTest {
    @Test
    fun frontFacingTorsoTriggersSquareShouldersButTurningBodyClearsIt() {
        val front = baseInput(
            leftShoulder = point(-50f, 0f),
            rightShoulder = point(50f, 0f),
        )
        val turned = baseInput(
            leftShoulder = point(-20f, 0f),
            rightShoulder = point(20f, 4f),
        )

        assertTrue(PoseSignalClassifier.classify(front).shouldersSquare)
        assertFalse(PoseSignalClassifier.classify(turned).shouldersSquare)
    }

    @Test
    fun followingBodyTurnRemovesAngleCueFromTheEngineInput() {
        val engine = CoachEngine.loadDefault()
        val front = PoseSignalClassifier.classify(baseInput())
        val turned = PoseSignalClassifier.classify(
            baseInput(
                leftShoulder = point(-20f, 0f),
                rightShoulder = point(20f, 4f),
            ),
        )

        val frontOutput = engine.evaluate(front.toSignals(), ShotIntent.CLOSE_UP)
        val turnedOutput = engine.evaluate(turned.toSignals(), ShotIntent.CLOSE_UP)

        assertEquals(CueId.ANGLE_BODY, frontOutput.cues.first { it.channel == Channel.POSE }.id)
        assertFalse(turnedOutput.cues.any { it.channel == Channel.POSE })
    }

    @Test
    fun halfBodyKeepsUpperBodySignalsButDoesNotGuessLowerBodySignals() {
        val missingHip = baseInput().copy(rightHip = null)
        val lowConfidenceShoulder = baseInput().copy(rightShoulder = point(50f, 0f, likelihood = 0.2f))

        assertTrue(PoseSignalClassifier.classify(missingHip).shouldersSquare)
        assertFalse(PoseSignalClassifier.classify(lowConfidenceShoulder).shouldersSquare)
        assertFalse(PoseSignalClassifier.classify(missingHip).handsNeedPlacement)
    }

    @Test
    fun halfBodyCanStillDetectRaisedShoulders() {
        val halfBody = baseInput(
            leftEar = point(-50f, -20f),
            rightEar = point(50f, -20f),
        ).copy(leftHip = null, rightHip = null)

        assertTrue(PoseSignalClassifier.classify(halfBody).shouldersRaised)
    }

    @Test
    fun halfBodyShoulderDepthMakesBodyTurnChangeTheCue() {
        val front = baseInput().copy(leftHip = null, rightHip = null)
        val turned = front.copy(
            leftShoulder = point(-45f, 0f, z = -35f),
            rightShoulder = point(45f, 0f, z = 35f),
        )

        assertTrue(PoseSignalClassifier.classify(front).shouldersSquare)
        assertFalse(PoseSignalClassifier.classify(turned).shouldersSquare)
    }

    @Test
    fun raisedShoulderAndHandsBlockingTorsoRequireNormalizedEvidence() {
        val needsCorrection = baseInput(
            leftEar = point(-50f, -35f),
            rightEar = point(50f, -35f),
            leftWrist = point(-20f, 90f),
            rightWrist = point(20f, 90f),
        )
        val relaxed = needsCorrection.copy(
            leftEar = point(-50f, -70f),
            rightEar = point(50f, -70f),
            leftWrist = point(-70f, 160f),
            rightWrist = point(70f, 160f),
        )

        val correctionSignals = PoseSignalClassifier.classify(needsCorrection)
        val relaxedSignals = PoseSignalClassifier.classify(relaxed)
        assertTrue(correctionSignals.shouldersRaised)
        assertTrue(correctionSignals.handsNeedPlacement)
        assertFalse(relaxedSignals.shouldersRaised)
        assertFalse(relaxedSignals.handsNeedPlacement)
    }

    private fun baseInput(
        leftShoulder: PosePointSample = point(-50f, 0f),
        rightShoulder: PosePointSample = point(50f, 0f),
        leftEar: PosePointSample? = null,
        rightEar: PosePointSample? = null,
        leftWrist: PosePointSample? = null,
        rightWrist: PosePointSample? = null,
    ): PoseSignalInput = PoseSignalInput(
        leftShoulder = leftShoulder,
        rightShoulder = rightShoulder,
        leftHip = point(-30f, 150f),
        rightHip = point(30f, 150f),
        leftEar = leftEar,
        rightEar = rightEar,
        leftWrist = leftWrist,
        rightWrist = rightWrist,
        faceYawDegrees = 0f,
    )

    private fun point(x: Float, y: Float, likelihood: Float = 0.95f, z: Float = 0f) =
        PosePointSample(x, y, likelihood, z)

    private fun PoseSignalResult.toSignals() = Signals(
        faceCount = 1,
        faceRatio = 0.16f,
        poseAvailable = true,
        shouldersSquare = shouldersSquare,
        shouldersRaised = shouldersRaised,
        handsIdle = handsNeedPlacement,
    )
}
