package com.photocoach.app

import android.net.Uri
import com.photocoach.app.analysis.OverlayGeometry
import com.photocoach.app.beauty.BeautyPreset
import com.photocoach.app.beauty.BeautyPreviewState
import com.photocoach.app.camera.CameraModePreference
import com.photocoach.app.camera.CaptureAspectRatio
import com.photocoach.app.camera.CapturePriority
import com.photocoach.app.camera.CaptureTimer
import com.photocoach.app.camera.DerivativeQuality
import com.photocoach.app.camera.ExposureCapability
import com.photocoach.app.camera.FlashSetting
import com.photocoach.app.camera.QuickFocalPreset
import com.photocoach.app.camera.SaveStrategy
import com.photocoach.app.camera.ThermalLevel
import com.photocoach.app.camera.ZoomCapability
import com.photocoach.app.creative.CaptureId
import com.photocoach.app.creative.CreativeStyle
import com.photocoach.app.creative.EditAdjustment
import com.photocoach.app.creative.ParameterSuggestion
import com.photocoach.app.creative.PhotoQualityScore
import com.photocoach.app.creative.StyleDiscovery
import com.photocoach.coach.CoachOutput
import com.photocoach.coach.GuidanceSnapshot
import com.photocoach.coach.PoseCategory
import com.photocoach.coach.SuggestedMode

enum class FocusStatus { FOCUSING, SUCCESS, FAILED }

data class FocusIndicator(
    val x: Float,
    val y: Float,
    val status: FocusStatus,
    val lockRequested: Boolean,
    val generation: Int,
)

data class SceneApplyRequest(
    val mode: SuggestedMode,
    val preferTelephoto: Boolean?,
    val evStops: Float?,
    val generation: Int,
)

data class CreativePhotoUi(
    val id: String,
    val captureId: String = "unknowncapture",
    val originalUri: String,
    val displayUri: String,
    val score: PhotoQualityScore,
    val sequence: Int,
    val effectWasDownsampled: Boolean,
    val warning: String? = null,
    val isMotionPhoto: Boolean = false,
    val beautyPreset: BeautyPreset = BeautyPreset.OFF,
    val beautyEngineVersion: Int = BeautyPreset.ENGINE_VERSION,
)

data class CreativeResultUi(
    val photos: List<CreativePhotoUi>,
    val recommendedId: String,
    val selectedId: String,
    val recommendationReason: String,
    val isBurst: Boolean,
    val edit: EditAdjustment = EditAdjustment(),
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val canReset: Boolean = false,
    val compareOriginal: Boolean = false,
    val exportInProgress: Boolean = false,
    val message: String? = null,
) {
    val selectedPhoto: CreativePhotoUi get() = photos.first { it.id == selectedId }
}

data class CreativeExportRequest(
    val source: Uri,
    val edit: EditAdjustment,
    val captureId: CaptureId,
    val sequence: Int,
    val takenAtMillis: Long,
    val beautyPreset: BeautyPreset = BeautyPreset.OFF,
    val beautyEngineVersion: Int = BeautyPreset.ENGINE_VERSION,
)

data class ViewfinderUi(
    val guidance: GuidanceSnapshot,
    val coach: CoachOutput? = null,
    val overlay: OverlayGeometry? = null,
    val evStops: Float = 0f,
    val focalPresets: List<QuickFocalPreset> = emptyList(),
    val selectedFocalId: String? = null,
    val exposureCapability: ExposureCapability = ExposureCapability(),
    val zoomCapability: ZoomCapability = ZoomCapability(),
    val availableModes: Set<SuggestedMode> = setOf(SuggestedMode.PHOTO),
    val activeMode: SuggestedMode = SuggestedMode.PHOTO,
    val flashSetting: FlashSetting = FlashSetting.OFF,
    val voiceEnabled: Boolean = true,
    val subjectCaptionsEnabled: Boolean = true,
    val gridEnabled: Boolean = true,
    val levelEnabled: Boolean = true,
    val captureTimer: CaptureTimer = CaptureTimer.OFF,
    val aspectRatio: CaptureAspectRatio = CaptureAspectRatio.FOUR_THREE,
    val capturePriority: CapturePriority = CapturePriority.FOCUS,
    val modePreference: CameraModePreference = CameraModePreference.AUTO,
    val ttsFailed: Boolean = false,
    val focusIndicator: FocusIndicator? = null,
    val aeAfLocked: Boolean = false,
    val showEv: Boolean = false,
    val countdownSeconds: Int? = null,
    val recentPhoto: String? = null,
    val cameraError: String? = null,
    val lensWarning: String? = null,
    val controlMessage: String? = null,
    val sceneApply: SceneApplyRequest? = null,
    val shutterPulse: Int = 0,
    val creativeStyle: CreativeStyle = CreativeStyle.ORIGINAL,
    val creativeStyleStrength: Float = 0f,
    val styleDiscovery: StyleDiscovery = StyleDiscovery(CreativeStyle.entries, emptyList()),
    val styleFavorites: Set<CreativeStyle> = emptySet(),
    val styleRecent: List<CreativeStyle> = emptyList(),
    val threeShotBurstEnabled: Boolean = false,
    val saveStrategy: SaveStrategy = SaveStrategy.ORIGINAL_WITH_RECIPE,
    val derivativeQuality: DerivativeQuality = DerivativeQuality.FULL,
    val livePhotoEnabled: Boolean = false,
    val beautyPreset: BeautyPreset = BeautyPreset.OFF,
    val beautyPreviewWarning: String? = null,
    val beautyPreviewState: BeautyPreviewState = BeautyPreviewState.WAITING_FACE,
    val livePhotoAvailable: Boolean = false,
    val liveFallbackReason: String? = null,
    val saveStatusText: String? = null,
    val savePartialSuccess: Boolean = false,
    val burstProgress: Int? = null,
    val parameterSuggestions: List<ParameterSuggestion> = emptyList(),
    val creativeResult: CreativeResultUi? = null,
    val creativeResultVisible: Boolean = false,
    val thermalLevel: ThermalLevel = ThermalLevel.UNKNOWN,
    val thermalMessage: String? = null,
    val selectedPoseCategory: PoseCategory? = null,
    val poseCueText: String? = null,
    val p1TechniquesEnabled: Boolean = false,
)
