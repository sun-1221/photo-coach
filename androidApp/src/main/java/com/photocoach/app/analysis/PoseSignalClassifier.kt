package com.photocoach.app.analysis

import kotlin.math.abs
import kotlin.math.hypot

internal data class PosePointSample(
    val x: Float,
    val y: Float,
    val likelihood: Float,
    val z: Float = 0f,
)

internal data class PoseSignalInput(
    val leftShoulder: PosePointSample? = null,
    val rightShoulder: PosePointSample? = null,
    val leftHip: PosePointSample? = null,
    val rightHip: PosePointSample? = null,
    val leftAnkle: PosePointSample? = null,
    val rightAnkle: PosePointSample? = null,
    val leftEar: PosePointSample? = null,
    val rightEar: PosePointSample? = null,
    val leftWrist: PosePointSample? = null,
    val rightWrist: PosePointSample? = null,
    val faceYawDegrees: Float? = null,
)

internal data class PoseSignalResult(
    val shouldersSquare: Boolean = false,
    val weightEven: Boolean = false,
    val shouldersRaised: Boolean = false,
    val handsNeedPlacement: Boolean = false,
)

internal object PoseSignalClassifier {
    fun classify(input: PoseSignalInput): PoseSignalResult {
        val upperBody = upperBody(input) ?: return PoseSignalResult()
        val torso = torso(input, upperBody)
        return PoseSignalResult(
            shouldersSquare = shouldersSquare(input, upperBody, torso),
            weightEven = torso?.let { weightEven(input, it) } == true,
            shouldersRaised = shouldersRaised(input, upperBody, torso),
            handsNeedPlacement = torso?.let { handsNeedPlacement(input, it) } == true,
        )
    }

    private fun shouldersSquare(input: PoseSignalInput, upperBody: UpperBody, torso: Torso?): Boolean {
        val faceYaw = input.faceYawDegrees ?: return false
        if (abs(faceYaw) > MAX_FORWARD_FACE_YAW_DEGREES) return false
        if (torso != null && upperBody.shoulderSpan / torso.height < FRONT_SHOULDER_TO_TORSO_RATIO) return false
        val shoulderDepthDifference = abs(upperBody.leftShoulder.z - upperBody.rightShoulder.z)
        return shoulderDepthDifference / upperBody.shoulderSpan <= MAX_SQUARE_SHOULDER_DEPTH_RATIO
    }

    private fun weightEven(input: PoseSignalInput, torso: Torso): Boolean {
        val leftAnkle = input.leftAnkle.visibleOrNull() ?: return false
        val rightAnkle = input.rightAnkle.visibleOrNull() ?: return false
        val ankleSpan = abs(leftAnkle.x - rightAnkle.x)
        if (ankleSpan / torso.height < MIN_STANCE_TO_TORSO_RATIO) return false
        val ankleCenterX = (leftAnkle.x + rightAnkle.x) / 2f
        return abs(torso.hipCenterX - ankleCenterX) / ankleSpan <= CENTERED_WEIGHT_OFFSET_RATIO
    }

    private fun shouldersRaised(input: PoseSignalInput, upperBody: UpperBody, torso: Torso?): Boolean {
        val distances = listOfNotNull(
            pairedDistance(input.leftEar, input.leftShoulder),
            pairedDistance(input.rightEar, input.rightShoulder),
        )
        val referenceSize = torso?.height ?: upperBody.shoulderSpan
        return distances.isNotEmpty() && distances.min() / referenceSize <= RAISED_SHOULDER_TO_REFERENCE_RATIO
    }

    private fun handsNeedPlacement(input: PoseSignalInput, torso: Torso): Boolean {
        val leftWrist = input.leftWrist.visibleOrNull() ?: return false
        val rightWrist = input.rightWrist.visibleOrNull() ?: return false
        val shoulderTop = minOf(torso.leftShoulder.y, torso.rightShoulder.y)
        val hipBottom = maxOf(torso.leftHip.y, torso.rightHip.y)
        val minX = minOf(torso.leftShoulder.x, torso.rightShoulder.x)
        val maxX = maxOf(torso.leftShoulder.x, torso.rightShoulder.x)
        return listOf(leftWrist, rightWrist).all { wrist ->
            wrist.y in shoulderTop..hipBottom && wrist.x in minX..maxX
        }
    }

    private fun upperBody(input: PoseSignalInput): UpperBody? {
        val leftShoulder = input.leftShoulder.visibleOrNull() ?: return null
        val rightShoulder = input.rightShoulder.visibleOrNull() ?: return null
        val shoulderSpan = distance(leftShoulder, rightShoulder)
        if (shoulderSpan < MIN_UPPER_BODY_SIZE) return null
        return UpperBody(leftShoulder, rightShoulder, shoulderSpan)
    }

    private fun torso(input: PoseSignalInput, upperBody: UpperBody): Torso? {
        val leftHip = input.leftHip.visibleOrNull() ?: return null
        val rightHip = input.rightHip.visibleOrNull() ?: return null
        val shoulderCenterX = (upperBody.leftShoulder.x + upperBody.rightShoulder.x) / 2f
        val shoulderCenterY = (upperBody.leftShoulder.y + upperBody.rightShoulder.y) / 2f
        val hipCenterX = (leftHip.x + rightHip.x) / 2f
        val hipCenterY = (leftHip.y + rightHip.y) / 2f
        val height = hypot(shoulderCenterX - hipCenterX, shoulderCenterY - hipCenterY)
        if (height < MIN_TORSO_SIZE) return null
        return Torso(upperBody.leftShoulder, upperBody.rightShoulder, leftHip, rightHip, hipCenterX, height)
    }

    private fun pairedDistance(first: PosePointSample?, second: PosePointSample?): Float? {
        val visibleFirst = first.visibleOrNull() ?: return null
        val visibleSecond = second.visibleOrNull() ?: return null
        return distance(visibleFirst, visibleSecond)
    }

    private fun PosePointSample?.visibleOrNull(): PosePointSample? =
        this?.takeIf { it.likelihood >= MIN_LANDMARK_LIKELIHOOD }

    private fun distance(first: PosePointSample, second: PosePointSample): Float =
        hypot(first.x - second.x, first.y - second.y)

    private data class Torso(
        val leftShoulder: PosePointSample,
        val rightShoulder: PosePointSample,
        val leftHip: PosePointSample,
        val rightHip: PosePointSample,
        val hipCenterX: Float,
        val height: Float,
    )

    private data class UpperBody(
        val leftShoulder: PosePointSample,
        val rightShoulder: PosePointSample,
        val shoulderSpan: Float,
    )

    private const val MIN_LANDMARK_LIKELIHOOD = 0.5f
    private const val MIN_UPPER_BODY_SIZE = 1f
    private const val MIN_TORSO_SIZE = 1f
    private const val MAX_FORWARD_FACE_YAW_DEGREES = 15f
    private const val FRONT_SHOULDER_TO_TORSO_RATIO = 0.6f
    private const val MAX_SQUARE_SHOULDER_DEPTH_RATIO = 0.35f
    private const val MIN_STANCE_TO_TORSO_RATIO = 0.35f
    private const val CENTERED_WEIGHT_OFFSET_RATIO = 0.15f
    private const val RAISED_SHOULDER_TO_REFERENCE_RATIO = 0.28f
}
