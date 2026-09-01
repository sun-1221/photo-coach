package com.photocoach.app.ui.viewfinder

import android.graphics.ColorMatrix as AndroidColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.os.Build
import android.widget.ImageView
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import com.photocoach.app.FocusStatus
import com.photocoach.app.CreativeResultUi
import com.photocoach.app.ViewfinderUi
import com.photocoach.app.camera.FlashSetting
import com.photocoach.app.camera.CameraModePreference
import com.photocoach.app.camera.CaptureAspectRatio
import com.photocoach.app.camera.CapturePriority
import com.photocoach.app.camera.CaptureTimer
import com.photocoach.app.camera.DerivativeQuality
import com.photocoach.app.camera.SaveStrategy
import com.photocoach.app.camera.ThermalPolicy
import com.photocoach.app.creative.CreativeColorMatrix
import com.photocoach.app.creative.CreativeStyle
import com.photocoach.app.creative.EditAdjustment
import com.photocoach.app.creative.ImageDecodePolicy
import com.photocoach.app.creative.ParameterSuggestion
import com.photocoach.coach.Audience
import com.photocoach.coach.GuidanceStage
import com.photocoach.coach.ShotIntent
import com.photocoach.coach.SuggestedMode
import com.photocoach.coach.PoseCategory
import kotlinx.coroutines.delay
import java.util.IdentityHashMap
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun ViewfinderScreen(
    ui: ViewfinderUi,
    tiltDegrees: Float,
    onPreviewReady: (PreviewView) -> Unit,
    onTapFocus: (x: Float, y: Float, lock: Boolean) -> Unit,
    onEv: (Float) -> Unit,
    onSetFocal: (String) -> Unit,
    onZoomBy: (Float) -> Unit,
    onVoiceEnabledChange: (Boolean) -> Unit,
    onSubjectCaptionsEnabledChange: (Boolean) -> Unit,
    onGridEnabledChange: (Boolean) -> Unit,
    onLevelEnabledChange: (Boolean) -> Unit,
    onTimerChange: (CaptureTimer) -> Unit,
    onAspectRatioChange: (CaptureAspectRatio) -> Unit,
    onCapturePriorityChange: (CapturePriority) -> Unit,
    onModePreferenceChange: (CameraModePreference) -> Unit,
    onResetSettings: () -> Unit,
    onUnlockFocus: () -> Unit,
    onToggleFlash: () -> Unit,
    onSelectIntent: (ShotIntent) -> Unit,
    onSkip: () -> Unit,
    onOptional: () -> Unit,
    onCapture: () -> Unit,
    onOpenRecentPhoto: (String) -> Unit,
    onRetrySave: () -> Unit,
    onDiscardSave: () -> Unit,
    onRetryCamera: () -> Unit,
    onOpenSettings: () -> Unit,
    onExit: () -> Unit,
    onHideFocusControls: (Int) -> Unit,
    onDismissControlMessage: () -> Unit,
    onCreativeStyleChange: (CreativeStyle) -> Unit = {},
    onCreativeStyleStrengthChange: (Float) -> Unit = {},
    onToggleCurrentStyleFavorite: () -> Unit = {},
    onPoseCategoryChange: (PoseCategory?) -> Unit = {},
    onP1TechniquesEnabledChange: (Boolean) -> Unit = {},
    onThreeShotBurstChange: (Boolean) -> Unit = {},
    onSaveStrategyChange: (SaveStrategy) -> Unit = {},
    onDerivativeQualityChange: (DerivativeQuality) -> Unit = {},
    onLivePhotoChange: (Boolean) -> Unit = {},
    onApplyParameterSuggestion: (ParameterSuggestion) -> Unit = {},
    onOpenCreativeResult: () -> Unit = {},
    onSelectCreativePhoto: (String) -> Unit = {},
    onCreativeEdit: (EditAdjustment) -> Unit = {},
    onUndoCreativeEdit: () -> Unit = {},
    onRedoCreativeEdit: () -> Unit = {},
    onResetCreativeEdit: () -> Unit = {},
    onCompareOriginal: (Boolean) -> Unit = {},
    onSaveCreativeCopy: () -> Unit = {},
    onOpenPhoto: (String) -> Unit = {},
    onSharePhoto: (String) -> Unit = {},
    onFavoritePhoto: (String) -> Unit = {},
    onTrashPhoto: (String) -> Unit = {},
    onDismissCreativeResult: () -> Unit = {},
) {
    ui.focusIndicator?.let { indicator ->
        LaunchedEffect(indicator.generation) {
            delay(FOCUS_CONTROLS_DURATION_MS)
            onHideFocusControls(indicator.generation)
        }
    }
    ui.controlMessage?.let { message ->
        LaunchedEffect(message) {
            delay(CONTROL_MESSAGE_DURATION_MS)
            onDismissControlMessage()
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize().background(Color.Black)) {
        val landscape = maxWidth > maxHeight
        val portraitPanelMaxHeight = maxHeight * OPERATION_PANEL_MAX_HEIGHT_FRACTION
        if (landscape) {
            Row(Modifier.fillMaxSize()) {
                PreviewPane(
                    ui = ui,
                    tiltDegrees = tiltDegrees,
                    onPreviewReady = onPreviewReady,
                    onTapFocus = onTapFocus,
                    onZoomBy = onZoomBy,
                    onVoiceEnabledChange = onVoiceEnabledChange,
                    onSubjectCaptionsEnabledChange = onSubjectCaptionsEnabledChange,
                    onGridEnabledChange = onGridEnabledChange,
                    onLevelEnabledChange = onLevelEnabledChange,
                    onTimerChange = onTimerChange,
                    onAspectRatioChange = onAspectRatioChange,
                    onCapturePriorityChange = onCapturePriorityChange,
                    onModePreferenceChange = onModePreferenceChange,
                    onResetSettings = onResetSettings,
                    onUnlockFocus = onUnlockFocus,
                    onToggleFlash = onToggleFlash,
                    onSelectIntent = onSelectIntent,
                    onExit = onExit,
                    onEv = onEv,
                    modifier = Modifier.weight(1f),
                )
                OperationPanel(
                    ui = ui,
                    onSetFocal = onSetFocal,
                    onSkip = onSkip,
                    onOptional = onOptional,
                    onCapture = onCapture,
                    onOpenRecentPhoto = onOpenRecentPhoto,
                    onRetrySave = onRetrySave,
                    onDiscardSave = onDiscardSave,
                    onCreativeStyleChange = onCreativeStyleChange,
                    onCreativeStyleStrengthChange = onCreativeStyleStrengthChange,
                    onToggleCurrentStyleFavorite = onToggleCurrentStyleFavorite,
                    onPoseCategoryChange = onPoseCategoryChange,
                    onP1TechniquesEnabledChange = onP1TechniquesEnabledChange,
                    onThreeShotBurstChange = onThreeShotBurstChange,
                    onSaveStrategyChange = onSaveStrategyChange,
                    onDerivativeQualityChange = onDerivativeQualityChange,
                    onLivePhotoChange = onLivePhotoChange,
                    onApplyParameterSuggestion = onApplyParameterSuggestion,
                    onOpenCreativeResult = onOpenCreativeResult,
                    modifier = Modifier.width(320.dp).fillMaxSize(),
                )
            }
        } else {
            Box(Modifier.fillMaxSize()) {
                PreviewPane(
                    ui = ui,
                    tiltDegrees = tiltDegrees,
                    onPreviewReady = onPreviewReady,
                    onTapFocus = onTapFocus,
                    onZoomBy = onZoomBy,
                    onVoiceEnabledChange = onVoiceEnabledChange,
                    onSubjectCaptionsEnabledChange = onSubjectCaptionsEnabledChange,
                    onGridEnabledChange = onGridEnabledChange,
                    onLevelEnabledChange = onLevelEnabledChange,
                    onTimerChange = onTimerChange,
                    onAspectRatioChange = onAspectRatioChange,
                    onCapturePriorityChange = onCapturePriorityChange,
                    onModePreferenceChange = onModePreferenceChange,
                    onResetSettings = onResetSettings,
                    onUnlockFocus = onUnlockFocus,
                    onToggleFlash = onToggleFlash,
                    onSelectIntent = onSelectIntent,
                    onExit = onExit,
                    onEv = onEv,
                    modifier = Modifier.fillMaxSize(),
                )
                OperationPanel(
                    ui = ui,
                    onSetFocal = onSetFocal,
                    onSkip = onSkip,
                    onOptional = onOptional,
                    onCapture = onCapture,
                    onOpenRecentPhoto = onOpenRecentPhoto,
                    onRetrySave = onRetrySave,
                    onDiscardSave = onDiscardSave,
                    onCreativeStyleChange = onCreativeStyleChange,
                    onCreativeStyleStrengthChange = onCreativeStyleStrengthChange,
                    onToggleCurrentStyleFavorite = onToggleCurrentStyleFavorite,
                    onPoseCategoryChange = onPoseCategoryChange,
                    onP1TechniquesEnabledChange = onP1TechniquesEnabledChange,
                    onThreeShotBurstChange = onThreeShotBurstChange,
                    onSaveStrategyChange = onSaveStrategyChange,
                    onDerivativeQualityChange = onDerivativeQualityChange,
                    onLivePhotoChange = onLivePhotoChange,
                    onApplyParameterSuggestion = onApplyParameterSuggestion,
                    onOpenCreativeResult = onOpenCreativeResult,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .heightIn(max = portraitPanelMaxHeight),
                )
            }
        }

        ui.cameraError?.let { error ->
            ErrorRecovery(
                message = error,
                onRetry = onRetryCamera,
                onSettings = onOpenSettings,
                modifier = Modifier.align(Alignment.Center),
            )
        }
        ui.controlMessage?.let { message ->
            Text(
                text = message,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
                    .testTag("control_message")
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            )
        }
        ui.countdownSeconds?.let { seconds ->
            Text(
                text = seconds.toString(),
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                    .testTag("capture_countdown")
                    .padding(horizontal = 28.dp, vertical = 16.dp),
            )
        }
        if (ui.ttsFailed) {
            Text(
                text = if (ui.subjectCaptionsEnabled) {
                    "中文语音不可用，已使用字幕"
                } else {
                    "中文语音不可用，本条已跳过"
                },
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .background(Color.Black.copy(alpha = 0.75f))
                    .padding(10.dp),
            )
        }
        ui.creativeResult?.takeIf { ui.creativeResultVisible }?.let { result ->
            CreativeResultPanel(
                ui = ui,
                result = result,
                onSelectPhoto = onSelectCreativePhoto,
                onStyleChange = onCreativeStyleChange,
                onEdit = onCreativeEdit,
                onUndo = onUndoCreativeEdit,
                onRedo = onRedoCreativeEdit,
                onReset = onResetCreativeEdit,
                onCompareOriginal = onCompareOriginal,
                onSaveCopy = onSaveCreativeCopy,
                onOpenPhoto = onOpenPhoto,
                onSharePhoto = onSharePhoto,
                onFavoritePhoto = onFavoritePhoto,
                onTrashPhoto = onTrashPhoto,
                onDismiss = onDismissCreativeResult,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun PreviewPane(
    ui: ViewfinderUi,
    tiltDegrees: Float,
    onPreviewReady: (PreviewView) -> Unit,
    onTapFocus: (x: Float, y: Float, lock: Boolean) -> Unit,
    onZoomBy: (Float) -> Unit,
    onVoiceEnabledChange: (Boolean) -> Unit,
    onSubjectCaptionsEnabledChange: (Boolean) -> Unit,
    onGridEnabledChange: (Boolean) -> Unit,
    onLevelEnabledChange: (Boolean) -> Unit,
    onTimerChange: (CaptureTimer) -> Unit,
    onAspectRatioChange: (CaptureAspectRatio) -> Unit,
    onCapturePriorityChange: (CapturePriority) -> Unit,
    onModePreferenceChange: (CameraModePreference) -> Unit,
    onResetSettings: () -> Unit,
    onUnlockFocus: () -> Unit,
    onToggleFlash: () -> Unit,
    onSelectIntent: (ShotIntent) -> Unit,
    onExit: () -> Unit,
    onEv: (Float) -> Unit,
    modifier: Modifier,
) {
    var previewEffectError by remember { mutableStateOf<String?>(null) }
    BoxWithConstraints(modifier.background(Color.Black)) {
        val landscapeSurface = maxWidth > maxHeight
        Box(Modifier.fillMaxSize().testTag("camera_surface")) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("camera_preview")
                    .pointerInput(Unit) {
                        detectTransformGestures { _, _, zoomChange, _ ->
                            if (abs(zoomChange - 1f) >= 0.005f) onZoomBy(zoomChange)
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = { onTapFocus(it.x, it.y, true) },
                            onTap = { onTapFocus(it.x, it.y, false) },
                        )
                    },
                factory = { context ->
                    PreviewView(context).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        // Match CameraBinder's FILL_CENTER ViewPort so the long-screen
                        // viewfinder is filled without letterboxing the 4:3 preview.
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        onPreviewReady(this)
                    }
                },
                update = { preview ->
                    val thermalPolicy = ThermalPolicy.forLevel(ui.thermalLevel)
                    previewEffectError = if (thermalPolicy.stylePreviewEnabled) {
                        applyPreviewStyle(preview, ui.creativeStyle, ui.creativeStyleStrength)
                    } else {
                        applyPreviewStyle(preview, CreativeStyle.ORIGINAL, 0f)
                        if (ui.creativeStyle == CreativeStyle.ORIGINAL) null else "设备升温，已暂停实时风格预览"
                    }
                },
            )
            CoachOverlay(
                geometry = ui.overlay,
                hint = ui.coach?.overlay,
                tiltDegrees = tiltDegrees,
                showGrid = ui.gridEnabled,
                showLevel = ui.levelEnabled,
                modifier = Modifier.fillMaxSize(),
            )
            FocusOverlay(ui)
            previewEffectError?.let { message ->
                Text(
                    text = message,
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.78f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("preview_effect_error"),
                )
            }
        }
        TopBar(
            ui = ui,
            onVoiceEnabledChange = onVoiceEnabledChange,
            onSubjectCaptionsEnabledChange = onSubjectCaptionsEnabledChange,
            onGridEnabledChange = onGridEnabledChange,
            onLevelEnabledChange = onLevelEnabledChange,
            onTimerChange = onTimerChange,
            onAspectRatioChange = onAspectRatioChange,
            onCapturePriorityChange = onCapturePriorityChange,
            onModePreferenceChange = onModePreferenceChange,
            onResetSettings = onResetSettings,
            onUnlockFocus = onUnlockFocus,
            onToggleFlash = onToggleFlash,
            onSelectIntent = onSelectIntent,
            onExit = onExit,
            modifier = Modifier.align(Alignment.TopCenter),
        )
        if (ui.showEv && ui.exposureCapability.supported) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(bottom = if (landscapeSurface) 0.dp else PORTRAIT_DOCK_CLEARANCE)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("EV", color = Color.White)
                Slider(
                    value = ui.evStops,
                    onValueChange = onEv,
                    valueRange = ui.exposureCapability.minimumStops..ui.exposureCapability.maximumStops,
                    modifier = Modifier.weight(1f).testTag("ev_slider"),
                )
                Text(if (ui.evStops >= 0f) "+${ui.evStops}" else ui.evStops.toString(), color = Color.White)
            }
        }
    }
}

@Composable
private fun TopBar(
    ui: ViewfinderUi,
    onVoiceEnabledChange: (Boolean) -> Unit,
    onSubjectCaptionsEnabledChange: (Boolean) -> Unit,
    onGridEnabledChange: (Boolean) -> Unit,
    onLevelEnabledChange: (Boolean) -> Unit,
    onTimerChange: (CaptureTimer) -> Unit,
    onAspectRatioChange: (CaptureAspectRatio) -> Unit,
    onCapturePriorityChange: (CapturePriority) -> Unit,
    onModePreferenceChange: (CameraModePreference) -> Unit,
    onResetSettings: () -> Unit,
    onUnlockFocus: () -> Unit,
    onToggleFlash: () -> Unit,
    onSelectIntent: (ShotIntent) -> Unit,
    onExit: () -> Unit,
    modifier: Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(Color.Black.copy(alpha = 0.38f))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SmallControl("退出", "exit", onExit)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                PromptSettingsControl(
                    ui = ui,
                    onVoiceEnabledChange = onVoiceEnabledChange,
                    onSubjectCaptionsEnabledChange = onSubjectCaptionsEnabledChange,
                )
                CameraSettingsControl(
                    ui = ui,
                    onGridEnabledChange = onGridEnabledChange,
                    onLevelEnabledChange = onLevelEnabledChange,
                    onTimerChange = onTimerChange,
                    onAspectRatioChange = onAspectRatioChange,
                    onCapturePriorityChange = onCapturePriorityChange,
                    onModePreferenceChange = onModePreferenceChange,
                    onResetSettings = onResetSettings,
                )
                SmallControl(
                    if (ui.flashSetting == FlashSetting.OFF) "闪光关" else "闪光自动",
                    "flash",
                    onToggleFlash,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            IntentButton(
                text = "人物特写",
                selected = ui.guidance.intent == ShotIntent.CLOSE_UP,
                tag = "intent_close_up",
                onClick = { onSelectIntent(ShotIntent.CLOSE_UP) },
            )
            Spacer(Modifier.width(6.dp))
            IntentButton(
                text = "人带景",
                selected = ui.guidance.intent == ShotIntent.PERSON_WITH_SCENERY,
                tag = "intent_scenery",
                onClick = { onSelectIntent(ShotIntent.PERSON_WITH_SCENERY) },
            )
        }
        if (ui.aeAfLocked) {
            FilledTonalButton(
                onClick = onUnlockFocus,
                modifier = Modifier.align(Alignment.CenterHorizontally).height(48.dp).testTag("ae_af_unlock"),
            ) {
                Text("对焦和曝光已锁定 · 点此解除")
            }
        }
    }
}

@Composable
private fun OperationPanel(
    ui: ViewfinderUi,
    onSetFocal: (String) -> Unit,
    onSkip: () -> Unit,
    onOptional: () -> Unit,
    onCapture: () -> Unit,
    onOpenRecentPhoto: (String) -> Unit,
    onRetrySave: () -> Unit,
    onDiscardSave: () -> Unit,
    onCreativeStyleChange: (CreativeStyle) -> Unit,
    onCreativeStyleStrengthChange: (Float) -> Unit,
    onToggleCurrentStyleFavorite: () -> Unit,
    onPoseCategoryChange: (PoseCategory?) -> Unit,
    onP1TechniquesEnabledChange: (Boolean) -> Unit,
    onThreeShotBurstChange: (Boolean) -> Unit,
    onSaveStrategyChange: (SaveStrategy) -> Unit,
    onDerivativeQualityChange: (DerivativeQuality) -> Unit,
    onLivePhotoChange: (Boolean) -> Unit,
    onApplyParameterSuggestion: (ParameterSuggestion) -> Unit,
    onOpenCreativeResult: () -> Unit,
    modifier: Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val guidanceBackground = when {
        ui.guidance.stage is GuidanceStage.SaveFailed -> Color(0xFFFFC7C7)
        ui.guidance.stage is GuidanceStage.Saved || ui.guidance.stage is GuidanceStage.Ready -> Color(0xFFB9F39A)
        else -> Color(0xFFFFCF45)
    }
    val guidanceForeground = Color(0xFF15130D)
    Column(
        modifier = modifier
            .testTag("operation_panel")
            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
            .background(Color(0xF20D0D0F))
            .navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(guidanceBackground)
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = guidanceLabel(ui),
                    color = guidanceForeground,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                    modifier = Modifier.widthIn(min = 34.dp).testTag("guidance_label"),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = guidanceText(ui),
                    color = guidanceForeground,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).testTag("guidance_text"),
                )
            }
            if (hasActiveLensWarning(ui)) {
                // Critical camera-quality warnings temporarily own the single action slot.
            } else if (ui.guidance.canSkip) {
                TextButton(onClick = onSkip, modifier = Modifier.size(width = 64.dp, height = 48.dp).testTag("skip")) {
                    Text("跳过")
                }
            } else {
                val failure = ui.guidance.stage as? GuidanceStage.SaveFailed
                when {
                    failure?.retryAvailable == true -> {
                        Button(onClick = onRetrySave, modifier = Modifier.height(48.dp).testTag("save_retry")) {
                            Text("重试")
                        }
                        TextButton(onClick = onDiscardSave, modifier = Modifier.height(48.dp).testTag("save_discard")) {
                            Text("放弃")
                        }
                    }
                    failure != null -> {
                        TextButton(onClick = onDiscardSave, modifier = Modifier.height(48.dp).testTag("save_discard")) {
                            Text("放弃")
                        }
                    }
                    ui.guidance.canRequestOptional -> {
                        TextButton(onClick = onOptional, modifier = Modifier.height(48.dp).testTag("optional")) {
                            Text("再优化")
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ui.focalPresets.filter { it.isQuickControlAvailable }.forEachIndexed { index, preset ->
                    ZoomButton(
                        text = preset.label,
                        selected = preset.cameraId == ui.selectedFocalId,
                        tag = "focal_$index",
                    ) { onSetFocal(preset.cameraId) }
                }
                CreativeCaptureControl(
                    ui = ui,
                    onStyleChange = onCreativeStyleChange,
                    onStyleStrengthChange = onCreativeStyleStrengthChange,
                    onToggleCurrentStyleFavorite = onToggleCurrentStyleFavorite,
                    onPoseCategoryChange = onPoseCategoryChange,
                    onP1TechniquesEnabledChange = onP1TechniquesEnabledChange,
                    onBurstChange = onThreeShotBurstChange,
                    onSaveStrategyChange = onSaveStrategyChange,
                    onDerivativeQualityChange = onDerivativeQualityChange,
                    onLivePhotoChange = onLivePhotoChange,
                    onApplyParameterSuggestion = onApplyParameterSuggestion,
                    onOpenCreativeResult = onOpenCreativeResult,
                )
            }
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onCapture()
                },
                enabled = ui.guidance.shutterEnabled,
                modifier = Modifier.size(56.dp).clip(CircleShape).testTag("shutter"),
            ) {
                Text(if (ui.countdownSeconds != null) "停" else "拍")
            }
            LatestPhoto(ui.recentPhoto, onOpenRecentPhoto)
        }
    }
}

@Composable
private fun LatestPhoto(uri: String?, onClick: (String) -> Unit) {
    if (uri == null) {
        Box(
            Modifier.size(48.dp).border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) { Text("最近", style = MaterialTheme.typography.labelSmall) }
        return
    }
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.DarkGray)
            .clickable { onClick(uri) }
            .testTag("recent_photo"),
    ) {
        BoundedLocalImage(
            uri = uri,
            maximumPixels = ImageDecodePolicy.THUMBNAIL_MAX_PIXELS,
            scaleType = ImageView.ScaleType.CENTER_CROP,
            contentDescription = "最近拍摄的照片",
            errorLabel = "缩略图不可用",
            errorTag = "recent_photo_error",
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun CreativeCaptureControl(
    ui: ViewfinderUi,
    onStyleChange: (CreativeStyle) -> Unit,
    onStyleStrengthChange: (Float) -> Unit,
    onToggleCurrentStyleFavorite: () -> Unit,
    onPoseCategoryChange: (PoseCategory?) -> Unit,
    onP1TechniquesEnabledChange: (Boolean) -> Unit,
    onBurstChange: (Boolean) -> Unit,
    onSaveStrategyChange: (SaveStrategy) -> Unit,
    onDerivativeQualityChange: (DerivativeQuality) -> Unit,
    onLivePhotoChange: (Boolean) -> Unit,
    onApplyParameterSuggestion: (ParameterSuggestion) -> Unit,
    onOpenCreativeResult: () -> Unit,
) {
    val capturing = ui.guidance.stage is GuidanceStage.Capturing
    var expanded by remember { mutableStateOf(false) }
    Box {
        TextButton(
            onClick = { expanded = true },
            enabled = !capturing,
            modifier = Modifier.height(48.dp).widthIn(min = 76.dp).testTag("creative_capture_menu"),
        ) {
            Text(
                ui.burstProgress?.let { "$it/3" }
                    ?: if (ui.threeShotBurstEnabled) "${ui.creativeStyle.label}·3张" else ui.creativeStyle.label,
                maxLines = 1,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(320.dp),
        ) {
            StyleMenuItem(
                style = CreativeStyle.ORIGINAL,
                selected = ui.creativeStyle,
                detail = "始终第一；不做颜色处理",
                tag = "creative_style_original",
                onStyleChange = onStyleChange,
            )
            StyleMenuSection(
                title = "推荐",
                styles = ui.styleDiscovery.recommendations.map { it.style },
                ui = ui,
                tagPrefix = "recommended",
                onStyleChange = onStyleChange,
            )
            StyleMenuSection(
                title = "最近",
                styles = ui.styleRecent,
                ui = ui,
                tagPrefix = "recent",
                onStyleChange = onStyleChange,
            )
            StyleMenuSection(
                title = "收藏",
                styles = ui.styleFavorites.sortedBy(CreativeStyle::ordinal),
                ui = ui,
                tagPrefix = "favorite",
                onStyleChange = onStyleChange,
            )
            StyleMenuSection(
                title = "全部",
                styles = CreativeStyle.entries.filterNot { it == CreativeStyle.ORIGINAL },
                ui = ui,
                tagPrefix = "all",
                onStyleChange = onStyleChange,
            )
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    if (ui.creativeStyle == CreativeStyle.ORIGINAL) "强度：原图" else "强度：${(ui.creativeStyleStrength * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                )
                Slider(
                    value = ui.creativeStyleStrength,
                    onValueChange = onStyleStrengthChange,
                    enabled = ui.creativeStyle != CreativeStyle.ORIGINAL,
                    valueRange = 0f..1f,
                    modifier = Modifier.testTag("creative_style_strength"),
                )
            }
            DropdownMenuItem(
                text = { Text(if (ui.creativeStyle in ui.styleFavorites) "取消收藏当前风格" else "收藏当前风格") },
                enabled = ui.creativeStyle != CreativeStyle.ORIGINAL,
                onClick = onToggleCurrentStyleFavorite,
                modifier = Modifier.testTag("creative_style_favorite"),
            )
            DropdownMenuItem(
                text = { SettingText("姿势灵感", ui.selectedPoseCategory?.poseLabel() ?: "关闭") },
                onClick = {
                    val values = listOf<PoseCategory?>(null) + PoseCategory.entries
                    onPoseCategoryChange(values[(values.indexOf(ui.selectedPoseCategory) + 1).mod(values.size)])
                },
                modifier = Modifier.testTag("pose_category"),
            )
            DropdownMenuItem(
                text = { SettingText("摄影技巧", if (ui.p1TechniquesEnabled) "开启" else "关闭") },
                leadingIcon = { Checkbox(checked = ui.p1TechniquesEnabled, onCheckedChange = onP1TechniquesEnabledChange) },
                onClick = { onP1TechniquesEnabledChange(!ui.p1TechniquesEnabled) },
                modifier = Modifier.testTag("p1_techniques"),
            )
            DropdownMenuItem(
                text = {
                    Column {
                        Text("无声 Live")
                        Text(
                            when {
                                ui.livePhotoEnabled && ui.livePhotoAvailable -> "Motion Photo 已就绪；主文件保持原始颜色"
                                ui.livePhotoEnabled -> ui.liveFallbackReason ?: "不可用时自动保存普通照片"
                                else -> "Android Motion Photo；不录音、不申请麦克风"
                            },
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                },
                leadingIcon = {
                    Checkbox(checked = ui.livePhotoEnabled, onCheckedChange = onLivePhotoChange, modifier = Modifier.testTag("live_photo"))
                },
                onClick = { onLivePhotoChange(!ui.livePhotoEnabled) },
            )
            DropdownMenuItem(
                text = {
                    Column {
                        Text("明确三张连拍")
                        Text("一次固定拍 3 张，三张都保留", style = MaterialTheme.typography.labelSmall)
                    }
                },
                leadingIcon = {
                    Checkbox(
                        checked = ui.threeShotBurstEnabled,
                        onCheckedChange = onBurstChange,
                        modifier = Modifier.testTag("three_shot_burst"),
                    )
                },
                onClick = { onBurstChange(!ui.threeShotBurstEnabled) },
            )
            DropdownMenuItem(
                text = { SettingText("保存策略", ui.saveStrategy.label) },
                onClick = {
                    onSaveStrategyChange(
                        if (ui.saveStrategy == SaveStrategy.ORIGINAL_WITH_RECIPE) SaveStrategy.ORIGINAL_AND_EFFECT
                        else SaveStrategy.ORIGINAL_WITH_RECIPE,
                    )
                },
                modifier = Modifier.testTag("save_strategy"),
            )
            DropdownMenuItem(
                text = { SettingText("效果副本", ui.derivativeQuality.label) },
                onClick = {
                    onDerivativeQualityChange(
                        if (ui.derivativeQuality == DerivativeQuality.FULL) DerivativeQuality.SPACE_SAVER
                        else DerivativeQuality.FULL,
                    )
                },
                modifier = Modifier.testTag("derivative_quality"),
            )
            if (ui.creativeResult != null) {
                DropdownMenuItem(
                    text = { SettingText("编辑刚拍照片", "照片已经保存；仅在需要时打开编辑和拍后操作") },
                    onClick = {
                        expanded = false
                        onOpenCreativeResult()
                    },
                    modifier = Modifier.testTag("open_creative_result"),
                )
            }
            if (ui.parameterSuggestions.isEmpty()) {
                DropdownMenuItem(
                    text = { SettingText("参数建议", "当前参数可以直接拍") },
                    enabled = false,
                    onClick = {},
                    modifier = Modifier.testTag("parameter_suggestion_empty"),
                )
            } else {
                ui.parameterSuggestions.take(3).forEachIndexed { index, suggestion ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(suggestion.title)
                                Text(suggestion.text, style = MaterialTheme.typography.labelSmall)
                                if (suggestion.action != null) Text("点按一键应用", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                        },
                        enabled = suggestion.action != null,
                        onClick = { onApplyParameterSuggestion(suggestion) },
                        modifier = Modifier.testTag("parameter_suggestion_$index"),
                    )
                }
            }
            DropdownMenuItem(
                text = {
                    Text(
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && ui.creativeStyle != CreativeStyle.ORIGINAL) {
                            "当前系统仅显示原图预览；仍可另存效果副本，原片始终保留"
                        } else {
                            "预览为近似效果；导出可能有细微差异，原片始终保留"
                        },
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
                enabled = false,
                onClick = {},
            )
        }
    }
}

@Composable
private fun StyleMenuSection(
    title: String,
    styles: List<CreativeStyle>,
    ui: ViewfinderUi,
    tagPrefix: String,
    onStyleChange: (CreativeStyle) -> Unit,
) {
    Text(
        title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
    )
    if (styles.isEmpty()) {
        Text(
            "暂无",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        )
        return
    }
    styles.distinct().filterNot { it == CreativeStyle.ORIGINAL }.forEach { style ->
        val recommendation = ui.styleDiscovery.recommendations.firstOrNull { it.style == style }
        val detail = when {
            recommendation != null -> "建议 ${(recommendation.suggestedStrength * 100).roundToInt()}% · ${recommendation.reason}"
            tagPrefix == "recent" -> "最近使用"
            tagPrefix == "favorite" -> "已收藏"
            else -> style.description
        }
        StyleMenuItem(
            style = style,
            selected = ui.creativeStyle,
            detail = detail,
            tag = if (tagPrefix == "all") {
                "creative_style_${style.name.lowercase()}"
            } else {
                "creative_style_${tagPrefix}_${style.name.lowercase()}"
            },
            onStyleChange = onStyleChange,
        )
    }
}

@Composable
private fun StyleMenuItem(
    style: CreativeStyle,
    selected: CreativeStyle,
    detail: String,
    tag: String,
    onStyleChange: (CreativeStyle) -> Unit,
) {
    DropdownMenuItem(
        text = {
            Column {
                Text(style.label)
                Text(detail, style = MaterialTheme.typography.labelSmall)
            }
        },
        leadingIcon = { Checkbox(checked = style == selected, onCheckedChange = null) },
        onClick = { onStyleChange(style) },
        modifier = Modifier.testTag(tag),
    )
}

@Composable
private fun CreativeStyleControl(
    selected: CreativeStyle,
    enabled: Boolean,
    onStyleChange: (CreativeStyle) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        FilledTonalButton(
            onClick = { expanded = true },
            enabled = enabled,
            modifier = Modifier.height(48.dp).testTag("creative_style"),
        ) { Text(selected.label) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            CreativeStyle.entries.forEach { style ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(style.label)
                            Text(style.description, style = MaterialTheme.typography.labelSmall)
                        }
                    },
                    onClick = {
                        onStyleChange(style)
                        expanded = false
                    },
                    modifier = Modifier.testTag("creative_style_${style.name.lowercase()}"),
                )
            }
        }
    }
}

@Composable
private fun CreativeResultPanel(
    ui: ViewfinderUi,
    result: CreativeResultUi,
    onSelectPhoto: (String) -> Unit,
    onStyleChange: (CreativeStyle) -> Unit,
    onEdit: (EditAdjustment) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onReset: () -> Unit,
    onCompareOriginal: (Boolean) -> Unit,
    onSaveCopy: () -> Unit,
    onOpenPhoto: (String) -> Unit,
    onSharePhoto: (String) -> Unit,
    onFavoritePhoto: (String) -> Unit,
    onTrashPhoto: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier,
) {
    Box(modifier.background(Color.Black.copy(alpha = 0.92f)).testTag("creative_result")) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .heightIn(max = 760.dp)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(if (result.isBurst) "三张都已保留" else "原片已保留", style = MaterialTheme.typography.titleLarge)
                    Text(
                        if (result.isBurst) "推荐第 ${result.photos.first { it.id == result.recommendedId }.sequence} 张：${result.recommendationReason}；你可以改选"
                        else "可撤销轻编辑，只会另存副本",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                TextButton(onClick = onDismiss, modifier = Modifier.height(48.dp).testTag("creative_done")) { Text("完成") }
            }
            EditedPhotoPreview(
                uri = result.selectedPhoto.originalUri,
                style = if (result.compareOriginal) CreativeStyle.ORIGINAL else ui.creativeStyle,
                edit = if (result.compareOriginal) EditAdjustment(styleStrength = 0f) else result.edit,
                modifier = Modifier.fillMaxWidth().height(220.dp),
            )
            if (result.photos.size > 1) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    result.photos.forEach { photo ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onSelectPhoto(photo.id) }
                                .border(
                                    2.dp,
                                    if (photo.id == result.selectedId) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    RoundedCornerShape(8.dp),
                                )
                                .padding(4.dp)
                                .testTag("burst_photo_${photo.sequence}"),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            ResultThumbnail(photo.displayUri, Modifier.fillMaxWidth().height(72.dp))
                            Text(
                                buildString {
                                    append("第 ${photo.sequence} 张")
                                    if (photo.id == result.recommendedId) append(" · 推荐")
                                },
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("风格", modifier = Modifier.width(56.dp))
                CreativeStyleControl(ui.creativeStyle, enabled = !result.exportInProgress, onStyleChange = onStyleChange)
                Spacer(Modifier.width(8.dp))
                Text("预览为近似效果，导出可能有细微差异", style = MaterialTheme.typography.labelSmall)
            }
            EditSlider("曝光", result.edit.exposureStops, EditAdjustment.MIN_EXPOSURE..EditAdjustment.MAX_EXPOSURE) {
                onEdit(result.edit.copy(exposureStops = it))
            }
            EditSlider("对比度", result.edit.contrast, EditAdjustment.MIN_CONTRAST..EditAdjustment.MAX_CONTRAST) {
                onEdit(result.edit.copy(contrast = it))
            }
            EditSlider("饱和度", result.edit.saturation, EditAdjustment.MIN_SATURATION..EditAdjustment.MAX_SATURATION) {
                onEdit(result.edit.copy(saturation = it))
            }
            EditSlider("色温", result.edit.temperature, EditAdjustment.MIN_TEMPERATURE..EditAdjustment.MAX_TEMPERATURE) {
                onEdit(result.edit.copy(temperature = it))
            }
            EditSlider("色调", result.edit.tint, EditAdjustment.MIN_TINT..EditAdjustment.MAX_TINT) {
                onEdit(result.edit.copy(tint = it))
            }
            EditSlider("褪色", result.edit.fade, EditAdjustment.MIN_FADE..EditAdjustment.MAX_FADE) {
                onEdit(result.edit.copy(fade = it))
            }
            EditSlider("强度", result.edit.styleStrength, 0f..1f) {
                onEdit(result.edit.copy(styleStrength = it))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onUndo, enabled = result.canUndo, modifier = Modifier.height(48.dp).testTag("creative_undo")) {
                    Text("撤销")
                }
                TextButton(onClick = onRedo, enabled = result.canRedo, modifier = Modifier.height(48.dp).testTag("creative_redo")) {
                    Text("重做")
                }
                TextButton(onClick = onReset, enabled = result.canReset, modifier = Modifier.height(48.dp).testTag("creative_reset")) {
                    Text("重置")
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(
                    onClick = { onCompareOriginal(!result.compareOriginal) },
                    modifier = Modifier.height(48.dp).testTag("creative_compare_original"),
                ) { Text(if (result.compareOriginal) "查看效果" else "对比原图") }
                Button(
                    onClick = onSaveCopy,
                    enabled = !result.exportInProgress,
                    modifier = Modifier.weight(1f).height(48.dp).testTag("creative_save_copy"),
                ) { Text(if (result.exportInProgress) "正在另存" else "另存副本") }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                val uri = result.selectedPhoto.displayUri
                TextButton(onClick = { onOpenPhoto(uri) }, modifier = Modifier.height(48.dp).testTag("post_open")) { Text("打开") }
                TextButton(onClick = { onSharePhoto(uri) }, modifier = Modifier.height(48.dp).testTag("post_share")) { Text("分享") }
                TextButton(onClick = { onFavoritePhoto(uri) }, modifier = Modifier.height(48.dp).testTag("post_favorite")) { Text("收藏") }
                TextButton(onClick = { onTrashPhoto(uri) }, modifier = Modifier.height(48.dp).testTag("post_trash")) { Text("回收站") }
            }
            result.message?.let { Text(it, style = MaterialTheme.typography.bodySmall, modifier = Modifier.testTag("creative_message")) }
        }
    }
}

@Composable
private fun EditSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onValueChange: (Float) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.width(56.dp))
        Slider(value = value, onValueChange = onValueChange, valueRange = range, modifier = Modifier.weight(1f))
        Text(formatSigned(value), modifier = Modifier.width(44.dp))
    }
}

@Composable
private fun EditedPhotoPreview(
    uri: String,
    style: CreativeStyle,
    edit: EditAdjustment,
    modifier: Modifier,
) {
    BoundedLocalImage(
        uri = uri,
        maximumPixels = ImageDecodePolicy.RESULT_PREVIEW_MAX_PIXELS,
        scaleType = ImageView.ScaleType.CENTER_INSIDE,
        contentDescription = "所选原片的编辑预览",
        colorMatrix = CreativeColorMatrix.forSelection(style, edit),
        errorLabel = "原片预览不可用",
        errorTag = "creative_preview_error",
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.DarkGray)
            .testTag("creative_edit_preview"),
    )
}

@Composable
private fun ResultThumbnail(uri: String, modifier: Modifier) {
    BoundedLocalImage(
        uri = uri,
        maximumPixels = ImageDecodePolicy.THUMBNAIL_MAX_PIXELS,
        scaleType = ImageView.ScaleType.CENTER_CROP,
        contentDescription = "连拍照片缩略图",
        errorLabel = "缩略图不可用",
        errorTag = "burst_thumbnail_error",
        modifier = modifier.clip(RoundedCornerShape(6.dp)).background(Color.DarkGray),
    )
}

@Composable
private fun BoundedLocalImage(
    uri: String,
    maximumPixels: Long,
    scaleType: ImageView.ScaleType,
    contentDescription: String,
    errorLabel: String,
    errorTag: String,
    modifier: Modifier,
    colorMatrix: FloatArray? = null,
) {
    var loadError by remember(uri, maximumPixels) { mutableStateOf<String?>(null) }
    var effectError by remember(uri) { mutableStateOf<String?>(null) }
    val imageControllers = remember { IdentityHashMap<ImageView, BoundedBitmapImageController>() }
    Box(modifier, contentAlignment = Alignment.Center) {
        AndroidView(
            factory = { context ->
                ImageView(context).also { image ->
                    imageControllers[image] = BoundedBitmapImageController(image)
                }
            },
            modifier = Modifier.fillMaxSize(),
            onReset = { image -> imageControllers[image]?.releaseImage() },
            onRelease = { image -> imageControllers.remove(image)?.releaseImage() },
            update = { image ->
                image.scaleType = scaleType
                image.contentDescription = contentDescription
                imageControllers.getValue(image).loadBounded(uri.toUri(), maximumPixels) { loadError = it }
                effectError = applyImageColorMatrix(image, colorMatrix)
            },
        )
        val visibleError = effectError ?: loadError
        visibleError?.let {
            Text(
                text = if (effectError != null) "效果不可用，已显示原图" else errorLabel,
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.78f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 4.dp)
                    .testTag(errorTag),
            )
        }
    }
}

private fun applyImageColorMatrix(image: ImageView, matrix: FloatArray?): String? = try {
    if (matrix == null || CreativeColorMatrix.isIdentity(matrix)) {
        image.clearColorFilter()
    } else {
        image.colorFilter = ColorMatrixColorFilter(AndroidColorMatrix(matrix))
    }
    null
} catch (error: Throwable) {
    runCatching { image.clearColorFilter() }
    error.message ?: "无法显示创意效果"
}

private fun applyPreviewStyle(preview: PreviewView, style: CreativeStyle, strength: Float): String? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        return if (style == CreativeStyle.ORIGINAL) null else "当前系统仅显示原图预览"
    }
    val matrix = CreativeColorMatrix.forSelection(style, EditAdjustment(styleStrength = strength))
    val applied = applyEffectWithOriginalFallback(
        applyEffect = {
            preview.setRenderEffect(
                if (CreativeColorMatrix.isIdentity(matrix)) null
                else RenderEffect.createColorFilterEffect(ColorMatrixColorFilter(AndroidColorMatrix(matrix))),
            )
        },
        clearEffect = { preview.setRenderEffect(null) },
    )
    return if (applied) null else "创意预览效果不可用，已显示原图"
}

internal fun applyEffectWithOriginalFallback(
    applyEffect: () -> Unit,
    clearEffect: () -> Unit,
): Boolean = try {
    applyEffect()
    true
} catch (error: Throwable) {
    runCatching(clearEffect)
    false
}

@Composable
private fun FocusOverlay(ui: ViewfinderUi) {
    val indicator = ui.focusIndicator ?: return
    val color = when (indicator.status) {
        FocusStatus.FOCUSING -> Color.White
        FocusStatus.SUCCESS -> Color(0xFF8FE388)
        FocusStatus.FAILED -> Color(0xFFFFC857)
    }
    Canvas(Modifier.fillMaxSize()) {
        drawCircle(color = color, radius = 34f, center = androidx.compose.ui.geometry.Offset(indicator.x, indicator.y), style = Stroke(3f))
    }
}

@Composable
private fun ErrorRecovery(
    message: String,
    onRetry: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier,
) {
    Column(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.86f), RoundedCornerShape(12.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(message, color = Color.White)
        Button(onClick = onRetry, modifier = Modifier.testTag("camera_retry")) { Text("重试开相机") }
        TextButton(onClick = onSettings, modifier = Modifier.testTag("camera_settings")) { Text("去系统设置") }
    }
}

@Composable
private fun SmallControl(text: String, tag: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.height(48.dp).testTag(tag)) { Text(text, color = Color.White) }
}

@Composable
private fun PromptSettingsControl(
    ui: ViewfinderUi,
    onVoiceEnabledChange: (Boolean) -> Unit,
    onSubjectCaptionsEnabledChange: (Boolean) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        SmallControl(
            text = promptSummary(ui),
            tag = "prompt_settings",
            onClick = { expanded = true },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(280.dp),
        ) {
            DropdownMenuItem(
                text = {
                    Column {
                        Text("中文语音")
                        Text("朗读当前拍摄者或被拍者动作", style = MaterialTheme.typography.labelSmall)
                    }
                },
                leadingIcon = {
                    Checkbox(
                        checked = ui.voiceEnabled,
                        onCheckedChange = onVoiceEnabledChange,
                        modifier = Modifier.testTag("voice_toggle"),
                    )
                },
                onClick = { onVoiceEnabledChange(!ui.voiceEnabled) },
            )
            DropdownMenuItem(
                text = {
                    Column {
                        Text("被拍者字幕")
                        Text("拍摄者动作卡始终保留", style = MaterialTheme.typography.labelSmall)
                    }
                },
                leadingIcon = {
                    Checkbox(
                        checked = ui.subjectCaptionsEnabled,
                        onCheckedChange = onSubjectCaptionsEnabledChange,
                        modifier = Modifier.testTag("subject_captions_toggle"),
                    )
                },
                onClick = { onSubjectCaptionsEnabledChange(!ui.subjectCaptionsEnabled) },
            )
        }
    }
}

@Composable
private fun CameraSettingsControl(
    ui: ViewfinderUi,
    onGridEnabledChange: (Boolean) -> Unit,
    onLevelEnabledChange: (Boolean) -> Unit,
    onTimerChange: (CaptureTimer) -> Unit,
    onAspectRatioChange: (CaptureAspectRatio) -> Unit,
    onCapturePriorityChange: (CapturePriority) -> Unit,
    onModePreferenceChange: (CameraModePreference) -> Unit,
    onResetSettings: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val modeOptions = cameraModeOptions(ui)
    Box {
        SmallControl("设置·${modeLabel(ui.activeMode)}", "camera_settings_menu", onClick = { expanded = true })
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(320.dp),
        ) {
            ToggleSettingItem("三分网格", ui.gridEnabled, "grid_toggle", onGridEnabledChange)
            ToggleSettingItem("水平仪", ui.levelEnabled, "level_toggle", onLevelEnabledChange)
            DropdownMenuItem(
                text = { SettingText("倒计时", ui.captureTimer.label) },
                onClick = {
                    onTimerChange(nextValue(ui.captureTimer, CaptureTimer.entries))
                },
                modifier = Modifier.testTag("timer_setting"),
            )
            DropdownMenuItem(
                text = { SettingText("画幅", ui.aspectRatio.label) },
                onClick = {
                    onAspectRatioChange(nextValue(ui.aspectRatio, CaptureAspectRatio.entries))
                },
                modifier = Modifier.testTag("aspect_ratio_setting"),
            )
            DropdownMenuItem(
                text = { SettingText("按快门时", ui.capturePriority.label) },
                onClick = {
                    onCapturePriorityChange(nextValue(ui.capturePriority, CapturePriority.entries))
                },
                modifier = Modifier.testTag("capture_priority_setting"),
            )
            DropdownMenuItem(
                text = { SettingText("拍摄模式", ui.modePreference.label) },
                onClick = {
                    onModePreferenceChange(nextValue(ui.modePreference, modeOptions))
                },
                modifier = Modifier.testTag("camera_mode_setting"),
            )
            DropdownMenuItem(
                text = {
                    Column {
                        Text("当前相机能力")
                        Text(capabilitySummary(ui), style = MaterialTheme.typography.labelSmall)
                    }
                },
                enabled = false,
                onClick = {},
                modifier = Modifier.testTag("camera_capability_summary"),
            )
            DropdownMenuItem(
                text = { Text("一键恢复默认设置") },
                onClick = {
                    onResetSettings()
                    expanded = false
                },
                modifier = Modifier.testTag("reset_camera_settings"),
            )
        }
    }
}

@Composable
private fun ToggleSettingItem(
    title: String,
    checked: Boolean,
    tag: String,
    onCheckedChange: (Boolean) -> Unit,
) {
    DropdownMenuItem(
        text = { Text(title) },
        leadingIcon = {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.testTag(tag),
            )
        },
        onClick = { onCheckedChange(!checked) },
    )
}

@Composable
private fun SettingText(title: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title)
        Text(value, style = MaterialTheme.typography.labelMedium)
    }
}

private fun cameraModeOptions(ui: ViewfinderUi): List<CameraModePreference> = buildList {
    add(CameraModePreference.AUTO)
    CameraModePreference.entries.forEach { preference ->
        val requested = preference.requestedMode
        if (requested != null && requested in ui.availableModes) add(preference)
    }
}

private fun modeLabel(mode: SuggestedMode): String = when (mode) {
    SuggestedMode.PHOTO -> "普通"
    SuggestedMode.PORTRAIT -> "人像"
    SuggestedMode.HDR -> "HDR"
    SuggestedMode.NIGHT -> "夜景"
}

private fun <T> nextValue(current: T, values: List<T>): T {
    if (values.isEmpty()) return current
    val index = values.indexOf(current)
    return values[(index + 1).mod(values.size)]
}

private fun capabilitySummary(ui: ViewfinderUi): String {
    val focal = ui.focalPresets
        .filter { it.isQuickControlAvailable }
        .joinToString(" / ") { it.label }
        .ifBlank { "仅标准后摄" }
    val zoom = if (ui.zoomCapability.supported) {
        "连续变焦 ${formatOneDecimal(ui.zoomCapability.minimumRatio)}–${formatOneDecimal(ui.zoomCapability.maximumRatio)}×"
    } else {
        "无连续变焦"
    }
    val exposure = if (ui.exposureCapability.supported) {
        "曝光 ${formatSigned(ui.exposureCapability.minimumStops)} 到 ${formatSigned(ui.exposureCapability.maximumStops)}"
    } else {
        "曝光不可调"
    }
    return "焦段 $focal；$zoom；$exposure"
}

private fun formatOneDecimal(value: Float): String =
    ((value * 10f).roundToInt() / 10f).toString()

private fun formatSigned(value: Float): String {
    val text = formatOneDecimal(value)
    return if (value > 0f) "+$text" else text
}

@Composable
private fun IntentButton(text: String, selected: Boolean, tag: String, onClick: () -> Unit) {
    if (selected) {
        FilledTonalButton(onClick = onClick, modifier = Modifier.height(48.dp).testTag(tag)) { Text(text) }
    } else {
        TextButton(onClick = onClick, modifier = Modifier.height(48.dp).testTag(tag)) { Text(text, color = Color.White) }
    }
}

@Composable
private fun ZoomButton(text: String, selected: Boolean, tag: String, onClick: () -> Unit) {
    if (selected) {
        FilledTonalButton(onClick = onClick, modifier = Modifier.height(48.dp).testTag(tag)) { Text(text) }
    } else {
        TextButton(onClick = onClick, modifier = Modifier.height(48.dp).testTag(tag)) { Text(text) }
    }
}

private fun guidanceLabel(ui: ViewfinderUi): String = guidanceStageLabel(ui, ui.guidance.stage)

private fun guidanceStageLabel(ui: ViewfinderUi, stage: GuidanceStage): String {
    if (hasActiveLensWarning(ui)) return "请检查"
    return when (stage) {
        is GuidanceStage.Action -> ui.guidance.stepLabel.orEmpty()
        is GuidanceStage.Ready -> if (
            ui.subjectCaptionsEnabled && stage.retainedSubjectCue != null
        ) {
            "可以拍了"
        } else {
            "就绪"
        }
        is GuidanceStage.Optional -> "优化"
        is GuidanceStage.Saved -> "✓"
        else -> ""
    }
}

private fun guidanceText(ui: ViewfinderUi): String {
    if (hasActiveLensWarning(ui)) return checkNotNull(ui.lensWarning)
    return when (val stage = ui.guidance.stage) {
        GuidanceStage.Initializing -> "正在准备相机"
        is GuidanceStage.Observing -> "正在观察画面"
        is GuidanceStage.Action -> promptText(stage.cue.audience, stage.cue.text, ui.subjectCaptionsEnabled)
        is GuidanceStage.Ready -> when {
            ui.poseCueText != null -> ui.poseCueText
            ui.subjectCaptionsEnabled && stage.retainedSubjectCue != null ->
                checkNotNull(stage.retainedSubjectCue).text
            ui.guidance.canRequestOptional -> "发现新建议，可再优化"
            else -> "可以拍了"
        }
        is GuidanceStage.Optional -> promptText(stage.cue.audience, stage.cue.text, ui.subjectCaptionsEnabled)
        GuidanceStage.Capturing -> ui.saveStatusText ?: "正在捕获并保存"
        is GuidanceStage.Saved -> "已保存到系统相册"
        is GuidanceStage.SaveFailed -> stage.message
    }
}

private fun PoseCategory.poseLabel(): String = when (this) {
    PoseCategory.CLOSE_UP -> "特写"
    PoseCategory.HALF_BODY -> "半身"
    PoseCategory.FULL_BODY -> "全身"
    PoseCategory.SEATED -> "坐姿"
    PoseCategory.WALKING -> "走动"
    PoseCategory.SOLO_INTERACTION -> "单人互动"
}

private fun hasActiveLensWarning(ui: ViewfinderUi): Boolean =
    ui.lensWarning != null &&
        ui.guidance.stage !is GuidanceStage.Capturing &&
        ui.guidance.stage !is GuidanceStage.Saved &&
        ui.guidance.stage !is GuidanceStage.SaveFailed

private fun promptText(audience: Audience, text: String, subjectCaptionsEnabled: Boolean): String =
    if (audience == Audience.SUBJECT && !subjectCaptionsEnabled) "中文口令播放中" else text

private fun promptSummary(ui: ViewfinderUi): String = when {
    ui.ttsFailed && ui.voiceEnabled -> "语音不可用"
    ui.voiceEnabled && ui.subjectCaptionsEnabled -> "语音+字幕"
    ui.voiceEnabled -> "仅语音"
    ui.subjectCaptionsEnabled -> "仅字幕"
    else -> "提示关闭"
}

private const val FOCUS_CONTROLS_DURATION_MS = 4_000L
private const val CONTROL_MESSAGE_DURATION_MS = 2_000L
private const val OPERATION_PANEL_MAX_HEIGHT_FRACTION = 0.20f
private val PORTRAIT_DOCK_CLEARANCE = 156.dp
