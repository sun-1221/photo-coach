package com.photocoach.app.camera

import android.content.Context
import com.photocoach.app.beauty.BeautyPreset
import com.photocoach.app.beauty.BeautyCameraEffect
import com.photocoach.app.beauty.BeautyFaceStore
import com.photocoach.app.beauty.BeautyCompatibilityPolicy
import com.photocoach.app.beauty.BeautyPreviewState
import android.net.Uri
import android.os.StatFs
import android.os.SystemClock
import android.util.LayoutDirection
import android.util.Rational
import android.util.Size
import android.view.Surface
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.ViewPort
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.extensions.ExtensionsManager
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.photocoach.app.analysis.AnalyzerExtras
import com.photocoach.app.analysis.CoachAnalyzer
import com.photocoach.app.analysis.OverlayGeometry
import com.photocoach.app.creative.CreativeColorMatrix
import com.photocoach.app.creative.CaptureAssetKind
import com.photocoach.app.creative.CaptureId
import com.photocoach.app.creative.CaptureIdentity
import com.photocoach.app.creative.CreativeImageProcessor
import com.photocoach.app.creative.CreativeStyle
import com.photocoach.app.creative.EditAdjustment
import com.photocoach.app.creative.EditRecipe
import com.photocoach.app.creative.EditRecipeStore
import com.photocoach.app.creative.PhotoQualityScore
import com.photocoach.app.creative.NormalizedFaceRegion
import com.photocoach.coach.Signals
import com.photocoach.coach.SuggestedMode
import java.io.File
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

internal fun captureViewPortDimensions(
    aspectRatio: CaptureAspectRatio,
    targetRotation: Int,
): Pair<Int, Int> {
    val portrait = targetRotation == Surface.ROTATION_0 || targetRotation == Surface.ROTATION_180
    val landscapeDimensions = when (aspectRatio) {
        CaptureAspectRatio.FOUR_THREE -> 4 to 3
        CaptureAspectRatio.SIXTEEN_NINE -> 16 to 9
    }
    return if (portrait) landscapeDimensions.second to landscapeDimensions.first else landscapeDimensions
}

class CameraBinder(
    private val context: Context,
    private val targetFocalVerifier: TargetFocalVerifier = TargetFocalVerifier.NONE,
) {
    private val analysisExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val saveExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val effectExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val savePipeline by lazy { OriginalFirstSavePipeline(saveExecutor, effectExecutor, mainExecutor) }
    private val backgroundLeases = mutableMapOf<String, SaveTransactionRegistry.Lease?>()
    private var captureBudget: java.io.Closeable? = null
    private val pendingSources = PendingSourceStore(File(context.noBackupFilesDir, "pending-captures"))
    private val creativeProcessor = CreativeImageProcessor(File(pendingSources.directory, "processing"))
    private val recipeStore = EditRecipeStore(File(context.filesDir, "edit-recipes"))
    private val journalStore = SaveJournalStore(File(context.filesDir, "save-journal"))
    private val interruptedSaveRecovery = InterruptedSaveRecovery(
        resolver = context.contentResolver,
        motionDirectory = File(context.cacheDir, "motion-recordings"),
        journalStore = journalStore,
        recipeStore = recipeStore,
        creativeProcessor = creativeProcessor,
        pendingSources = pendingSources,
        researchRecoveryStore = com.photocoach.app.research.ResearchRecoveryStore(File(context.noBackupFilesDir,"research-recovery")),
    )
    private val mainExecutor by lazy { ContextCompat.getMainExecutor(context) }
    private val liveRecording = LiveRecordingController(
        context = context,
        mainExecutor = mainExecutor,
        directory = File(context.cacheDir, "motion-recordings"),
        onStartFailure = ::downgradeLive,
        onRecorderReady=::startRollingRecording,
        onFinalized = ::onLiveRecordingFinalized,
    )
    private var provider: ProcessCameraProvider? = null
    private var extensions: ExtensionsManager? = null
    private var discovered = DiscoveredCameras(emptyList(), emptyMap())
    private var selectedFocalId: String? = null
    private var camera: Camera? = null
    private val exposureController = ExposureController()
    private var imageCapture: ImageCapture? = null
    private var sessionCoordinator: CameraSessionCoordinator? = null
    private var livePhotoAvailable = false
    private var standardLiveFallback:(()->Unit)?=null
    private var liveRebindPending=false
    private var liveFallbackReason: String? = null
    private var onLiveFallback: ((String) -> Unit)? = null
    private var analyzer: CoachAnalyzer? = null
    private var previewView: PreviewView? = null
    private var pendingCapture: PendingCapture? = null
    private var captureInProgress = false
    private var capturedSensorTimestampNs: Long? = null
    private data class CompletedLiveOutput(val file:File,val durationUs:Long,val error:Throwable?,val timeline:VerifiedMotionTimeline?,val generation:Long)
    private var awaitingLiveCapture:Pair<String,Long>?=null
    private var earlyLiveOutput:CompletedLiveOutput?=null
    private fun clearEarlyLiveOutput(){earlyLiveOutput?.file?.delete();earlyLiveOutput=null;awaitingLiveCapture=null}
    private var released = false
    private var saveWorkInProgress = false
    private val cameraLocks by lazy { CameraLocks(mainExecutor) }
    var recoveryFailures: List<SaveJournal> = emptyList()
        private set
    private var captureLease: SaveTransactionRegistry.Lease? = null
    private var smoothZoomRatio = 1f
    private val prepareMutex = Mutex()
    @Volatile private var thermalLevel: ThermalLevel = ThermalLevel.UNKNOWN
    @Volatile private var beautyPreset = BeautyPreset.OFF
    private var beautyEffect: BeautyCameraEffect? = null
    private var beautyStore: BeautyFaceStore? = null

    fun updateBeautyPreset(preset: BeautyPreset) { beautyPreset = preset }

    var flashSetting: FlashSetting = FlashSetting.OFF
        private set
    val aeAfLocked: Boolean get() = cameraLocks.state.bothConfirmed
    internal val timestampsRealtime: Boolean get() = cameraLocks.timestampsRealtime

    suspend fun prepare() = prepareMutex.withLock {
        if (provider != null) return
        recoveryFailures = withContext(Dispatchers.IO) { interruptedSaveRecovery.recover() }
        val cameraProvider = awaitProvider()
        provider = cameraProvider
        extensions = runCatching { awaitExtensions(cameraProvider) }.getOrNull()
        discovered = CameraCapabilityDiscovery.discoverRearCameras(cameraProvider, targetFocalVerifier)
        selectedFocalId = QuickFocalPolicy
            .safestSelection(QuickFocalPolicy.quickControls(discovered.presets), selectedFocalId)
            ?.cameraId
    }

    fun bind(
        owner: LifecycleOwner,
        previewView: PreviewView,
        requestedMode: SuggestedMode,
        settings: CameraUserSettings,
        extras: () -> AnalyzerExtras,
        onSignals: (Signals, OverlayGeometry) -> Unit,
        onLiveFallback: (String) -> Unit,
        onError: (Throwable) -> Unit,
        onBeautyFallback: (String) -> Unit = {},
        onBeautyPreviewState: (BeautyPreviewState) -> Unit = {},
        onLockState: (CameraLockState) -> Unit = {},
    ): CameraCapabilities? {
        if (released || captureInProgress || saveWorkInProgress) return null
        cameraLocks.invalidate()
        val cameraProvider = provider ?: run {
            onError(IllegalStateException("camera provider missing"))
            return null
        }
        cameraLocks.onState = onLockState
        cameraLocks.onSensorResult=liveRecording::observe
        this.previewView = previewView
        this.onLiveFallback = onLiveFallback
        stopRollingRecording(deleteFile = true)
        standardLiveFallback=null;liveRebindPending=false
        unbindOwnedUseCases()
        beautyEffect?.close()
        beautyEffect = null
        beautyStore?.clear()
        val beautyRejection = BeautyCompatibilityPolicy.rejection(settings.beautyPreset,
            settings.livePhotoEnabled, settings.modePreference == CameraModePreference.PHOTO)
        val beautyRequested = settings.beautyPreset != BeautyPreset.OFF && beautyRejection == null
        beautyPreset = if (beautyRequested) settings.beautyPreset else BeautyPreset.OFF
        val faceStore = if (beautyRequested) BeautyFaceStore() else null
        beautyStore = faceStore
        if (beautyRejection != null) mainExecutor.execute { onBeautyFallback(beautyRejection) }
        val effect = faceStore?.let { store ->
            BeautyCameraEffect(store, { beautyPreset }, { ThermalPolicy.forLevel(thermalLevel).stylePreviewEnabled },
                onState = { state -> mainExecutor.execute {
                    if (beautyStore === store) onBeautyPreviewState(state)
                } }) {
                mainExecutor.execute {
                    if (beautyStore === store) onBeautyFallback("美颜预览不可用，正在关闭效果；拍摄结束后恢复普通预览")
                }
            }
        }
        beautyEffect = effect
        exposureController.invalidate()
        camera = null
        cameraLocks.invalidate()
        analyzer?.close()

        val selector = resolveSelector()
        val useCaseResolution = resolutionSelector(settings.aspectRatio)
        val previewBuilder = Preview.Builder()
            .setResolutionSelector(useCaseResolution)
        cameraLocks.observe(previewBuilder)
        val preview = previewBuilder.build().also { it.surfaceProvider = previewView.surfaceProvider }
        val captureBuilder = ImageCapture.Builder()
            .setCaptureMode(captureMode(settings.capturePriority))
            .setFlashMode(flashMode(flashSetting))
            .setOutputFormat(stillCaptureOutputFormat())
            .setResolutionSelector(useCaseResolution)
        if (settings.capturePriority == CapturePriority.FOCUS) captureBuilder.setJpegQuality(100)
        val capture = captureBuilder.build()
        imageCapture = capture

        val coachAnalyzer = CoachAnalyzer(
            executor = analysisExecutor,
            viewSize = { previewView.width to previewView.height },
            extras = extras,
            onFrame = onSignals,
            captureTimeIsRealtime = { cameraLocks.timestampsRealtime },
            minimumFrameIntervalMs = { ThermalPolicy.forLevel(thermalLevel).analysisIntervalMs },
            poseAndBackgroundEnabled = { ThermalPolicy.forLevel(thermalLevel).poseAndBackgroundEnabled },
            onBeautyFrame = faceStore?.let { store -> { frame -> store.publish(frame) } },
        )
        analyzer = coachAnalyzer

        val analysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setResolutionSelector(analysisResolutionSelector(settings.aspectRatio))
            .build()
            .also { it.setAnalyzer(analysisExecutor, coachAnalyzer) }

        val thermalPolicy = ThermalPolicy.forLevel(thermalLevel)
        val liveRequested = settings.livePhotoEnabled && thermalPolicy.allowNewLive
        val effectiveRequestedMode = if (liveRequested || beautyRequested) SuggestedMode.PHOTO else requestedMode
        val choice = ExtensionPolicy.resolve(
            requested = effectiveRequestedMode,
            isAvailable = { mode -> extensions?.isExtensionAvailable(selector, mode) == true },
            isAnalysisSupported = { mode -> extensions?.isImageAnalysisSupported(selector, mode) == true },
        )
        val boundSelector = when (choice) {
            is ExtensionChoice.Enabled ->
                extensions?.getExtensionEnabledCameraSelector(selector, choice.mode) ?: selector
            ExtensionChoice.Standard -> selector
        }
        var activeMode = if (choice is ExtensionChoice.Enabled) effectiveRequestedMode else SuggestedMode.PHOTO
        var extensionFallback = !liveRequested && effectiveRequestedMode != SuggestedMode.PHOTO && choice == ExtensionChoice.Standard
        livePhotoAvailable = false
        liveFallbackReason = if (settings.livePhotoEnabled && !thermalPolicy.allowNewLive) {
            "设备温度较高，Live 已降级为普通照片"
        } else null
        liveRecording.install(null)
        val liveCandidateTiers=mutableMapOf<Recorder,LiveVideoTier>()
        val liveCandidates: List<Pair<Recorder, VideoCapture<Recorder>>> = if (liveRequested) {
            (if (thermalPolicy.preferSdLive) LIVE_VIDEO_TIER_FALLBACK_ORDER.reversed() else LIVE_VIDEO_TIER_FALLBACK_ORDER).mapNotNull { tier ->
                runCatching { liveRecording.createCandidate(tier) }.getOrNull()?.also { candidate ->
                    liveCandidateTiers[candidate.first]=tier
                    android.util.Log.i("MotionStage","preflight candidate=${System.identityHashCode(candidate.first)}; requestedTier=$tier; thermal=$thermalLevel; preferSd=${thermalPolicy.preferSdLive}")
                }
            }.also {
                if (it.isEmpty()) liveFallbackReason = "编码器不可用，已回退普通照片"
            }
        } else {
            emptyList()
        }
        val targetRotation = preview.targetRotation
        val (viewPortWidth, viewPortHeight) = captureViewPortDimensions(settings.aspectRatio, targetRotation)
        val viewPortLayoutDirection = if (previewView.layoutDirection == LayoutDirection.RTL) {
            LayoutDirection.RTL
        } else {
            LayoutDirection.LTR
        }
        val viewPort = ViewPort.Builder(Rational(viewPortWidth, viewPortHeight), targetRotation)
            .setScaleType(ViewPort.FILL_CENTER)
            .setLayoutDirection(viewPortLayoutDirection)
            .build()
        val activeSession = CameraSessionCoordinator(
            provider = cameraProvider,
            owner = owner,
            preview = preview,
            analysis = analysis,
            capture = capture,
            viewPort = viewPort,
            effect = { beautyEffect },
        ).also { sessionCoordinator = it }
        standardLiveFallback={
            if(sessionCoordinator===activeSession && !released) {
                try {
                    activeSession.unbind()
                    liveRecording.install(null)
                    camera=activeSession.bind(selector,null)
                    camera?.let {cameraLocks.bind(it,true)}
                } catch(error:Throwable){onError(error)}
            }
        }

        if (liveRequested) {
            var liveBindingError: Exception? = null
            val boundCandidate = firstBindableLiveCandidate(
                candidates = liveCandidates,
                isSupported = { (candidateRecorder, candidateVideo) -> runCatching {
                    activeSession.isSupported(selector, candidateVideo,liveRecording.effectFor(candidateRecorder)).also {
                        android.util.Log.i("MotionStage","candidate=${System.identityHashCode(candidateRecorder)}; supported=$it; thermal=$thermalLevel")
                    }
                }.getOrDefault(false) },
                tryBind = { (candidateRecorder, candidateVideo) ->
                    unbindOwnedUseCases()
                    try {
                        camera = activeSession.bind(selector, candidateVideo,liveRecording.effectFor(candidateRecorder))
                        true
                    } catch (error: Exception) {
                        liveBindingError = error
                        false
                    }
                },
            )
            liveCandidates.filter {it!==boundCandidate}.forEach {liveRecording.discardCandidate(it.first)}
            if (boundCandidate != null) {
                android.util.Log.i("MotionStage","selected candidate=${System.identityHashCode(boundCandidate.first)}; requestedTier=${liveCandidateTiers[boundCandidate.first]}; thermal=$thermalLevel; preferSd=${thermalPolicy.preferSdLive}")
                liveRecording.install(boundCandidate.first)
                livePhotoAvailable = true
            }
            if (!livePhotoAvailable) {
                unbindOwnedUseCases()
                liveRecording.install(null)
                liveFallbackReason = when {
                    liveFallbackReason != null -> liveFallbackReason
                    liveBindingError != null -> "当前设备无法同时绑定 Live 与实时指导，已回退普通照片"
                    else -> "当前相机会话不支持 Live 与实时指导，已回退普通照片"
                }
                try {
                    camera = activeSession.bind(selector, null)
                    activeMode = SuggestedMode.PHOTO
                } catch (fallbackError: Exception) {
                    onError(fallbackError)
                    return null
                }
            }
        } else {
            try {
                camera = activeSession.bind(boundSelector, null)
            } catch (bindingError: Exception) {
                unbindOwnedUseCases()
                if (choice is ExtensionChoice.Enabled || beautyEffect != null) {
                    if (beautyEffect != null) {
                        beautyEffect?.close()
                        beautyEffect = null
                        faceStore?.clear()
                        mainExecutor.execute { onBeautyFallback("美颜绑定失败，正在尝试普通预览") }
                    }
                    try {
                        camera = activeSession.bind(selector, null)
                        activeMode = SuggestedMode.PHOTO
                        extensionFallback = true
                    } catch (fallbackError: Exception) {
                        onError(fallbackError)
                        return null
                    }
                } else {
                    onError(bindingError)
                    return null
                }
            }
        }
        camera?.let { cameraLocks.bind(it, activeMode == SuggestedMode.PHOTO) }
        liveRecording.bindSource(cameraLocks.sensorSource)
        applyStoredZoom()
        if (livePhotoAvailable) {
            startRollingRecording()
        } else if (liveRequested) {
            val reason = liveFallbackReason ?: "Live 当前不可用，已回退普通照片"
            onLiveFallback(reason)
        }
        return capabilities(activeMode, extensionFallback, livePhotoAvailable, liveFallbackReason)
    }

    fun setFocalPreset(cameraId: String): QuickFocalPreset? {
        val controls = QuickFocalPolicy.quickControls(discovered.presets)
        val selected = controls.firstOrNull { it.cameraId == cameraId } ?: return null
        selectedFocalId = selected.cameraId
        smoothZoomRatio = 1f
        cameraLocks.invalidate()
        return selected
    }

    fun updateThermalLevel(level: ThermalLevel) { thermalLevel = level }

    fun setTelephotoEnabled(enabled: Boolean): QuickFocalPreset? {
        val requested = if (enabled) {
            QuickFocalPolicy.preferredTelephoto(discovered.presets)
        } else {
            discovered.presets.firstOrNull { it.isDefault }
        }
        return requested?.let { setFocalPreset(it.cameraId) }
    }

    fun currentFocalPreset(): QuickFocalPreset? =
        QuickFocalPolicy.safestSelection(QuickFocalPolicy.quickControls(discovered.presets), selectedFocalId)

    fun hasTelephotoPreset(): Boolean =
        QuickFocalPolicy.preferredTelephoto(discovered.presets) != null

    fun zoomBy(scaleFactor: Float): Float? {
        if (!scaleFactor.isFinite() || abs(scaleFactor - 1f) < MIN_ZOOM_GESTURE_DELTA) return null
        val activeCamera = camera ?: return null
        val zoomState = activeCamera.cameraInfo.zoomState.value ?: return null
        val target = (smoothZoomRatio * scaleFactor)
            .coerceIn(zoomState.minZoomRatio, zoomState.maxZoomRatio)
        if (abs(target - smoothZoomRatio) < MIN_ZOOM_RATIO_DELTA) return null
        smoothZoomRatio = target
        activeCamera.cameraControl.setZoomRatio(target)
        return target * (currentFocalPreset()?.relativeZoom ?: 1f)
    }

    fun setExposure(evStops: Float, onResult: (Result<Float>) -> Unit) {
        val activeCamera = camera
        if (activeCamera == null) {
            exposureController.invalidate()
            onResult(Result.failure(IllegalStateException("相机尚未就绪")))
            return
        }
        val state = activeCamera.cameraInfo.exposureState
        val step = state.exposureCompensationStep.toFloat()
        val capability = ExposureCapability(state.exposureCompensationRange.lower * step,
            state.exposureCompensationRange.upper * step, step)
        exposureController.request(evStops, capability, apply = { index, complete ->
            val future = activeCamera.cameraControl.setExposureCompensationIndex(index)
            future.addListener({ complete(runCatching { future.get() }) }, mainExecutor)
        }, onResult = onResult)
    }

    fun setFlash(setting: FlashSetting) {
        flashSetting = setting
        imageCapture?.flashMode = flashMode(setting)
        camera?.cameraControl?.enableTorch(false)
    }

    fun tapToFocus(x: Float, y: Float, lock: Boolean, onResult: (Boolean, Boolean) -> Unit) {
        val factory = previewView?.meteringPointFactory ?: return
        val action = FocusMeteringAction.Builder(factory.createPoint(x, y),
            FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE).apply {
            if (lock) disableAutoCancel() else setAutoCancelDuration(3, TimeUnit.SECONDS)
        }.build()
        cameraLocks.request(action, lock) { success -> onResult(success, cameraLocks.state.bothConfirmed) }
    }

    fun unlockAeAf() { cameraLocks.request(null, false) }
    fun capture(
        spec: CaptureSpec,
        onSaveProgress: (CaptureSaveProgress) -> Unit,
        onSaved: (CapturedPhoto) -> Unit,
        onSaveError: (Throwable) -> Unit,
        onCaptureError: (Throwable) -> Unit,
        onCaptureAccepted: () -> Unit = {},
    ) {
        val capture = imageCapture
        if (!ThermalPolicy.forLevel(thermalLevel).preserveShutter) { onCaptureError(IllegalStateException("系统热保护，暂不能拍摄")); return }
        if (released || capture == null || captureInProgress || pendingCapture != null) {
            onCaptureError(IllegalStateException("camera is not ready"))
            return
        }
        val file = runCatching {
            captureBudget = pendingSources.reserve(creative = false, live = spec.livePhotoRequested)
            pendingSources.create("${spec.captureId.value}-S${spec.sequence}-")
        }.getOrElse { captureBudget?.close(); captureBudget = null; onCaptureError(it); return }
        captureInProgress = true
        val shutterElapsedMs = SystemClock.elapsedRealtime()
        capturedSensorTimestampNs = null
        captureLease = SaveTransactionRegistry.tryAcquire(journalStore.transactionKey(spec.captureId.value, spec.sequence))
        if (captureLease == null) {
            captureBudget?.close(); captureBudget = null
            captureInProgress = false
            file.delete()
            onCaptureError(IllegalStateException("照片正在由另一个保存任务处理"))
            return
        }
        val recordingAtShutter = liveRecording.marker()
        val recordingStartedAtShutterMs = recordingAtShutter?.startedElapsedMs
        val liveWindowReadyAtShutter = liveRecording.hasPrebuffer()
        if(spec.livePhotoRequested)android.util.Log.i("MotionStage","shutter requested t=$shutterElapsedMs; generation=${recordingAtShutter?.generation}; source=${recordingAtShutter?.source}; prebuffer=$liveWindowReadyAtShutter")
        if(spec.livePhotoRequested && liveWindowReadyAtShutter && recordingAtShutter!=null) {
            awaitingLiveCapture=spec.captureId.value to recordingAtShutter.generation
            liveRecording.holdForCoverage(recordingAtShutter)
        }
        val captureRecord = PendingCapture(file, spec, onSaved, onSaveError, onSaveProgress,
            shutterElapsedMs, recordingStartedAtShutterMs ?: shutterElapsedMs).also {
                it.coordinator.complete(SaveStage.SPACE_CHECK) // The live/still reservation above was accepted.
            }
        try {
            // Persist identity/source before CameraX can finish writing, including process death after capture.
            writeJournal(captureRecord, notify = false)
            val jpegCallback=object : JpegSaveCallback {
                    override fun onImageSaved() {
                        val early=earlyLiveOutput.takeIf {awaitingLiveCapture==Pair(spec.captureId.value,recordingAtShutter?.generation)}
                        if(early==null)earlyLiveOutput?.file?.delete()
                        earlyLiveOutput=null;awaitingLiveCapture=null
                        val sameSegmentAvailable=liveRecording.isCurrent(recordingAtShutter) || (early!=null && early.error==null && early.timeline!=null)
                        val liveWillPackage = spec.livePhotoRequested &&
                            !captureRecord.coordinator.snapshot.motionPhotoFallback &&
                            livePhotoAvailable &&
                            liveWindowReadyAtShutter &&
                            sameSegmentAvailable
                        val liveCaptureFallbackReason = when {
                            !spec.livePhotoRequested -> null
                            captureRecord.coordinator.snapshot.motionPhotoFallback -> "原始 JPEG 元数据不兼容 Live，已保存普通照片"
                            !livePhotoAvailable -> liveFallbackReason ?: "Live 不可用，已保存普通照片"
                            !liveWindowReadyAtShutter -> "Live 缓冲窗口不足，已保存普通照片"
                            !sameSegmentAvailable -> "Live 录制分段已切换，已保存普通照片"
                            else -> null
                        }
                        pendingCapture = PendingCapture(
                            file = file,
                            spec = spec.copy(livePhotoRequested = liveWillPackage),
                            onSaved = onSaved,
                            onSaveError = onSaveError,
                            onSaveProgress = onSaveProgress,
                            shutterElapsedMs = shutterElapsedMs,
                            recordingStartedElapsedMs = recordingStartedAtShutterMs ?: shutterElapsedMs,
                            capturedSensorTimestampNs = capturedSensorTimestampNs,
                            jpegRawPath=captureRecord.jpegRawPath,
                            liveGeneration=recordingAtShutter?.generation,
                            liveSource=recordingAtShutter?.source,
                        )
                        runCatching {
                            java.io.FileOutputStream(file, true).use { it.fd.sync() }
                            writeJournal(requireNotNull(pendingCapture))
                        }.onFailure { error ->
                            captureInProgress = false
                            settleCaptureOwnership()
                            early?.file?.delete()
                            onSaveError(IOException("无法建立异常恢复记录；临时原片仍可重试", error))
                            return
                        }
                        if (liveWillPackage) {
                            val pending = requireNotNull(pendingCapture)
                            val available = StatFs(context.cacheDir.absolutePath).availableBytes
                            val liveEstimate = SpaceEstimate(
                                sourceBytes = file.length() + MotionTemporaryPolicy.MAX_RECORDING_BYTES,
                                processingPeakBytes = file.length() + MotionTemporaryPolicy.MAX_RECORDING_BYTES,
                                derivativeBytes = 0L,
                            )
                            if (liveEstimate.fits(available)) {
                                pending.coordinator.complete(SaveStage.SPACE_CHECK)
                                writeJournal(pending)
                                if(early!=null)finalizeLiveCapture(pending,early.file,early.durationUs,early.error,early.timeline)
                                else liveRecording.stopWhenCovered(
                                    requireNotNull(recordingAtShutter),
                                    pending.capturedSensorTimestampNs,
                                )
                            } else {
                                pending.coordinator.complete(SaveStage.SPACE_CHECK)
                                early?.file?.delete()
                                pending.coordinator.fallbackFromMotionPhoto("Live 临时空间不足")
                                pending.warnings += "Live 空间不足，已降级普通照片"
                                writeJournal(pending)
                                recordingAtShutter?.let { liveRecording.stopAfter(it, 0L) }
                                publishPending(onSaved, onSaveError)
                            }
                        } else {
                            early?.file?.delete()
                            liveCaptureFallbackReason?.let { pendingCapture?.warnings?.add(it) }
                            publishPending(onSaved, onSaveError)
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        clearEarlyLiveOutput()
                        captureInProgress = false
                        if(captureRecord.jpegPreparation!=null) {
                            retainJpegPreparationFailure(captureRecord,exception)
                            return
                        } else {
                            file.delete()
                            runCatching { journalStore.delete(captureRecord.toJournal()) }
                        }
                        settleCaptureOwnership()
                        if (livePhotoAvailable && !liveRecording.isRecording) startRollingRecording()
                        onCaptureError(exception)
                    }
                }
            if(spec.livePhotoRequested && livePhotoAvailable) {
                captureTimestampedJpeg(capture,file,saveExecutor,mainExecutor,
                    timestamp={sensor ->
                        capturedSensorTimestampNs=sensor
                        android.util.Log.i("MotionStage","JPEG sensor received t=${SystemClock.elapsedRealtime()}; sensor=$sensor")
                        if(liveWindowReadyAtShutter && liveRecording.isCurrent(recordingAtShutter))
                            liveRecording.stopWhenCovered(requireNotNull(recordingAtShutter),sensor)
                    },
                    persistPreparation={preparation ->
                        val rawPath=preparation?.rawPath ?: captureRecord.jpegRawPath
                        if(preparation?.motionCompatible==false)
                            captureRecord.coordinator.fallbackFromMotionPhoto("原始 JPEG 元数据不兼容 Live")
                        val next=captureRecord.toJournal().copy(jpegPreparation=preparation,jpegRawPath=rawPath)
                        journalStore.write(next)
                        captureRecord.jpegPreparation=preparation
                        captureRecord.jpegRawPath=rawPath
                    },callback=jpegCallback)
            } else capture.takePicture(ImageCapture.OutputFileOptions.Builder(file).build(),mainExecutor,
                object:ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(result:ImageCapture.OutputFileResults)=jpegCallback.onImageSaved()
                    override fun onError(exception:ImageCaptureException)=jpegCallback.onError(exception)
                })
            onCaptureAccepted()
        } catch (error: Exception) {
            clearEarlyLiveOutput()
            captureInProgress = false
            file.delete()
            runCatching { journalStore.delete(captureRecord.toJournal()) }
            settleCaptureOwnership()
            onCaptureError(error)
        }
    }

    val pendingCaptureId: String? get() = pendingCapture?.spec?.captureId?.value
    internal fun retainJpegPreparationFailure(record:PendingCapture,error:Throwable) {
        check(record.jpegPreparation!=null)
        captureInProgress=false
        pendingCapture=record
        record.coordinator.fallbackFromMotionPhoto("JPEG 保存准备需要重试")
        record.coordinator.fail(SaveStage.ORIGINAL_PUBLISH,error.message ?: "JPEG 保存准备失败")
        runCatching {writeJournal(record)}
        settleCaptureOwnership()
        record.onSaveError(error)
    }

    fun retrySave(captureId: String, onSaved: (CapturedPhoto) -> Unit, onSaveError: (Throwable) -> Unit): Boolean {
        if (released || pendingCaptureId != captureId || pendingCapture?.file?.isFile != true || captureInProgress) return false
        pendingCapture?.let { pending ->
            pending.coordinator.snapshot.failedStage?.name?.let { failed ->
                pending.stageRetryCounts[failed] = (pending.stageRetryCounts[failed] ?: 0) + 1
            }
            pending.coordinator.retryFailed()
        }
        pendingCapture?.let(::writeJournal)
        captureInProgress = true
        publishPending(onSaved, onSaveError)
        return true
    }

    private val recoveryBusy = AtomicBoolean(false)
    fun loadRecoveryRecords(onResult: (List<SaveJournal>) -> Unit) {
        saveExecutor.execute {
            val records = journalStore.readAll().filter { SaveStage.COMPLETE.name !in it.completedStages || it.error != null }
            mainExecutor.execute { recoveryFailures = records; onResult(records) }
        }
    }

    fun retryInterruptedSaves(onResult: (List<SaveJournal>) -> Unit, key: String? = null) {
        if (!recoveryBusy.compareAndSet(false, true)) return
        effectExecutor.execute {
            val remaining = runCatching { interruptedSaveRecovery.recover(key) }.getOrElse { recoveryFailures }
            mainExecutor.execute { recoveryBusy.set(false); recoveryFailures = remaining; onResult(remaining) }
        }
    }

    fun discardPending():Boolean {
        pendingCapture?.let { pending ->
            try {discardCaptureSources(context.contentResolver,journalStore,pending.toJournal())}
            catch(error:DiscardIntentNotPersisted) {
                pending.onSaveError(error)
                return false
            } catch(error:Throwable) {pending.onSaveError(IOException("照片已放弃，暂存清理待重试",error))}
        }
        pendingCapture = null
        settleCaptureOwnership()
        if (livePhotoAvailable && !liveRecording.isRecording) startRollingRecording()
        return true
    }

    fun exportEditedCopy(
        source: Uri,
        style: CreativeStyle,
        edit: EditAdjustment,
        quality: DerivativeQuality,
        captureId: CaptureId,
        sequence: Int,
        takenAtMillis: Long,
        onSaved: (ExportedCopy) -> Unit,
        onError: (Throwable) -> Unit,
        beautyPreset: BeautyPreset = BeautyPreset.OFF,
        beautyEngineVersion: Int = BeautyPreset.ENGINE_VERSION,
        derivativeId: String = CaptureIdentity.create().value,
    ) {
        effectExecutor.execute {
            val result = runCatching {
                val record = SaveJournal(
                    captureId = captureId.value, sequence = sequence, takenAtMillis = takenAtMillis,
                    sourcePath = "", displayName = CaptureIdentity.displayName(CaptureId(derivativeId), CaptureAssetKind.EDITED,
                        sequence, takenAtMillis), style = style.name, derivativeId = derivativeId,
                    exportSourceUri = source.toString(), derivativeQuality = quality.name,
                    exposureStops = edit.exposureStops, contrast = edit.contrast, saturation = edit.saturation,
                    temperature = edit.temperature, tint = edit.tint, fade = edit.fade, styleStrength = edit.styleStrength,
                    beautyPreset = beautyPreset.name, beautyEngineVersion = beautyEngineVersion,
                    failedStage = SaveStage.DERIVATIVE_GENERATE.name,
                )
                interruptedSaveRecovery.createExport(record)

            }
            mainExecutor.execute {
                result.onSuccess(onSaved).onFailure(onError)
            }
        }
    }

    fun retryEditedCopy(derivativeId: String, onSaved: (ExportedCopy) -> Unit, onError: (Throwable) -> Unit) {
        effectExecutor.execute {
            val result = runCatching {
                val record = journalStore.readAll().singleOrNull { it.derivativeId == derivativeId }
                    ?: error("副本事务不存在，不会按当前参数重新创建")
                interruptedSaveRecovery.recoverExport(record)
            }
            mainExecutor.execute { result.onSuccess(onSaved).onFailure(onError) }
        }
    }

    fun saveEditRecipe(recipe: EditRecipe, onError: (Throwable) -> Unit = {}) {
        effectExecutor.execute {
            val result = runCatching { recipeStore.write(recipe) }
            mainExecutor.execute { result.exceptionOrNull()?.let(onError) }
        }
    }

    fun release() {
        clearEarlyLiveOutput()
        exposureController.invalidate()
        cameraLocks.invalidate()
        released = true
        stopRollingRecording(deleteFile = true)
        unbindOwnedUseCases()
        beautyEffect?.close()
        beautyStore?.clear()
        beautyStore = null
        analyzer?.close()
        analysisExecutor.shutdown()
        val pending = pendingCapture
        if (pending != null && captureInProgress && !saveWorkInProgress) {
            // Retiring the recording invalidates Finalize. Preserve its already captured JPEG.
            if (pending.coordinator.snapshot.nextStage == SaveStage.MOTION_PACKAGE) {
                pending.coordinator.fallbackFromMotionPhoto("相机会话已结束，保存普通照片")
                pending.warnings += "Live 会话已结束，已回退普通照片"
            }
            publishPending(pending.onSaved, pending.onSaveError)
        } else if (!captureInProgress) {
            settleCaptureOwnership()
        }
        shutdownSaversIfIdle()
        liveRecording.shutdown()
    }

    private fun settleCaptureOwnership() {
        if (pendingCapture == null || released) {
            captureBudget?.close(); captureBudget = null
            captureLease?.close()
            captureLease = null
        }
        shutdownSaversIfIdle()
        finishLiveDowngrade()
    }

    private fun shutdownSaversIfIdle() {
        if (released && captureLease == null && backgroundLeases.isEmpty()) {
            saveExecutor.shutdown(); effectExecutor.shutdown()
        }
    }

    private fun unbindOwnedUseCases() {
        exposureController.invalidate()
        // All fallback attempts reuse this Preview callback; invalidate once at bind entry/release.
        sessionCoordinator?.unbind()
    }

    private fun capabilities(
        activeMode: SuggestedMode,
        extensionFallback: Boolean,
        livePhotoAvailable: Boolean,
        livePhotoFallbackReason: String?,
    ): CameraCapabilities {
        val activeCamera = camera
        val exposure = activeCamera?.cameraInfo?.exposureState?.let { state ->
            val step = state.exposureCompensationStep.toFloat()
            ExposureCapability(
                minimumStops = state.exposureCompensationRange.lower * step,
                maximumStops = state.exposureCompensationRange.upper * step,
                stepStops = step,
            )
        } ?: ExposureCapability()
        val zoom = activeCamera?.cameraInfo?.zoomState?.value?.let { state ->
            ZoomCapability(state.minZoomRatio, state.maxZoomRatio)
        } ?: ZoomCapability()
        return CameraCapabilities(
            analysisSessionId = analyzer?.sessionId ?: 0L,
            focalPresets = QuickFocalPolicy.quickControls(discovered.presets),
            focalCandidates = QuickFocalPolicy.calibrationCandidates(discovered.presets),
            selectedFocalId = currentFocalPreset()?.cameraId,
            availableModes = availableModes(resolveSelector()),
            activeMode = activeMode,
            exposure = exposure,
            zoom = zoom,
            extensionFallback = extensionFallback,
            afLockSupported = cameraLocks.state.af != LockStatus.UNSUPPORTED,
            aeLockSupported = cameraLocks.state.ae != LockStatus.UNSUPPORTED,
            livePhotoAvailable = livePhotoAvailable,
            livePhotoFallbackReason = livePhotoFallbackReason,
        )
    }

    private fun availableModes(selector: CameraSelector): Set<SuggestedMode> = buildSet {
        add(SuggestedMode.PHOTO)
        listOf(SuggestedMode.PORTRAIT, SuggestedMode.HDR, SuggestedMode.NIGHT).forEach { mode ->
            val extensionMode = ExtensionPolicy.extensionMode(mode) ?: return@forEach
            if (
                extensions?.isExtensionAvailable(selector, extensionMode) == true &&
                extensions?.isImageAnalysisSupported(selector, extensionMode) == true
            ) {
                add(mode)
            }
        }
    }

    private fun startRollingRecording() {
        if (!livePhotoAvailable) return
        liveRecording.start(blocked = pendingCapture != null || captureInProgress || released)
    }

    private fun onLiveRecordingFinalized(file: File, durationUs: Long, error: Throwable?,timeline:VerifiedMotionTimeline?,generation:Long) {
        val pending = pendingCapture
        when {
            pending?.spec?.livePhotoRequested == true && pending.liveGeneration==generation && !pending.coordinator.snapshot.motionPhotoFallback -> {
                finalizeLiveCapture(pending, file, durationUs, error,timeline)
            }
            captureInProgress && awaitingLiveCapture?.second==generation -> {
                if(earlyLiveOutput==null)earlyLiveOutput=CompletedLiveOutput(file,durationUs,error,timeline,generation)
                else if(earlyLiveOutput?.file!=file)file.delete()
                if(error!=null)downgradeLive("Live 编码中断，已回退普通照片")
            }
            else -> {
                file.delete()
                if (error != null) {
                    downgradeLive("Live 编码中断，已回退普通照片")
                } else if (livePhotoAvailable) {
                    startRollingRecording()
                }
            }
        }
    }

    private fun finalizeLiveCapture(
        pending: PendingCapture,
        recordingFile: File,
        durationUs: Long,
        recordingError: Throwable?,
        timeline:VerifiedMotionTimeline?,
    ) {
        saveWorkInProgress = true
        saveExecutor.execute {
            val result = runCatching {
                if (recordingError != null) throw IOException("Live 编码失败", recordingError)
                if (!recordingFile.isFile || durationUs <= 0L) throw IOException("Live 临时视频无有效数据")
                val source=pending.liveSource ?: throw IOException("Live 相机身份未确认")
                val sensor=pending.capturedSensorTimestampNs ?: throw IOException("Live 快门 SENSOR_TIMESTAMP 未确认")
                check(timeline?.recordingGeneration==pending.liveGeneration)
                android.util.Log.i("MotionStage","join generation=${pending.liveGeneration}; source=$source; JPEG sensor=$sensor; retainedFirst=${timeline?.frames?.firstOrNull()?.sensorNs}; retainedLast=${timeline?.frames?.lastOrNull()?.sensorNs}; samples=${timeline?.frames?.size}")
                val window=timeline?.fullWindow(source,sensor)
                    ?: throw IOException("Live 实测视频未覆盖完整快门窗口，改存普通照片")
                val clipFile = File.createTempFile("motion-clip-", ".mp4", recordingFile.parentFile)
                val clip = Mp4Clipper.clip(recordingFile, clipFile, window)
                recordingFile.delete()
                pending.motionFile = clip.file
                val packageDirectory = File(context.cacheDir, "motion-packages")
                check(packageDirectory.exists() || packageDirectory.mkdirs())
                val packaged = File.createTempFile("motion-photo-", ".jpg", packageDirectory)
                MotionPhotoAssembler.assemble(
                    jpeg = pending.file,
                    mp4 = clip.file,
                    output = packaged,
                    presentationTimestampUs = clip.presentationTimestampUs,
                )
                pending.packagedFile = packaged
                pending.isMotionPhoto = true
                pending.coordinator.complete(SaveStage.MOTION_PACKAGE)
                writeJournal(pending)
            }
            result.onFailure { error ->
                recordingFile.delete()
                pending.motionFile?.delete()
                pending.motionFile = null
                pending.packagedFile?.delete()
                pending.packagedFile = null
                pending.isMotionPhoto = false
                pending.coordinator.fallbackFromMotionPhoto(error.message ?: "Live 打包失败")
                pending.warnings += "${error.message ?: "Live 打包失败"}；已保存普通照片"
                writeJournal(pending)
            }
            mainExecutor.execute {
                saveWorkInProgress = false
                publishPending(pending.onSaved, pending.onSaveError)
            }
        }
    }

    private fun stopRollingRecording(deleteFile: Boolean) {
        if (deleteFile) livePhotoAvailable = false
        liveRecording.stop(deleteFile)
    }

    private fun downgradeLive(reason: String) {
        livePhotoAvailable = false
        liveFallbackReason = reason
        stopRollingRecording(deleteFile = true)
        liveRebindPending=true
        finishLiveDowngrade()
        onLiveFallback?.invoke(reason)
    }
    private fun finishLiveDowngrade() {
        if(liveRebindPending && !captureInProgress && !released) {
            liveRebindPending=false
            standardLiveFallback?.invoke()
        }
    }

    private fun publishPending(onSaved: (CapturedPhoto) -> Unit, onSaveError: (Throwable) -> Unit) {
        if (saveWorkInProgress) return
        val pending = pendingCapture ?: run {
            captureInProgress = false
            onSaveError(IllegalStateException("pending capture missing"))
            return
        }
        saveWorkInProgress = true
        savePipeline.run(publishOriginal = {
                try {
                    publishPrimary(pending)
                } catch (error: Throwable) {
                    if (!pending.isMotionPhoto || pending.originalUri != null ||
                        pending.coordinator.snapshot.failedStage != SaveStage.ORIGINAL_PUBLISH) throw error
                    CaptureSaver.deletePendingOnly(context.contentResolver, pending.pendingUri)
                    pending.pendingUri = null
                    pending.verifiedAssetStages.removeAll { it.startsWith("ORIGINAL:") }
                    pending.coordinator.fallbackFromMotionPhoto("Live 发布失败")
                    pending.isMotionPhoto = false
                    val packageFile = pending.packagedFile
                    pending.packagedFile = null
                    pending.warnings += "Live 发布失败，已回退普通照片"
                    writeJournal(pending)
                    packageFile?.delete()
                    publishPrimary(pending)
                }
            }, originalReady = {
                if (!pending.spec.holdBatchCapture) {
                    check(pendingCapture === pending)
                    backgroundLeases[pending.spec.captureId.value] = captureLease
                    captureLease = null
                    captureBudget?.close(); captureBudget = null
                    pendingCapture = null
                    captureInProgress = false
                    saveWorkInProgress = false
                    finishLiveDowngrade()
                    pending.captureReleased = true
                    pending.onSaveProgress(CaptureSaveProgress(pending.spec.captureId.value,
                        pending.coordinator.snapshot, pending.originalUri?.toString(), captureReleased = true))
                }
            }, finish = { publishNonDestructive(pending) }, completed = { result ->
                val detached = pending.captureReleased
                if (!detached && pendingCapture === pending) {
                    captureInProgress = false
                    saveWorkInProgress = false
                }
                result.onSuccess { photo ->
                    val cleaned=listOfNotNull(pending.file,pending.jpegRawPath?.let(::File),
                        JpegPreparation.temporaryFor(pending.file),pending.motionFile,pending.packagedFile,pending.derivativeFile)
                        .map {source ->!source.exists() || source.delete()}.all {it}
                    if(cleaned)journalStore.delete(pending.toJournal())
                    else journalStore.write(pending.toJournal().copy(error="已发布照片的暂存清理待重试"))
                    if (pendingCapture === pending) { pendingCapture = null; settleCaptureOwnership() }
                    onSaved(photo)
                    if (livePhotoAvailable) startRollingRecording()
                }.onFailure { error ->
                    if (!detached) settleCaptureOwnership()
                    val originalUri = pending.originalUri
                    onSaveError(
                        if (originalUri != null) PartialSaveException(error.message ?: "后续保存阶段失败", originalUri, error)
                        else error,
                    )
                }
                if (detached) backgroundLeases.remove(pending.spec.captureId.value)?.close()
                shutdownSaversIfIdle()
            })
    }

    private fun publishPrimary(pending: PendingCapture) {
        pending.jpegPreparation?.let {preparation ->
            preparation.prepare(pending.file) // This capture retains its original live storage reservation.
            val next=pending.toJournal().copy(jpegPreparation=null,jpegRawPath=pending.jpegRawPath ?: preparation.rawPath)
            journalStore.write(next)
            pending.jpegPreparation=null;pending.jpegRawPath=next.jpegRawPath
        }
        runStage(pending, SaveStage.SPACE_CHECK) {
            val primary = pending.primaryFile()
            val derivativeBytes = if (pending.derivativeRequested) primary.length() else 0L
            val estimate = SpaceEstimate(
                sourceBytes = primary.length(),
                processingPeakBytes = if (pending.derivativeRequested) primary.length() * 2L else 0L,
                derivativeBytes = derivativeBytes,
            )
            val available = StatFs(context.cacheDir.absolutePath).availableBytes
            if (!estimate.fits(available)) throw IOException("空间不足：至少还需 ${estimate.requiredBytes} 字节")
        }

        runStage(pending, SaveStage.ORIGINAL_PUBLISH) {
            if (pending.originalUri == null) {
                val displayName = if (pending.isMotionPhoto) {
                    CaptureIdentity.motionPhotoDisplayName(pending.spec.captureId, pending.spec.sequence, pending.spec.takenAtMillis)
                } else {
                    CaptureIdentity.displayName(
                        pending.spec.captureId,
                        CaptureAssetKind.ORIGINAL,
                        pending.spec.sequence,
                        pending.spec.takenAtMillis,
                    )
                }
                pending.displayName = displayName
                writeJournal(pending)
                try {
                    pending.originalUri = CaptureSaver.publish(
                        context.contentResolver,
                        pending.primaryFile(),
                        displayName,
                        pending.spec.takenAtMillis,
                        motionPhoto = pending.isMotionPhoto,
                        existingUri = pending.pendingUri,
                        onAssetStage = { stage ->
                            pending.verifiedAssetStages += assetStageKey(PublishedAssetKind.ORIGINAL, stage)
                            if (stage == AssetPublishStage.VERIFY_PUBLISHED) pending.outputLength = pending.primaryFile().length()
                            writeJournal(pending)
                        },
                        onPendingCreated = { pendingUri ->
                            pending.pendingUri = pendingUri
                            writeJournal(pending)
                        },
                    )
                    pending.pendingUri = null
                } catch (error: Throwable) {
                    writeJournal(pending)
                    throw error
                }
            }
        }

    }

    private fun publishNonDestructive(pending: PendingCapture): CapturedPhoto {
        val warnings = pending.warnings
        val score = runCatching { creativeProcessor.score(pending.file) }.getOrElse {
            warnings += "本地清晰度/曝光评分不可用"
            PhotoQualityScore(total = 0.0, sharpness = 0.0, exposure = 0.0)
        }
        runStage(pending, SaveStage.RECIPE_WRITE) {
            if (!pending.recipeWritten) {
                recipeStore.write(
                    EditRecipe.create(
                        captureId = pending.spec.captureId,
                        sequence = pending.spec.sequence,
                        takenAtMillis = pending.spec.takenAtMillis,
                        style = pending.spec.style,
                        edit = pending.spec.edit,
                        sourceIsMotionPhoto = pending.isMotionPhoto,
                        beautyPreset = pending.spec.beautyPreset,
                        beautyEngineVersion = pending.spec.beautyEngineVersion,
                    ),
                )
                pending.recipeWritten = true
            }
        }

        if (pending.derivativeRequested) {
            runStage(pending, SaveStage.DERIVATIVE_GENERATE) {
                if (pending.derivativeFile?.isFile != true) {
                    val processed = pendingSources.generate { creativeProcessor.process(
                        pending.file,
                        pending.spec.style,
                        pending.spec.edit,
                        pending.spec.derivativeQuality,
                        pending.spec.portraitRegion,
                        BeautyPreset.requireSupported(pending.spec.beautyPreset.name, pending.spec.beautyEngineVersion),
                    ) }
                    pending.derivativeFile = processed.file
                    pending.effectWasDownsampled = processed.wasDownsampled
                    processed.beautyWarning?.let { pending.warnings += it }
                    if (processed.portraitConservativeStrengthApplied) {
                        pending.warnings += "已使用人像保守强度"
                    }
                }
            }
            runStage(pending, SaveStage.DERIVATIVE_PUBLISH) {
                if (pending.derivativeUri == null) {
                    writeJournal(pending)
                    try {
                        pending.derivativeUri = CaptureSaver.publish(
                            context.contentResolver,
                            requireNotNull(pending.derivativeFile),
                            CaptureIdentity.displayName(
                                pending.spec.captureId,
                                CaptureAssetKind.EFFECT,
                                pending.spec.sequence,
                                pending.spec.takenAtMillis,
                            ),
                            pending.spec.takenAtMillis,
                            existingUri = pending.derivativePendingUri,
                            onAssetStage = { stage ->
                                pending.verifiedAssetStages += assetStageKey(PublishedAssetKind.DERIVATIVE, stage)
                                writeJournal(pending)
                            },
                            onPendingCreated = { pendingUri ->
                                pending.derivativePendingUri = pendingUri
                                writeJournal(pending)
                            },
                        )
                        pending.derivativePendingUri = null
                    } catch (error: Throwable) {
                        writeJournal(pending)
                        throw error
                    }
                    pending.derivativeFile?.delete()
                    pending.derivativeFile = null
                }
            }
        }
        runStage(pending, SaveStage.COMPLETE) { Unit }

        val originalUri = requireNotNull(pending.originalUri)
        if (!pending.derivativeRequested && (pending.spec.beautyPreset != BeautyPreset.OFF || !CreativeColorMatrix.isIdentity(CreativeColorMatrix.forSelection(pending.spec.style, pending.spec.edit)))) {
            warnings += "已保存原片和编辑配方；效果 JPEG 仅在你明确另存时生成"
        }
        return CapturedPhoto(
            captureId = pending.spec.captureId,
            originalUri = originalUri,
            displayUri = pending.derivativeUri ?: originalUri,
            score = score,
            style = pending.spec.style,
            effectWasDownsampled = pending.effectWasDownsampled,
            warning = warnings.joinToString("；").ifBlank { null },
            isMotionPhoto = pending.isMotionPhoto,
            completedSaveStages = pending.coordinator.snapshot.completed,
            edit = pending.spec.edit,
            beautyPreset = pending.spec.beautyPreset,
            beautyEngineVersion = pending.spec.beautyEngineVersion,
        )
    }

    private fun runStage(pending: PendingCapture, stage: SaveStage, block: () -> Unit) {
        if (stage in pending.coordinator.snapshot.completed) return
        check(pending.coordinator.snapshot.nextStage == stage) {
            "save stage mismatch: expected ${pending.coordinator.snapshot.nextStage}, got $stage"
        }
        try {
            block()
            pending.coordinator.complete(stage)
            writeJournal(pending)
        } catch (error: Throwable) {
            if (stage !in pending.coordinator.snapshot.completed) {
                pending.coordinator.fail(stage, error.message ?: "保存阶段失败")
            }
            writeJournal(pending)
            val partial = if (pending.coordinator.snapshot.isPartialSuccess) "原片已保存；" else ""
            throw IOException("${partial}${stage.userLabel()}失败：${error.message ?: "未知错误"}", error)
        }
    }

    private fun resolveSelector(): CameraSelector {
        val selected = QuickFocalPolicy.safestSelection(discovered.presets, selectedFocalId)
        val selectedInfo = selected?.cameraId?.let(discovered.cameraInfoById::get)
        return selectedInfo?.let(CameraCapabilityDiscovery::selectorFor)
            ?: CameraCapabilityDiscovery.fallbackRearSelector()
    }

    private fun applyStoredZoom() {
        val activeCamera = camera ?: return
        val state = activeCamera.cameraInfo.zoomState.value ?: return
        smoothZoomRatio = smoothZoomRatio.coerceIn(state.minZoomRatio, state.maxZoomRatio)
        activeCamera.cameraControl.setZoomRatio(smoothZoomRatio)
    }

    private fun resolutionSelector(aspectRatio: CaptureAspectRatio): ResolutionSelector =
        ResolutionSelector.Builder()
            .setAspectRatioStrategy(
                AspectRatioStrategy(cameraXAspectRatio(aspectRatio), AspectRatioStrategy.FALLBACK_RULE_AUTO),
            )
            .build()

    private fun analysisResolutionSelector(aspectRatio: CaptureAspectRatio): ResolutionSelector {
        val size = when (aspectRatio) {
            CaptureAspectRatio.FOUR_THREE -> Size(1280, 960)
            CaptureAspectRatio.SIXTEEN_NINE -> Size(1280, 720)
        }
        return ResolutionSelector.Builder()
            .setAspectRatioStrategy(
                AspectRatioStrategy(cameraXAspectRatio(aspectRatio), AspectRatioStrategy.FALLBACK_RULE_AUTO),
            )
            .setResolutionStrategy(
                ResolutionStrategy(size, ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER),
            )
            .build()
    }

    private fun cameraXAspectRatio(aspectRatio: CaptureAspectRatio): Int = when (aspectRatio) {
        CaptureAspectRatio.FOUR_THREE -> AspectRatio.RATIO_4_3
        CaptureAspectRatio.SIXTEEN_NINE -> AspectRatio.RATIO_16_9
    }

    private fun captureMode(priority: CapturePriority): Int = when (priority) {
        CapturePriority.FOCUS -> ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
        CapturePriority.SPEED -> ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
    }

    private fun flashMode(setting: FlashSetting): Int = when (setting) {
        FlashSetting.OFF -> ImageCapture.FLASH_MODE_OFF
        FlashSetting.AUTO -> ImageCapture.FLASH_MODE_AUTO
    }

    private suspend fun awaitProvider(): ProcessCameraProvider = suspendCancellableCoroutine { cont ->
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener(
            {
                try {
                    cont.resume(future.get())
                } catch (error: Exception) {
                    cont.resumeWithException(error)
                }
            },
            mainExecutor,
        )
    }

    private suspend fun awaitExtensions(cameraProvider: ProcessCameraProvider): ExtensionsManager =
        suspendCancellableCoroutine { cont ->
            val future = ExtensionsManager.getInstanceAsync(context, cameraProvider)
            future.addListener(
                {
                    try {
                        cont.resume(future.get())
                    } catch (error: Exception) {
                        cont.resumeWithException(error)
                    }
                },
                mainExecutor,
            )
        }

    private fun writeJournal(pending: PendingCapture, notify: Boolean = true) {
        journalStore.write(pending.toJournal())
        if (!notify) return
        val progress = CaptureSaveProgress(pending.spec.captureId.value, pending.coordinator.snapshot, pending.originalUri?.toString(), pending.captureReleased)
        mainExecutor.execute { pending.onSaveProgress(progress) }
    }


    private companion object {
        const val MIN_ZOOM_GESTURE_DELTA = 0.005f
        const val MIN_ZOOM_RATIO_DELTA = 0.005f
        const val LIVE_POST_SHUTTER_MS = 1_500L
        const val MIN_STILL_CAPTURE_FREE_BYTES = 24L * 1024L * 1024L
        const val MIN_LIVE_CAPTURE_FREE_BYTES = 64L * 1024L * 1024L
    }

}

internal fun stillCaptureOutputFormat(): Int = ImageCapture.OUTPUT_FORMAT_JPEG

internal fun <T> firstBindableLiveCandidate(
    candidates: List<T>,
    isSupported: (T) -> Boolean,
    tryBind: (T) -> Boolean,
): T? {
    for (candidate in candidates) {
        if (isSupported(candidate) && tryBind(candidate)) return candidate
    }
    return null
}

private fun SaveStage.userLabel(): String = when (this) {
    SaveStage.SPACE_CHECK -> "空间预检"
    SaveStage.MOTION_PACKAGE -> "Live 打包"
    SaveStage.ORIGINAL_PUBLISH -> "原片发布"
    SaveStage.RECIPE_WRITE -> "编辑配方保存"
    SaveStage.DERIVATIVE_GENERATE -> "效果生成"
    SaveStage.DERIVATIVE_PUBLISH -> "效果发布"
    SaveStage.COMPLETE -> "保存完成"
}
