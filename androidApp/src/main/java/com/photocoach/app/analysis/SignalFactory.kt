package com.photocoach.app.analysis

import android.graphics.PointF
import android.graphics.RectF
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import com.photocoach.coach.CoarseScene
import com.photocoach.coach.Signals
import kotlin.math.abs

data class OverlayGeometry(
    val faceRects: List<RectF>,
    val posePoints: List<PointF>,
    val showSilhouette: Boolean,
    val canvasWidth: Int = 0,
    val canvasHeight: Int = 0,
)

object SignalFactory {
    fun build(
        faces: List<Face>,
        pose: Pose?,
        stats: FrameStats,
        viewWidth: Int,
        viewHeight: Int,
        tiltDegrees: Float,
        hasTelephotoPreset: Boolean,
        focusOnFace: Boolean,
        lensObscured: Boolean,
        handheldStable: Boolean = true,
        backgroundAnalysisEnabled: Boolean = true,
    ): Pair<Signals, OverlayGeometry> {
        val viewArea = (viewWidth * viewHeight).coerceAtLeast(1).toFloat()
        val largest = faces.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }
        val faceRatio = largest?.let { (it.boundingBox.width() * it.boundingBox.height()) / viewArea } ?: 0f
        val faceDetails = largest?.let {
            FaceDetailClassifier.classify(
                FaceDetailInput(
                    yawDegrees = it.headEulerAngleY,
                    leftEyeOpenProbability = it.leftEyeOpenProbability,
                    rightEyeOpenProbability = it.rightEyeOpenProbability,
                ),
            )
        } ?: FaceDetailSignals()
        val faceDarker = largest?.boundingBox?.let { box ->
            stats.lumaGrid?.let { grid ->
                FaceLuminanceClassifier.isDarkerThanBackground(
                    grid,
                    LumaRegion(box.left.toFloat(), box.top.toFloat(), box.right.toFloat(), box.bottom.toFloat()),
                )
            }
        } == true
        // FaceDetection is sensitive to phones, masks and reflected faces. Pose already
        // represents one prominent person, so keep its reliable upper-body signal unless
        // FaceDetection explicitly proves this is a multi-person frame.
        val poseOk = faces.size <= 1 && pose != null && hasUsableUpperBody(pose)
        val poseSignals = if (poseOk) {
            PoseSignalClassifier.classify(
                PoseSignalInput(
                    leftShoulder = sample(pose, PoseLandmark.LEFT_SHOULDER),
                    rightShoulder = sample(pose, PoseLandmark.RIGHT_SHOULDER),
                    leftHip = sample(pose, PoseLandmark.LEFT_HIP),
                    rightHip = sample(pose, PoseLandmark.RIGHT_HIP),
                    leftEar = sample(pose, PoseLandmark.LEFT_EAR),
                    rightEar = sample(pose, PoseLandmark.RIGHT_EAR),
                    leftWrist = sample(pose, PoseLandmark.LEFT_WRIST),
                    rightWrist = sample(pose, PoseLandmark.RIGHT_WRIST),
                    leftElbow = sample(pose, PoseLandmark.LEFT_ELBOW),
                    rightElbow = sample(pose, PoseLandmark.RIGHT_ELBOW),
                    leftKnee = sample(pose, PoseLandmark.LEFT_KNEE),
                    rightKnee = sample(pose, PoseLandmark.RIGHT_KNEE),
                    leftAnkle = sample(pose, PoseLandmark.LEFT_ANKLE),
                    rightAnkle = sample(pose, PoseLandmark.RIGHT_ANKLE),
                    faceYawDegrees = largest?.headEulerAngleY,
                    frameWidth = viewWidth.toFloat(),
                    frameHeight = viewHeight.toFloat(),
                ),
            )
        } else {
            PoseSignalResult()
        }
        val coarse = when {
            stats.meanY < 75f -> CoarseScene.INDOOR
            stats.sceneBright -> CoarseScene.OUTDOOR
            faces.isNotEmpty() -> CoarseScene.PORTRAIT
            else -> CoarseScene.UNKNOWN
        }
        val subjectCutOff = largest?.boundingBox?.let { box ->
            val marginX = viewWidth * 0.015f
            val marginY = viewHeight * 0.015f
            box.left <= marginX || box.right >= viewWidth - marginX ||
                box.top <= marginY || box.bottom >= viewHeight - marginY
        } == true
        val backgroundEdgeDensityHigh = backgroundAnalysisEnabled && largest?.boundingBox?.let { box ->
            stats.lumaGrid?.edgeDensityAround(
                LumaRegion(box.left.toFloat(), box.top.toFloat(), box.right.toFloat(), box.bottom.toFloat()),
            )?.let { it >= 0.35f }
        } == true
        val signals = Signals(
            faceCount = faces.size,
            faceRatio = faceRatio,
            faceDarkerThanScene = faceDarker,
            tiltDegrees = tiltDegrees,
            coarseScene = coarse,
            poseAvailable = poseOk,
            hasTelephotoPreset = hasTelephotoPreset,
            oneSideBrighter = stats.oneSideBrighter,
            hasLargeEnvironment = faces.size == 1 && (stats.hasLargeArchitecture || stats.hasHorizon),
            personCentered = largest != null && viewWidth > 0 &&
                abs(largest.boundingBox.exactCenterX() - viewWidth / 2f) < viewWidth * 0.08f,
            faceTooLowInFrame = largest != null && viewHeight > 0 &&
                largest.boundingBox.exactCenterY() > viewHeight * 0.48f,
            focusOnFace = focusOnFace,
            faceTurnedAway = faceDetails.faceTurnedAway,
            eyesLikelyClosed = faceDetails.eyesLikelyClosed,
            headTiltedBack = largest?.headEulerAngleX?.let { it > 12f } == true,
            shouldersSquare = poseSignals.shouldersSquare,
            shouldersRaised = poseSignals.shouldersRaised,
            handsIdle = poseSignals.handsNeedPlacement,
            skyOverexposed = stats.topMean > 200f,
            subjectCutOff = subjectCutOff,
            lensObscured = lensObscured,
            faceReliable = faces.size == 1,
            poseReliable = poseOk,
            anklesVisible = poseSignals.anklesVisible,
            anklesNearBottomEdge = poseSignals.anklesNearBottomEdge,
            jointsNearFrameEdge = poseSignals.jointsNearFrameEdge,
            atLeastOneHandOutsideTorso = poseSignals.atLeastOneHandOutsideTorso,
            seatedCandidate = poseSignals.seatedCandidate,
            torsoUpright = poseSignals.torsoUpright,
            walkingCandidate = poseSignals.fullBodyVisible,
            backgroundEdgeDensityHigh = backgroundEdgeDensityHigh,
            handheldStable = handheldStable,
            meanLuma = stats.meanY,
            highlightRatio = stats.highlightRatio,
        )
        val geometry = OverlayGeometry(
            faceRects = faces.map { RectF(it.boundingBox) },
            posePoints = posePoints(pose),
            showSilhouette = faces.size == 1 || poseOk,
        )
        return signals to geometry
    }

    private fun landmark(pose: Pose?, type: Int): PoseLandmark? = pose?.getPoseLandmark(type)

    private fun sample(pose: Pose?, type: Int): PosePointSample? = landmark(pose, type)?.let {
        PosePointSample(it.position.x, it.position.y, it.inFrameLikelihood, it.position3D.z)
    }

    private fun visible(landmark: PoseLandmark?): Boolean =
        landmark != null && landmark.inFrameLikelihood >= 0.5f

    private fun hasUsableUpperBody(pose: Pose): Boolean {
        return visible(landmark(pose, PoseLandmark.LEFT_SHOULDER)) &&
            visible(landmark(pose, PoseLandmark.RIGHT_SHOULDER))
    }

    private fun posePoints(pose: Pose?): List<PointF> {
        if (pose == null) return emptyList()
        return listOf(
            PoseLandmark.NOSE,
            PoseLandmark.LEFT_SHOULDER,
            PoseLandmark.RIGHT_SHOULDER,
            PoseLandmark.LEFT_HIP,
            PoseLandmark.RIGHT_HIP,
            PoseLandmark.LEFT_ANKLE,
            PoseLandmark.RIGHT_ANKLE,
        ).mapNotNull { type ->
            landmark(pose, type)?.takeIf(::visible)?.let { PointF(it.position.x, it.position.y) }
        }
    }
}

internal fun poseMotionSample(pose: Pose?, timestampMs: Long): PoseMotionSample? {
    if (pose == null) return null
    fun visible(type: Int): PoseLandmark? = pose.getPoseLandmark(type)?.takeIf { it.inFrameLikelihood >= 0.5f }
    val leftHip = visible(PoseLandmark.LEFT_HIP) ?: return null
    val rightHip = visible(PoseLandmark.RIGHT_HIP) ?: return null
    val leftAnkle = visible(PoseLandmark.LEFT_ANKLE) ?: return null
    val rightAnkle = visible(PoseLandmark.RIGHT_ANKLE) ?: return null
    val leftShoulder = visible(PoseLandmark.LEFT_SHOULDER) ?: return null
    val rightShoulder = visible(PoseLandmark.RIGHT_SHOULDER) ?: return null
    val reference = kotlin.math.hypot(
        leftShoulder.position.x - rightShoulder.position.x,
        leftShoulder.position.y - rightShoulder.position.y,
    )
    if (reference < 1f) return null
    return PoseMotionSample(
        timestampMs = timestampMs,
        centerX = (leftHip.position.x + rightHip.position.x) / 2f,
        centerY = (leftHip.position.y + rightHip.position.y) / 2f,
        leftAnkleY = leftAnkle.position.y,
        rightAnkleY = rightAnkle.position.y,
        referenceSize = reference,
    )
}
