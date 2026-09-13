package com.photocoach.app.camera

import android.net.Uri
import com.photocoach.app.beauty.BeautyCompatibilityPolicy
import com.photocoach.app.creative.CaptureAssetKind
import com.photocoach.app.creative.CaptureIdentity
import com.photocoach.app.creative.CreativeColorMatrix
import java.io.File

/** Mutable state for one staged, retryable save transaction. */
internal data class PendingCapture(
    val file: File,
    val spec: CaptureSpec,
    val onSaved: (CapturedPhoto) -> Unit,
    val onSaveError: (Throwable) -> Unit,
    val onSaveProgress: (CaptureSaveProgress) -> Unit,
    val shutterElapsedMs: Long,
    val recordingStartedElapsedMs: Long,
    val capturedSensorTimestampNs: Long? = null,
    val liveGeneration:Long?=null,
    val liveSource:MotionSensorSource?=null,
    var jpegPreparation:JpegPreparation?=null,
    var jpegRawPath:String?=null,
    val warnings: MutableList<String> = mutableListOf(),
    var motionFile: File? = null,
    var packagedFile: File? = null,
    var derivativeFile: File? = null,
    var pendingUri: Uri? = null,
    var originalUri: Uri? = null,
    var captureReleased: Boolean = false,
    var derivativePendingUri: Uri? = null,
    var derivativeUri: Uri? = null,
    var recipeWritten: Boolean = false,
    var isMotionPhoto: Boolean = false,
    var effectWasDownsampled: Boolean = false,
    var outputLength: Long? = null,
    val verifiedAssetStages: MutableSet<String> = mutableSetOf(),
    val stageRetryCounts: MutableMap<String, Int> = mutableMapOf(),
    var displayName: String = CaptureIdentity.displayName(
        spec.captureId,
        CaptureAssetKind.ORIGINAL,
        spec.sequence,
        spec.takenAtMillis,
    ),
    val coordinator: SaveCoordinator = SaveCoordinator(
        SavePlan(
            motionPhotoRequested = spec.livePhotoRequested,
            derivativeRequested = BeautyCompatibilityPolicy.needsDerivative(
                spec.beautyPreset,
                spec.saveStrategy == SaveStrategy.ORIGINAL_AND_EFFECT,
                CreativeColorMatrix.isIdentity(CreativeColorMatrix.forSelection(spec.style, spec.edit)),
            ),
        ),
    ),
) {
    val derivativeRequested: Boolean get() = coordinator.snapshot.plan.derivativeRequested

    fun primaryFile(): File = packagedFile?.takeIf(File::isFile) ?: file

    fun toJournal(): SaveJournal = SaveJournal(
        captureId = spec.captureId.value,
        researchContext = spec.researchContext,
        batchId = spec.batchId,
        sequence = spec.sequence,
        takenAtMillis = spec.takenAtMillis,
        sourcePath = file.absolutePath,
        jpegPreparation=jpegPreparation,
        jpegRawPath=jpegRawPath,
        motionPath = motionFile?.absolutePath,
        packagedPath = packagedFile?.absolutePath,
        displayName = displayName,
        style = spec.style.name,
        beautyPreset = spec.beautyPreset.name,
        beautyEngineVersion = spec.beautyEngineVersion,
        exposureStops = spec.edit.exposureStops,
        contrast = spec.edit.contrast,
        saturation = spec.edit.saturation,
        temperature = spec.edit.temperature,
        tint = spec.edit.tint,
        fade = spec.edit.fade,
        styleStrength = spec.edit.styleStrength,
        derivativeQuality = spec.derivativeQuality.name,
        completedStages = coordinator.snapshot.completed.map(SaveStage::name).toSet(),
        failedStage = coordinator.snapshot.failedStage?.name,
        error = coordinator.snapshot.error,
        pendingUri = pendingUri?.toString(),
        originalUri = originalUri?.toString(),
        derivativeUri = derivativeUri?.toString(),
        derivativePendingUri = derivativePendingUri?.toString(),
        derivativePath = derivativeFile?.absolutePath,
        motionPhotoRequested = spec.livePhotoRequested,
        motionPhotoFallback = coordinator.snapshot.motionPhotoFallback,
        derivativeRequested = derivativeRequested,
        outputLength = outputLength,
        verifiedAssetStages = verifiedAssetStages.toSet(),
        stageRetryCounts = stageRetryCounts.toMap(),
    )
}
