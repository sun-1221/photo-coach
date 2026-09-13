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
    val leftEar: PosePointSample? = null,
    val rightEar: PosePointSample? = null,
    val leftWrist: PosePointSample? = null,
    val rightWrist: PosePointSample? = null,
    val leftElbow: PosePointSample? = null,
    val rightElbow: PosePointSample? = null,
    val leftKnee: PosePointSample? = null,
    val rightKnee: PosePointSample? = null,
    val leftAnkle: PosePointSample? = null,
    val rightAnkle: PosePointSample? = null,
    val faceYawDegrees: Float? = null,
    val frameWidth: Float = 0f,
    val frameHeight: Float = 0f,
)

internal data class PoseSignalResult(
    val known: Set<String> = emptySet(),
    val shouldersSquare: Boolean = false,
    val shouldersRaised: Boolean = false,
    val handsNeedPlacement: Boolean = false,
    val atLeastOneHandOutsideTorso: Boolean = false,
    val anklesVisible: Boolean = false,
    val anklesNearBottomEdge: Boolean = false,
    val jointsNearFrameEdge: Boolean = false,
    val seatedCandidate: Boolean = false,
    val torsoUpright: Boolean = false,
    val fullBodyVisible: Boolean = false,
)

internal object PoseSignalClassifier {
    fun classify(input: PoseSignalInput): PoseSignalResult {
        val upperBody = upperBody(input) ?: return PoseSignalResult()
        val torso = torso(input, upperBody)
        val leftAnkle = input.leftAnkle.visibleOrNull()
        val rightAnkle = input.rightAnkle.visibleOrNull()
        val anklesVisible = leftAnkle != null && rightAnkle != null
        return PoseSignalResult(
            known = buildSet {
                if (torso != null && input.faceYawDegrees?.isFinite() == true) add("body-angle")
                if (input.leftEar.visibleOrNull() != null && input.rightEar.visibleOrNull() != null) add("shoulders")
                if (torso != null && input.leftWrist.visibleOrNull() != null && input.rightWrist.visibleOrNull() != null) add("hands")
                if (anklesVisible && input.frameHeight > 0f) add("feet")
                if (listOf(input.leftWrist, input.rightWrist, input.leftElbow, input.rightElbow,
                    input.leftKnee, input.rightKnee, input.leftAnkle, input.rightAnkle).all { it.visibleOrNull() != null } &&
                    input.frameWidth > 0f && input.frameHeight > 0f) add("frame-edge")
                if (torso != null && anklesVisible && input.leftKnee.visibleOrNull() != null && input.rightKnee.visibleOrNull() != null) { add("seated"); add("walking") }
            },
            shouldersSquare = shouldersSquare(input, upperBody, torso),
            shouldersRaised = shouldersRaised(input, upperBody, torso),
            handsNeedPlacement = torso?.let { handsNeedPlacement(input, it) } == true,
            atLeastOneHandOutsideTorso = torso?.let { handOutsideTorso(input, it) } == true,
            anklesVisible = anklesVisible,
            anklesNearBottomEdge = anklesVisible && listOfNotNull(leftAnkle, rightAnkle).any {
                input.frameHeight > 0f && it.y >= input.frameHeight * BOTTOM_EDGE_RATIO
            },
            jointsNearFrameEdge = jointsNearFrameEdge(input),
            seatedCandidate = torso?.let { seatedCandidate(input, it) } == true,
            torsoUpright = torso?.let { torsoUpright(upperBody, it) } == true,
            fullBodyVisible = anklesVisible && torso != null,
        )
    }

    private fun shouldersSquare(input: PoseSignalInput, upperBody: UpperBody, torso: Torso?): Boolean {
        val faceYaw = input.faceYawDegrees?.takeIf { it.isFinite() } ?: return false
        if (abs(faceYaw) > MAX_FORWARD_FACE_YAW_DEGREES) return false
        return torso != null && upperBody.shoulderSpan / torso.height >= FRONT_SHOULDER_TO_TORSO_RATIO
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

    private fun handOutsideTorso(input: PoseSignalInput, torso: Torso): Boolean {
        val minX = minOf(torso.leftShoulder.x, torso.rightShoulder.x, torso.leftHip.x, torso.rightHip.x)
        val maxX = maxOf(torso.leftShoulder.x, torso.rightShoulder.x, torso.leftHip.x, torso.rightHip.x)
        return listOfNotNull(input.leftWrist.visibleOrNull(), input.rightWrist.visibleOrNull()).any { it.x !in minX..maxX }
    }

    private fun jointsNearFrameEdge(input: PoseSignalInput): Boolean {
        if (input.frameWidth <= 0f || input.frameHeight <= 0f) return false
        val marginX = input.frameWidth * JOINT_EDGE_RATIO
        val marginY = input.frameHeight * JOINT_EDGE_RATIO
        return listOf(input.leftWrist, input.rightWrist, input.leftElbow, input.rightElbow,
            input.leftKnee, input.rightKnee, input.leftAnkle, input.rightAnkle)
            .mapNotNull { it.visibleOrNull() }
            .any { it.x <= marginX || it.x >= input.frameWidth - marginX || it.y <= marginY || it.y >= input.frameHeight - marginY }
    }

    private fun seatedCandidate(input: PoseSignalInput, torso: Torso): Boolean {
        val knees = listOfNotNull(input.leftKnee.visibleOrNull(), input.rightKnee.visibleOrNull())
        if (knees.size < 2) return false
        val hipY = (torso.leftHip.y + torso.rightHip.y) / 2f
        val kneeY = knees.map { it.y }.average().toFloat()
        return kneeY > hipY && kneeY - hipY <= torso.height * MAX_SEATED_KNEE_DROP_RATIO
    }

    private fun torsoUpright(upperBody: UpperBody, torso: Torso): Boolean {
        val shoulderX = (upperBody.leftShoulder.x + upperBody.rightShoulder.x) / 2f
        val hipX = (torso.leftHip.x + torso.rightHip.x) / 2f
        return abs(shoulderX - hipX) / torso.height <= MAX_UPRIGHT_HORIZONTAL_RATIO
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
        return Torso(upperBody.leftShoulder, upperBody.rightShoulder, leftHip, rightHip, height)
    }

    private fun pairedDistance(first: PosePointSample?, second: PosePointSample?): Float? {
        val visibleFirst = first.visibleOrNull() ?: return null
        val visibleSecond = second.visibleOrNull() ?: return null
        return distance(visibleFirst, visibleSecond)
    }

    private fun PosePointSample?.visibleOrNull(): PosePointSample? =
        this?.takeIf { it.likelihood >= MIN_LANDMARK_LIKELIHOOD && it.x.isFinite() && it.y.isFinite() }

    private fun distance(first: PosePointSample, second: PosePointSample): Float =
        hypot(first.x - second.x, first.y - second.y)

    private data class Torso(
        val leftShoulder: PosePointSample,
        val rightShoulder: PosePointSample,
        val leftHip: PosePointSample,
        val rightHip: PosePointSample,
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
    private const val RAISED_SHOULDER_TO_REFERENCE_RATIO = 0.28f
    private const val BOTTOM_EDGE_RATIO = 0.94f
    private const val JOINT_EDGE_RATIO = 0.025f
    private const val MAX_SEATED_KNEE_DROP_RATIO = 0.9f
    private const val MAX_UPRIGHT_HORIZONTAL_RATIO = 0.22f
}
