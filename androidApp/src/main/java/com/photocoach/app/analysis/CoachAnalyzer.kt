package com.photocoach.app.analysis

import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.mlkit.vision.MlKitAnalyzer
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import java.util.concurrent.Executor

class CoachAnalyzer(
    executor: Executor,
    private val viewSize: () -> Pair<Int, Int>,
    private val extras: () -> AnalyzerExtras,
    private val onFrame: (signals: com.photocoach.coach.Signals, overlay: OverlayGeometry) -> Unit,
) : ImageAnalysis.Analyzer {

    private val faceDetector = FaceDetection.getClient(
        coachFaceDetectorOptions(),
    )

    private val poseDetector = PoseDetection.getClient(
        PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build(),
    )

    private val mlKit = MlKitAnalyzer(
        listOf(faceDetector, poseDetector),
        // This analyzer is bound directly to camera-core ImageAnalysis rather than
        // CameraController. camera-core cannot provide view-referenced transforms,
        // so requesting them makes MlKitAnalyzer discard every frame.
        ImageAnalysis.COORDINATE_SYSTEM_ORIGINAL,
        executor,
    ) { result ->
        val faces = result.getValue(faceDetector) ?: emptyList()
        val pose = result.getValue(poseDetector)
        val frame = lastFrame ?: return@MlKitAnalyzer
        val (w, h) = viewSize()
        if (w <= 0 || h <= 0) return@MlKitAnalyzer
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
        )
        onFrame(signals, overlay.fitCenter(frame.width, frame.height, w, h))
    }

    @Volatile
    private var lastFrame: AnalysisFrame? = null
    @Volatile
    private var lastLensObscured: Boolean = false
    private val lensObstructionDetector = LensObstructionDetector()

    override fun analyze(image: ImageProxy) {
        try {
            val stats = FrameStats.compute(image)
            val (width, height) = rotatedAnalysisDimensions(
                image.width,
                image.height,
                image.imageInfo.rotationDegrees,
            )
            lastFrame = AnalysisFrame(stats, width, height)
            lastLensObscured = lensObstructionDetector.update(stats)
            mlKit.analyze(image)
        } catch (_: Exception) {
            image.close()
        }
    }

    override fun getTargetCoordinateSystem(): Int = mlKit.targetCoordinateSystem

    override fun updateTransform(matrix: Matrix?) {
        mlKit.updateTransform(matrix)
    }

    fun close() {
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
)

private data class AnalysisFrame(
    val stats: FrameStats,
    val width: Int,
    val height: Int,
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
    )
}
