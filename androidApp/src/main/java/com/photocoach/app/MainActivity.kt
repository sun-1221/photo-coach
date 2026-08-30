package com.photocoach.app

import android.Manifest
import android.content.Intent
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.view.KeyEvent
import androidx.core.view.doOnLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.IntentSenderRequest
import androidx.activity.viewModels
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.photocoach.app.camera.CameraBinder
import com.photocoach.app.camera.CameraModePreference
import com.photocoach.app.camera.CapturePriority
import com.photocoach.app.camera.CaptureSpec
import com.photocoach.app.creative.ParameterAction
import com.photocoach.app.creative.ParameterSuggestion
import com.photocoach.app.tts.GuidanceTts
import com.photocoach.app.ui.consent.CameraConsentScreen
import com.photocoach.app.ui.theme.PhotoCoachTheme
import com.photocoach.app.ui.viewfinder.PermissionDeniedScreen
import com.photocoach.app.ui.viewfinder.ViewfinderScreen
import com.photocoach.coach.GuidanceStage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()
    private lateinit var camera: CameraBinder
    private lateinit var tts: GuidanceTts
    private var previewView: PreviewView? = null
    private var cameraConsented by mutableStateOf(false)
    private var permissionState by mutableStateOf(PermissionState.UNKNOWN)
    private var countdownJob: Job? = null
    private val captureRequestGate = CaptureRequestGate()
    private var rebindJob: Job? = null
    private val trashLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        viewModel.showControlMessage(if (result.resultCode == RESULT_OK) "已移到系统回收站" else "未移动照片")
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        permissionState = if (granted) PermissionState.GRANTED else PermissionState.DENIED
        if (granted) prepareAndRebind()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        camera = CameraBinder(this)
        tts = GuidanceTts(this)
        cameraConsented = prefs().getBoolean(PREF_CAMERA_CONSENT, false)
        if (cameraConsented) requestCameraPermission()

        setContent {
            PhotoCoachTheme {
                val ui by viewModel.ui.collectAsStateWithLifecycle()
                LaunchedEffect(
                    ui.guidance.stage,
                    ui.voiceEnabled,
                    ui.subjectCaptionsEnabled,
                ) {
                    val activePrompt = when (ui.guidance.stage) {
                        is GuidanceStage.Action,
                        is GuidanceStage.Optional,
                        -> true
                        else -> false
                    }
                    if (!activePrompt) {
                        tts.stop()
                        return@LaunchedEffect
                    }
                    if (!ui.voiceEnabled) {
                        tts.stop()
                        viewModel.onPlaybackUnavailable()
                        return@LaunchedEffect
                    }
                    val cue = viewModel.cueForSpeech()
                    if (cue == null) return@LaunchedEffect
                    viewModel.onPlaybackStarting()
                    tts.speak(
                        cue = cue,
                        muted = false,
                        onUnavailable = viewModel::onPlaybackUnavailable,
                        onFailure = viewModel::markTtsFailed,
                        onFinished = viewModel::onPlaybackFinished,
                    )
                }
                LaunchedEffect(ui.sceneApply?.generation) {
                    ui.sceneApply?.let(::applySceneStart)
                }
                LaunchedEffect(ui.flashSetting) {
                    camera.setFlash(ui.flashSetting)
                }
                LaunchedEffect(ui.aspectRatio, ui.capturePriority, ui.modePreference, ui.livePhotoEnabled) {
                    if (permissionState == PermissionState.GRANTED) prepareAndRebind()
                }
                LaunchedEffect(ui.guidance.stage, ui.creativeResultVisible, ui.cameraError) {
                    if (captureAdmissionState().blocked) cancelScheduledCapture()
                }

                when {
                    !cameraConsented -> CameraConsentScreen(
                        deniedOnce = false,
                        onAgree = {
                            cameraConsented = true
                            prefs().edit { putBoolean(PREF_CAMERA_CONSENT, true) }
                            requestCameraPermission()
                        },
                        onDeny = { finish() },
                    )
                    permissionState == PermissionState.UNKNOWN -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("正在等待相机权限")
                    }
                    permissionState == PermissionState.DENIED -> PermissionDeniedScreen(
                        onRetry = ::requestCameraPermission,
                        onSettings = ::openAppSettings,
                    )
                    else -> ViewfinderScreen(
                        ui = ui,
                        tiltDegrees = viewModel.tiltDegrees,
                        onPreviewReady = ::bindPreview,
                        onTapFocus = { x, y, lock ->
                            viewModel.onFocusStarted(x, y, lock)
                            camera.tapToFocus(x, y, lock, viewModel::onFocusResult)
                        },
                        onEv = {
                            viewModel.applyUserEv(it)
                            viewModel.onExposureApplied(camera.setExposure(viewModel.currentEv()))
                        },
                        onSetFocal = ::setUserFocal,
                        onZoomBy = { zoomBy(it) },
                        onVoiceEnabledChange = { enabled ->
                            viewModel.setVoiceEnabled(enabled)
                        },
                        onSubjectCaptionsEnabledChange = { enabled ->
                            viewModel.setSubjectCaptionsEnabled(enabled)
                        },
                        onGridEnabledChange = viewModel::setGridEnabled,
                        onLevelEnabledChange = viewModel::setLevelEnabled,
                        onTimerChange = viewModel::setCaptureTimer,
                        onAspectRatioChange = viewModel::setAspectRatio,
                        onCapturePriorityChange = viewModel::setCapturePriority,
                        onModePreferenceChange = viewModel::setModePreference,
                        onResetSettings = {
                            cancelScheduledCapture()
                            viewModel.resetCameraSettings()
                        },
                        onUnlockFocus = {
                            camera.unlockAeAf()
                            viewModel.onFocusUnlocked()
                        },
                        onToggleFlash = viewModel::toggleFlash,
                        onSelectIntent = viewModel::selectIntent,
                        onSkip = viewModel::skip,
                        onOptional = viewModel::requestOptional,
                        onCapture = ::capture,
                        onOpenRecentPhoto = ::openRecentPhoto,
                        onRetrySave = ::retrySave,
                        onDiscardSave = {
                            camera.discardPending()
                            viewModel.abandonSaveFailure()
                        },
                        onRetryCamera = ::prepareAndRebind,
                        onOpenSettings = ::openAppSettings,
                        onExit = { finish() },
                        onHideFocusControls = viewModel::hideFocusControls,
                        onDismissControlMessage = viewModel::dismissControlMessage,
                        onCreativeStyleChange = viewModel::setCreativeStyle,
                        onThreeShotBurstChange = viewModel::setThreeShotBurstEnabled,
                        onSaveStrategyChange = viewModel::setSaveStrategy,
                        onDerivativeQualityChange = viewModel::setDerivativeQuality,
                        onLivePhotoChange = viewModel::setLivePhotoEnabled,
                        onApplyParameterSuggestion = ::applyParameterSuggestion,
                        onOpenCreativeResult = viewModel::openCreativeResult,
                        onSelectCreativePhoto = viewModel::selectCreativePhoto,
                        onCreativeEdit = viewModel::updateCreativeEdit,
                        onUndoCreativeEdit = viewModel::undoCreativeEdit,
                        onRedoCreativeEdit = viewModel::redoCreativeEdit,
                        onResetCreativeEdit = viewModel::resetCreativeEdit,
                        onCompareOriginal = viewModel::setCompareOriginal,
                        onSaveCreativeCopy = ::saveCreativeCopy,
                        onOpenPhoto = ::openRecentPhoto,
                        onSharePhoto = ::sharePhoto,
                        onFavoritePhoto = ::favoritePhoto,
                        onTrashPhoto = ::trashPhoto,
                        onDismissCreativeResult = ::dismissCreativeResult,
                    )
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (cameraConsented && permissionState == PermissionState.GRANTED) capture()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onResume() {
        super.onResume()
        if (!cameraConsented) return
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            permissionState = PermissionState.GRANTED
            prepareAndRebind()
        } else if (permissionState == PermissionState.GRANTED) {
            permissionState = PermissionState.DENIED
            viewModel.markCameraError(getString(R.string.permission_denied_title))
        }
    }

    override fun onDestroy() {
        cancelScheduledCapture()
        rebindJob?.cancel()
        camera.release()
        tts.shutdown()
        super.onDestroy()
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            permissionState = PermissionState.GRANTED
            prepareAndRebind()
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun bindPreview(view: PreviewView) {
        previewView = view
        if (permissionState != PermissionState.GRANTED) return
        view.doOnLayout {
            if (it.width > 0 && it.height > 0) prepareAndRebind()
        }
    }

    private fun prepareAndRebind() {
        val view = previewView ?: return
        rebindJob?.cancel()
        rebindJob = lifecycleScope.launch {
            try {
                camera.prepare()
                val capabilities = camera.bind(
                    owner = this@MainActivity,
                    previewView = view,
                    requestedMode = viewModel.requestedMode(),
                    settings = viewModel.cameraSettings(),
                    extras = { viewModel.extras(camera.hasTelephotoPreset()) },
                    onSignals = viewModel::onFrame,
                    onLiveFallback = viewModel::onLiveFallback,
                    onError = { viewModel.markCameraError(getString(R.string.camera_busy)) },
                ) ?: return@launch
                camera.setFlash(viewModel.ui.value.flashSetting)
                viewModel.onExposureApplied(camera.setExposure(viewModel.currentEv()))
                viewModel.onCameraReady(capabilities)
            } catch (error: Exception) {
                viewModel.markCameraError(error.message ?: getString(R.string.camera_busy))
            }
        }
    }

    private fun setUserFocal(cameraId: String) {
        val preset = camera.setFocalPreset(cameraId) ?: return
        viewModel.onUserFocalChanged(preset)
        prepareAndRebind()
    }

    internal fun zoomBy(scaleFactor: Float): Float? {
        val ratio = camera.zoomBy(scaleFactor) ?: return null
        viewModel.onUserPinchZoomChanged(ratio)
        return ratio
    }

    private fun capture() {
        if (captureRequestGate.hasPendingRequest) {
            cancelScheduledCapture("已取消倒计时")
            return
        }
        val token = captureRequestGate.trySchedule(captureAdmissionState()) ?: return
        val seconds = viewModel.ui.value.captureTimer.seconds
        if (seconds <= 0) {
            executeScheduledCapture(token)
            return
        }
        countdownJob = lifecycleScope.launch {
            try {
                for (remaining in seconds downTo 1) {
                    viewModel.setCountdown(remaining)
                    delay(1_000)
                }
                viewModel.setCountdown(null)
                executeScheduledCapture(token)
            } finally {
                viewModel.setCountdown(null)
                countdownJob = null
            }
        }
    }

    private fun executeScheduledCapture(token: Long) {
        if (!captureRequestGate.tryExecute(token, captureAdmissionState())) return
        captureNow()
    }

    private fun cancelScheduledCapture(message: String? = null) {
        val cancelled = captureRequestGate.cancelPending()
        countdownJob?.cancel()
        countdownJob = null
        viewModel.setCountdown(null)
        if (cancelled && message != null) viewModel.showControlMessage(message)
    }

    private fun captureAdmissionState(): CaptureAdmissionState {
        val ui = viewModel.ui.value
        return CaptureAdmissionState(
            cameraAvailable = permissionState == PermissionState.GRANTED && ui.cameraError == null,
            shutterEnabled = ui.guidance.shutterEnabled,
            captureInProgress = ui.guidance.stage is GuidanceStage.Capturing,
            saveFailureVisible = ui.guidance.stage is GuidanceStage.SaveFailed,
            resultVisible = ui.creativeResultVisible,
        )
    }

    private fun captureNow() {
        if (!viewModel.beginCapture()) return
        captureNextShot()
    }

    private fun captureNextShot() {
        val spec = viewModel.activeCaptureSpec() ?: run {
            viewModel.onSaveFailed(IllegalStateException("拍摄标识不可用"), retryAvailable = false)
            return
        }
        val ui = viewModel.ui.value
        val face = ui.overlay?.faceRects?.maxByOrNull { it.width() * it.height() }
        if (ui.capturePriority == CapturePriority.FOCUS) {
            camera.prepareQualityCapture(face?.centerX(), face?.centerY()) {
                performCapture(spec)
            }
        } else {
            performCapture(spec)
        }
    }

    private fun performCapture(spec: CaptureSpec) {
        camera.capture(
            spec = spec,
            onSaveProgress = viewModel::onSaveProgress,
            onSaved = { photo ->
                if (viewModel.onPhotoCaptured(photo)) captureNextShot()
            },
            onSaveError = { viewModel.onSaveFailed(it, retryAvailable = true) },
            onCaptureError = { viewModel.onSaveFailed(it, retryAvailable = false) },
        )
    }

    private fun retrySave() {
        if (!viewModel.beginRetrySave()) return
        val started = camera.retrySave(
            onSaved = { photo ->
                if (viewModel.onPhotoCaptured(photo)) captureNextShot()
            },
            onSaveError = { viewModel.onSaveFailed(it, retryAvailable = true) },
        )
        if (!started) viewModel.onSaveFailed(IllegalStateException("待保存照片不可用"), retryAvailable = false)
    }

    private fun saveCreativeCopy() {
        viewModel.currentEditRecipe()?.let { recipe ->
            camera.saveEditRecipe(recipe) { viewModel.showControlMessage("编辑配方保存失败；原片未改动") }
        }
        val request = viewModel.beginCreativeExport() ?: return
        camera.exportEditedCopy(
            source = request.source,
            style = viewModel.ui.value.creativeStyle,
            edit = request.edit,
            quality = viewModel.ui.value.derivativeQuality,
            captureId = request.captureId,
            sequence = request.sequence,
            takenAtMillis = request.takenAtMillis,
            onSaved = viewModel::onCreativeExported,
            onError = viewModel::onCreativeExportFailed,
        )
    }

    private fun dismissCreativeResult() {
        viewModel.currentEditRecipe()?.let { recipe ->
            camera.saveEditRecipe(recipe) { viewModel.showControlMessage("编辑配方保存失败；原片未改动") }
        }
        viewModel.dismissCreativeResult()
    }

    private fun applyParameterSuggestion(suggestion: ParameterSuggestion) {
        when (val action = suggestion.action) {
            is ParameterAction.SetEv -> {
                viewModel.applyUserEv(action.stops)
                viewModel.onExposureApplied(camera.setExposure(action.stops))
            }
            is ParameterAction.FocusOnFace -> {
                val face = viewModel.ui.value.overlay?.faceRects?.maxByOrNull { it.width() * it.height() }
                if (face == null) {
                    viewModel.showControlMessage("当前没有可用的人脸对焦点")
                } else {
                    viewModel.onFocusStarted(face.centerX(), face.centerY(), action.lockAfterFocus)
                    camera.tapToFocus(face.centerX(), face.centerY(), action.lockAfterFocus, viewModel::onFocusResult)
                }
            }
            ParameterAction.UnlockAeAf -> {
                camera.unlockAeAf()
                viewModel.onFocusUnlocked()
            }
            is ParameterAction.SetMode -> {
                val preference = CameraModePreference.entries.firstOrNull { it.requestedMode == action.mode }
                    ?: CameraModePreference.PHOTO
                viewModel.setModePreference(preference)
            }
            is ParameterAction.SetFocal -> setUserFocal(action.cameraId)
            is ParameterAction.SetTimer -> viewModel.setCaptureTimer(action.timer)
            is ParameterAction.SetCapturePriority -> viewModel.setCapturePriority(action.priority)
            is ParameterAction.SetBurstEnabled -> viewModel.setThreeShotBurstEnabled(action.enabled)
            is ParameterAction.SetCompositionAids -> {
                viewModel.setGridEnabled(action.grid)
                viewModel.setLevelEnabled(action.level)
            }
            is ParameterAction.SetAspectRatio -> viewModel.setAspectRatio(action.ratio)
            is ParameterAction.SetSaveStrategy -> viewModel.setSaveStrategy(action.strategy)
            null -> viewModel.showControlMessage(suggestion.actionText)
        }
    }

    private fun applySceneStart(request: SceneApplyRequest) {
        if (permissionState != PermissionState.GRANTED) return
        val preset = request.preferTelephoto?.let(camera::setTelephotoEnabled)
        val appliedEv = request.evStops?.let(camera::setExposure)
        camera.setFlash(viewModel.ui.value.flashSetting)
        viewModel.onSceneApplied(preset, appliedEv)
        prepareAndRebind()
    }

    private fun openRecentPhoto(uriValue: String) {
        val uri = uriValue.toUri()
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, contentResolver.getType(uri) ?: "image/*")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        runCatching { startActivity(intent) }
            .onFailure { viewModel.showControlMessage("没有可用的图片查看器") }
    }

    private fun sharePhoto(uriValue: String) {
        val uri = uriValue.toUri()
        val intent = Intent(Intent.ACTION_SEND)
            .setType(contentResolver.getType(uri) ?: "image/jpeg")
            .putExtra(Intent.EXTRA_STREAM, uri)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        runCatching { startActivity(Intent.createChooser(intent, "分享照片")) }
            .onFailure { viewModel.showControlMessage("没有可用的分享应用") }
    }

    private fun favoritePhoto(uriValue: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            viewModel.showControlMessage("当前系统不支持相册收藏标记")
            return
        }
        val updated = runCatching {
            contentResolver.update(
                uriValue.toUri(),
                ContentValues().apply { put(MediaStore.MediaColumns.IS_FAVORITE, 1) },
                null,
                null,
            )
        }.getOrDefault(0)
        viewModel.showControlMessage(if (updated == 1) "已标记为收藏" else "系统相册未接受收藏标记")
    }

    private fun trashPhoto(uriValue: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            viewModel.showControlMessage("当前系统不支持移到系统回收站")
            return
        }
        runCatching {
            val request = MediaStore.createTrashRequest(contentResolver, listOf(uriValue.toUri()), true)
            trashLauncher.launch(IntentSenderRequest.Builder(request.intentSender).build())
        }.onFailure { viewModel.showControlMessage("无法请求系统回收站") }
    }

    private fun openAppSettings() {
        startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null),
            ),
        )
    }

    private fun prefs() = getSharedPreferences(PREFS, MODE_PRIVATE)
}

private enum class PermissionState { UNKNOWN, GRANTED, DENIED }

private const val PREFS = "photo_coach"
private const val PREF_CAMERA_CONSENT = "camera_consent"
