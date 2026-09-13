package com.photocoach.app.camera

import android.net.Uri
import com.photocoach.app.beauty.BeautyPreset
import com.photocoach.app.creative.CaptureId
import com.photocoach.app.creative.CreativeStyle
import com.photocoach.app.creative.EditAdjustment
import com.photocoach.app.creative.NormalizedFaceRegion
import com.photocoach.app.creative.PhotoQualityScore
import java.io.IOException

enum class FlashSetting { OFF, AUTO }

data class CapturedPhoto(
    val captureId: CaptureId,
    val originalUri: Uri,
    val displayUri: Uri,
    val score: PhotoQualityScore,
    val style: CreativeStyle,
    val effectWasDownsampled: Boolean,
    val warning: String? = null,
    val isMotionPhoto: Boolean = false,
    val completedSaveStages: Set<SaveStage> = emptySet(),
    val beautyPreset: BeautyPreset = BeautyPreset.OFF,
    val beautyEngineVersion: Int = BeautyPreset.ENGINE_VERSION,
    val edit: EditAdjustment = EditAdjustment(),
)

data class ExportedCopy(
    val uri: Uri,
    val wasDownsampled: Boolean,
    val warning: String? = null,
)

class PartialSaveException(message: String, val originalUri: Uri, cause: Throwable) : IOException(message, cause)

data class CaptureSpec(
    val captureId: CaptureId,
    val sequence: Int,
    val takenAtMillis: Long,
    val style: CreativeStyle,
    val edit: EditAdjustment = EditAdjustment(),
    val saveStrategy: SaveStrategy,
    val derivativeQuality: DerivativeQuality,
    val livePhotoRequested: Boolean,
    val portraitRegion: NormalizedFaceRegion? = null,
    val beautyPreset: BeautyPreset = BeautyPreset.OFF,
    val beautyEngineVersion: Int = BeautyPreset.ENGINE_VERSION,
    val batchId: String? = null,
    val holdBatchCapture: Boolean = false,
    val researchContext: com.photocoach.app.research.ResearchCaptureContext? = null,
)
