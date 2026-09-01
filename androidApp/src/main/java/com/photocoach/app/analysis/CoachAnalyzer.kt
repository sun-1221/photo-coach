package com.photocoach.app.analysis

import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.mlkit.vision.MlKitAnalyzer
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import java.util.concurrent.Executor

class CoachAnalyzer(
    executor: Executor,
    private val viewSize: () -> Pair<Int, Int>,
    private val extras: () -> AnalyzerExtras,
    private val onFrame: (signals: com.photocoach.coach.Signals, overlay: OverlayGeometry) -> Unit,
    private val minimumFrameIntervalMs: () -> Long = { 0L },
    private val poseAndBackgroundEnabled: () -> Boolean = { true },
) : ImageAnalysis.Analyzer {

    private val faceDetector = FaceDetection.getClient(coachFaceDetectorOptions())
    private val poseDetector = PoseDetection.getClient(
        PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build(),
    )

    private val fullMlKit = MlKitAnalyzer(
        listOf(faceDetector, poseDetector),
        ImageAnalysis.COORDINATE_SYSTEM_ORIGINAL,
        executor,
    ) { result ->
        emitResult(
            faces = result.getValue(faceDetector) ?: emptyList(),
            pose = result.getValue(poseDetector),
            backgroundEnabled = true,
        )
    }

    private val basicMlKit = MlKitAnalyzer(
        listOf(faceDetector),
        ImageAnalysis.COORDINATE_SYSTEM_ORIGINAL,
        executor,
    ) { result ->
        emitResult(
            faces = result.getValue(faceDetector) ?: emptyList(),
            pose = null,
            backgroundEnabled = false,
        )
    }

    @Volatile
    private var lastFrame: AnalysisFrame? = null
    @Volatile
    private var lastLensObscured: Boolean = false
    private val lensObstructionDetector = LensObstructionDetector()
    private val temporalPoseTracker = TemporalPoseTracker()
    private var lastAcceptedFrameMs = Long.MIN_VALUE

    private fun emitResult(faces: List<Face>, pose: Pose?, backgroundEnabled: Boolean) {
        val frame = lastFrame ?: return
        val (targetWidth, targetHeight) = viewSize()
        if (targetWidth <= 0 || targetHeight <= 0) return
        val extra = extras()
        val (signals, overlay) = SignalFactory.build(
            faces = faces,
            pose = pose,
            stats = frame.stats,
            viewWidth = frame.width,
            viewHeight = frame.height,
            tiltDegrees = extra.tiltDegrees,
            hasTelephotoPreset = extra.hasTelephotoPreset,
            focusOnFace = extra.focusOnFace,
            lensObscured = lastLensObscured,
            handheldStable = extra.handheldStable,
            backgroundAnalysisEnabled = backgroundEnabled,
        )
        val temporal = if (pose == null) {
            temporalPoseTracker.reset()
            TemporalPoseSignals()
        } else {
            temporalPoseTracker.update(poseMotionSample(pose, frame.timestampMs))
        }
        onFrame(
            signals.copy(
                walkingMotionStable = temporal.walkingMotionStable,
                subjectMotionHigh = temporal.subjectMotionHigh,
            ),
            overlay.fitCenter(frame.width, frame.height, targetWidth, targetHeight),
        )
    }

    override fun analyze(image: ImageProxy) {
        try {
            val timestampMs = image.imageInfo.timestamp / 1_000_000L
            val interval = minimumFrameIntervalMs().coerceAtLeast(0L)
            if (lastAcceptedFrameMs != Long.MIN_VALUE && timestampMs - lastAcceptedFrameMs < interval) {
                image.close()
                return
            }
            lastAcceptedFrameMs = timestampMs
            val stats = FrameStats.compute(image, image.imageInfo.rotationDegrees)
            val (width, height) = rotatedAnalysisDimensions(
                image.width,
                image.height,
                image.imageInfo.rotationDegrees,
            )
            lastFrame = AnalysisFrame(stats, width, height, timestampMs)
            lastLensObscured = lensObstructionDetector.update(stats)
            if (poseAndBackgroundEnabled()) {
                fullMlKit.analyze(image)
            } else {
                temporalPoseTracker.reset()
                basicMlKit.analyze(image)
            }
        } catch (_: Exception) {
            image.close()
        }
    }

    override fun getTargetCoordinateSystem(): Int = fullMlKit.targetCoordinateSystem

    override fun updateTransform(matrix: Matrix?) {
        fullMlKit.updateTransform(matrix)
        basicMlKit.updateTransform(matrix)
    }

    fun close() {
        temporalPoseTracker.reset()
        faceDetector.close()
        poseDetector.close()
    }
}
internal fun coachFaceDetectorOptions(): FaceDetectorOptions =
    FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .setMinFaceSize(0.05f)
        .enableTracking()
        .build()

data class AnalyzerExtras(
    val tiltDegrees: Float,
    val hasTelephotoPreset: Boolean,
    val focusOnFace: Boolean,
    val handheldStable: Boolean = true,
)

private data class AnalysisFrame(
    val stats: FrameStats,
    val width: Int,
    val height: Int,
    val timestampMs: Long,
)

internal fun rotatedAnalysisDimensions(width: Int, height: Int, rotationDegrees: Int): Pair<Int, Int> =
    if (rotationDegrees.mod(180) == 0) width to height else height to width

internal data class FitCenterMapping(
    val scale: Float,
    val offsetX: Float,
    val offsetY: Float,
)

internal fun fitCenterMapping(
    sourceWidth: Int,
    sourceHeight: Int,
    targetWidth: Int,
    targetHeight: Int,
): FitCenterMapping {
    if (sourceWidth <= 0 || sourceHeight <= 0 || targetWidth <= 0 || targetHeight <= 0) {
        return FitCenterMapping(1f, 0f, 0f)
    }
    val scale = minOf(targetWidth.toFloat() / sourceWidth, targetHeight.toFloat() / sourceHeight)
    return FitCenterMapping(
        scale = scale,
        offsetX = (targetWidth - sourceWidth * scale) / 2f,
        offsetY = (targetHeight - sourceHeight * scale) / 2f,
    )
}

private fun OverlayGeometry.fitCenter(
    sourceWidth: Int,
    sourceHeight: Int,
    targetWidth: Int,
    targetHeight: Int,
): OverlayGeometry {
    val mapping = fitCenterMapping(sourceWidth, sourceHeight, targetWidth, targetHeight)
    fun mapX(value: Float) = value * mapping.scale + mapping.offsetX
    fun mapY(value: Float) = value * mapping.scale + mapping.offsetY
    return copy(
        faceRects = faceRects.map { rect ->
            RectF(mapX(rect.left), mapY(rect.top), mapX(rect.right), mapY(rect.bottom))
        },
        posePoints = posePoints.map { point -> PointF(mapX(point.x), mapY(point.y)) },
        canvasWidth = targetWidth,
        canvasHeight = targetHeight,
    )
}
