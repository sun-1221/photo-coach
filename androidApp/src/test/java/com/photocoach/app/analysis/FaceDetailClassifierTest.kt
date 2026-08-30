package com.photocoach.app.analysis

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FaceDetailClassifierTest {
    @Test
    fun turnedFaceWinsAndDoesNotGuessEyeOrExpressionDetails() {
        val result = classify(yaw = 24f, smile = 0.02f, leftEye = 0.1f, rightEye = 0.1f)

        assertTrue(result.faceTurnedAway)
        assertFalse(result.eyesLikelyClosed)
        assertFalse(result.expressionNeedsRelaxing)
    }

    @Test
    fun oneLikelyClosedEyeProducesBlinkCueBeforeExpressionCue() {
        val result = classify(yaw = 4f, smile = 0.02f, leftEye = 0.2f, rightEye = 0.9f)

        assertFalse(result.faceTurnedAway)
        assertTrue(result.eyesLikelyClosed)
        assertFalse(result.expressionNeedsRelaxing)
    }

    @Test
    fun frontalLowSmileProbabilityProducesConservativeExpressionCue() {
        val result = classify(yaw = 0f, smile = 0.08f, leftEye = 0.9f, rightEye = 0.9f)

        assertTrue(result.expressionNeedsRelaxing)
    }

    @Test
    fun unavailableClassificationNeverInventsFacialDetails() {
        val result = classify(yaw = 0f, smile = null, leftEye = null, rightEye = null)

        assertFalse(result.faceTurnedAway)
        assertFalse(result.eyesLikelyClosed)
        assertFalse(result.expressionNeedsRelaxing)
    }

    @Test
    fun oneMissingEyeProbabilityDoesNotGuessBlinkOrExpression() {
        val result = classify(yaw = 0f, smile = 0.02f, leftEye = 0.9f, rightEye = null)

        assertFalse(result.faceTurnedAway)
        assertFalse(result.eyesLikelyClosed)
        assertFalse(result.expressionNeedsRelaxing)
    }

    private fun classify(
        yaw: Float?,
        smile: Float?,
        leftEye: Float?,
        rightEye: Float?,
    ) = FaceDetailClassifier.classify(
        FaceDetailInput(yaw, smile, leftEye, rightEye),
    )
}
