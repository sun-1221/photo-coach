package com.photocoach.app.analysis

import android.graphics.Matrix
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
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .build(),
    )

    private val poseDetector = PoseDetection.getClient(
        PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build(),
    )

    private val mlKit = MlKitAnalyzer(
        listOf(faceDetector, poseDetector),
        ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED,
        executor,
    ) { result ->
        val faces = result.getValue(faceDetector) ?: emptyList()
        val pose = result.getValue(poseDetector)
        val stats = lastStats ?: return@MlKitAnalyzer
        val (w, h) = viewSize()
        if (w <= 0 || h <= 0) return@MlKitAnalyzer
        val extra = extras()
        val (signals, overlay) = SignalFactory.build(
            faces = faces,
            pose = pose,
            stats = stats,
            viewWidth = w,
            viewHeight = h,
            tiltDegrees = extra.tiltDegrees,
            hasTelephotoPreset = extra.hasTelephotoPreset,
            focusOnFace = extra.focusOnFace,
            lensObscured = lastLensObscured,
        )
        onFrame(signals, overlay)
    }

    @Volatile
    private var lastStats: FrameStats? = null
    @Volatile
    private var lastLensObscured: Boolean = false
    private val lensObstructionDetector = LensObstructionDetector()

    override fun analyze(image: ImageProxy) {
        try {
            val stats = FrameStats.compute(image)
            lastStats = stats
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

data class AnalyzerExtras(
    val tiltDegrees: Float,
    val hasTelephotoPreset: Boolean,
    val focusOnFace: Boolean,
)
