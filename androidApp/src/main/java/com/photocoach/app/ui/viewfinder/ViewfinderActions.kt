package com.photocoach.app.ui.viewfinder

import androidx.camera.view.PreviewView
import com.photocoach.app.beauty.BeautyPreset
import com.photocoach.app.camera.CameraModePreference
import com.photocoach.app.camera.CaptureAspectRatio
import com.photocoach.app.camera.CapturePriority
import com.photocoach.app.camera.CaptureTimer
import com.photocoach.app.camera.DerivativeQuality
import com.photocoach.app.camera.SaveStrategy
import com.photocoach.app.creative.CreativeStyle
import com.photocoach.app.creative.EditAdjustment
import com.photocoach.app.creative.ParameterSuggestion
import com.photocoach.coach.PoseCategory
import com.photocoach.coach.ShotIntent

/** User and platform actions emitted by the viewfinder UI. */
data class ViewfinderActions(
    val onPreviewReady: (PreviewView) -> Unit,
    val onTapFocus: (x: Float, y: Float, lock: Boolean) -> Unit,
    val onEv: (Float) -> Unit,
    val onSetFocal: (String) -> Unit,
    val onZoomBy: (Float) -> Unit,
    val onVoiceEnabledChange: (Boolean) -> Unit,
    val onSubjectCaptionsEnabledChange: (Boolean) -> Unit,
    val onGridEnabledChange: (Boolean) -> Unit,
    val onLevelEnabledChange: (Boolean) -> Unit,
    val onTimerChange: (CaptureTimer) -> Unit,
    val onAspectRatioChange: (CaptureAspectRatio) -> Unit,
    val onCapturePriorityChange: (CapturePriority) -> Unit,
    val onModePreferenceChange: (CameraModePreference) -> Unit,
    val onResetSettings: () -> Unit,
    val onUnlockFocus: () -> Unit,
    val onToggleFlash: () -> Unit,
    val onSelectIntent: (ShotIntent) -> Unit,
    val onSkip: () -> Unit,
    val onOptional: () -> Unit,
    val onCapture: () -> Unit,
    val onOpenRecentPhoto: (String) -> Unit,
    val onRetrySave: () -> Unit,
    val onDiscardSave: () -> Unit,
    val onRetryCamera: () -> Unit,
    val onOpenSettings: () -> Unit,
    val onExit: () -> Unit,
    val onHideFocusControls: (Int) -> Unit,
    val onDismissControlMessage: () -> Unit,
    val onCreativeStyleChange: (CreativeStyle) -> Unit = {},
    val onCreativeStyleStrengthChange: (Float) -> Unit = {},
    val onToggleCurrentStyleFavorite: () -> Unit = {},
    val onPoseCategoryChange: (PoseCategory?) -> Unit = {},
    val onP1TechniquesEnabledChange: (Boolean) -> Unit = {},
    val onThreeShotBurstChange: (Boolean) -> Unit = {},
    val onSaveStrategyChange: (SaveStrategy) -> Unit = {},
    val onDerivativeQualityChange: (DerivativeQuality) -> Unit = {},
    val onLivePhotoChange: (Boolean) -> Unit = {},
    val onBeautyPresetChange: (BeautyPreset) -> Unit = {},
    val onApplyParameterSuggestion: (ParameterSuggestion) -> Unit = {},
    val onOpenCreativeResult: () -> Unit = {},
    val onSelectCreativePhoto: (String) -> Unit = {},
    val onCreativeEdit: (EditAdjustment) -> Unit = {},
    val onUndoCreativeEdit: () -> Unit = {},
    val onRedoCreativeEdit: () -> Unit = {},
    val onResetCreativeEdit: () -> Unit = {},
    val onCompareOriginal: (Boolean) -> Unit = {},
    val onSaveCreativeCopy: () -> Unit = {},
    val onOpenPhoto: (String) -> Unit = {},
    val onSharePhoto: (String) -> Unit = {},
    val onFavoritePhoto: (String) -> Unit = {},
    val onTrashPhoto: (String) -> Unit = {},
    val onDismissCreativeResult: () -> Unit = {},
    val onContinueBurst: () -> Unit = {},
    val onParameterPanelChange: (Boolean) -> Unit = {},
    val onRetryCreativeCopy: () -> Unit = {},
    val onRecoveryPanelChange: (Boolean) -> Unit = {},
    val onRetryRecoveredSave: (String) -> Unit = {},
)
