package com.photocoach.app.analysis

import kotlin.math.abs

/**
 * Converts the limited, non-identity face classifications exposed by ML Kit into
 * conservative coaching signals. Missing probabilities stay unknown instead of
 * being treated as a bad expression.
 */
data class FaceDetailInput(
    val yawDegrees: Float?,
    val smileProbability: Float?,
    val leftEyeOpenProbability: Float?,
    val rightEyeOpenProbability: Float?,
)

data class FaceDetailSignals(
    val faceTurnedAway: Boolean = false,
    val eyesLikelyClosed: Boolean = false,
    val expressionNeedsRelaxing: Boolean = false,
)

object FaceDetailClassifier {
    internal const val FRONTAL_YAW_LIMIT_DEGREES = 18f
    internal const val EYE_OPEN_THRESHOLD = 0.35f
    internal const val SMILE_THRESHOLD = 0.12f

    fun classify(input: FaceDetailInput): FaceDetailSignals {
        val yaw = input.yawDegrees
        val turnedAway = yaw != null && abs(yaw) > FRONTAL_YAW_LIMIT_DEGREES
        if (turnedAway) return FaceDetailSignals(faceTurnedAway = true)

        val leftEye = input.leftEyeOpenProbability
        val rightEye = input.rightEyeOpenProbability
        if (leftEye == null || rightEye == null) return FaceDetailSignals()

        val eyesLikelyClosed = minOf(leftEye, rightEye) <= EYE_OPEN_THRESHOLD
        if (eyesLikelyClosed) return FaceDetailSignals(eyesLikelyClosed = true)

        val needsRelaxing = input.smileProbability?.let { it <= SMILE_THRESHOLD } == true
        return FaceDetailSignals(expressionNeedsRelaxing = needsRelaxing)
    }
}
