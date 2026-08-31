package com.photocoach.app.analysis

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FaceDetailClassifierTest {
    @Test
    fun turnedFaceWinsAndDoesNotGuessEyeOrExpressionDetails() {
        val result = classify(yaw = 24f, leftEye = 0.1f, rightEye = 0.1f)

        assertTrue(result.faceTurnedAway)
        assertFalse(result.eyesLikelyClosed)
    }

    @Test
    fun oneLikelyClosedEyeProducesBlinkCue() {
        val result = classify(yaw = 4f, leftEye = 0.2f, rightEye = 0.9f)

        assertFalse(result.faceTurnedAway)
        assertTrue(result.eyesLikelyClosed)
    }

    @Test
    fun frontalOpenEyesDoNotInventAnExpressionCorrection() {
        val result = classify(yaw = 0f, leftEye = 0.9f, rightEye = 0.9f)

        assertFalse(result.faceTurnedAway)
        assertFalse(result.eyesLikelyClosed)
    }

    @Test
    fun unavailableClassificationNeverInventsFacialDetails() {
        val result = classify(yaw = 0f, leftEye = null, rightEye = null)

        assertFalse(result.faceTurnedAway)
        assertFalse(result.eyesLikelyClosed)
    }

    @Test
    fun oneMissingEyeProbabilityDoesNotGuessBlinkOrExpression() {
        val result = classify(yaw = 0f, leftEye = 0.9f, rightEye = null)

        assertFalse(result.faceTurnedAway)
        assertFalse(result.eyesLikelyClosed)
    }

    private fun classify(
        yaw: Float?,
        leftEye: Float?,
        rightEye: Float?,
    ) = FaceDetailClassifier.classify(
        FaceDetailInput(yaw, leftEye, rightEye),
    )
}
