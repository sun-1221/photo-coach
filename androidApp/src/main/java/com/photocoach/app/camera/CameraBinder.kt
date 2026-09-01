package com.photocoach.app.camera

import android.content.Context
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
import androidx.camera.core.SessionConfig
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.ViewPort
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.extensions.ExtensionsManager
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
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
import java.util.concurrent.ScheduledExecutorService
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

enum class FlashSetting { OFF, AUTO }

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
)

data class ExportedCopy(
    val uri: Uri,
    val wasDownsampled: Boolean,
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
)

class CameraBinder(
    private val context: Context,
    private val targetFocalVerifier: TargetFocalVerifier = TargetFocalVerifier.NONE,
) {
    private val analysisExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val saveExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val videoExecutor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private val creativeProcessor = CreativeImageProcessor(context.cacheDir)
    private val recipeStore = EditRecipeStore(File(context.filesDir, "edit-recipes"))
    private val journalStore = SaveJournalStore(File(context.filesDir, "save-journal"))
    private val mainExecutor by lazy { ContextCompat.getMainExecutor(context) }
    private var provider: ProcessCameraProvider? = null
    private var extensions: ExtensionsManager? = null
    private var discovered = DiscoveredCameras(emptyList(), emptyMap())
    private var selectedFocalId: String? = null
    private var camera: Camera? = null
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recorder: Recorder? = null
    private var activeRecording: Recording? = null
    private var activeVideoFile: File? = null
    private var recordingStartedElapsedMs: Long = 0L
    private var livePhotoAvailable = false
    private var liveFallbackReason: String? = null
    private var onLiveFallback: ((String) -> Unit)? = null
    private var analyzer: CoachAnalyzer? = null
    private var previewView: PreviewView? = null
    private var pendingCapture: PendingCapture? = null
    private var captureInProgress = false
    private var smoothZoomRatio = 1f
    private val prepareMutex = Mutex()
    @Volatile private var thermalLevel: ThermalLevel = ThermalLevel.UNKNOWN

    var flashSetting: FlashSetting = FlashSetting.OFF
        private set
    var aeAfLocked: Boolean = false
        private set

    suspend fun prepare() = prepareMutex.withLock {
        if (provider != null) return
        withContext(Dispatchers.IO) { recoverInterruptedSaves() }
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
    ): CameraCapabilities? {
        val cameraProvider = provider ?: run {
            onError(IllegalStateException("camera provider missing"))
            return null
        }
        this.previewView = previewView
        this.onLiveFallback = onLiveFallback
        stopRollingRecording(deleteFile = true)
        cameraProvider.unbindAll()
        camera = null
        aeAfLocked = false
        analyzer?.close()

        val selector = resolveSelector()
        val useCaseResolution = resolutionSelector(settings.aspectRatio)
        val preview = Preview.Builder()
            .setResolutionSelector(useCaseResolution)
            .build()
            .also { it.surfaceProvider = previewView.surfaceProvider }
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
            minimumFrameIntervalMs = { ThermalPolicy.forLevel(thermalLevel).analysisIntervalMs },
            poseAndBackgroundEnabled = { ThermalPolicy.forLevel(thermalLevel).poseAndBackgroundEnabled },
        )
        analyzer = coachAnalyzer

        val analysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setResolutionSelector(analysisResolutionSelector(settings.aspectRatio))
            .build()
            .also { it.setAnalyzer(analysisExecutor, coachAnalyzer) }

        val thermalPolicy = ThermalPolicy.forLevel(thermalLevel)
        val liveRequested = settings.livePhotoEnabled && thermalPolicy.allowNewLive
        val effectiveRequestedMode = if (liveRequested) SuggestedMode.PHOTO else requestedMode
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
        recorder = null
        videoCapture = null
        val liveCandidates: List<Pair<Recorder, VideoCapture<Recorder>>> = if (liveRequested) {
            (if (thermalPolicy.preferSdLive) LIVE_VIDEO_TIER_FALLBACK_ORDER.reversed() else LIVE_VIDEO_TIER_FALLBACK_ORDER).mapNotNull { tier ->
                runCatching {
                    val candidateRecorder = Recorder.Builder()
                        .setExecutor(videoExecutor)
                        .setQualitySelector(QualitySelector.from(tier.toCameraXQuality()))
                        .build()
                    candidateRecorder to VideoCapture.withOutput(candidateRecorder)
                }.getOrNull()
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
        fun useCaseGroup(video: VideoCapture<Recorder>?): UseCaseGroup = UseCaseGroup.Builder()
            .setViewPort(viewPort)
            .addUseCase(preview)
            .addUseCase(analysis)
            .addUseCase(capture)
            .apply { if (video != null) addUseCase(video) }
            .build()
        fun sessionConfig(video: VideoCapture<Recorder>): SessionConfig =
            SessionConfig.Builder(listOf(preview, analysis, capture, video))
                .setViewPort(viewPort)
                .build()
        fun sessionSupported(video: VideoCapture<Recorder>): Boolean = runCatching {
            cameraProvider.getCameraInfo(selector).isSessionConfigSupported(sessionConfig(video))
        }.getOrDefault(true)

        if (liveRequested) {
            var liveBindingError: Exception? = null
            val boundCandidate = firstBindableLiveCandidate(
                candidates = liveCandidates,
                isSupported = { (_, candidateVideo) -> sessionSupported(candidateVideo) },
                tryBind = { (_, candidateVideo) ->
                    cameraProvider.unbindAll()
                    try {
                        camera = cameraProvider.bindToLifecycle(owner, selector, useCaseGroup(candidateVideo))
                        true
                    } catch (error: Exception) {
                        liveBindingError = error
                        false
                    }
                },
            )
            if (boundCandidate != null) {
                recorder = boundCandidate.first
                videoCapture = boundCandidate.second
                livePhotoAvailable = true
            }
            if (!livePhotoAvailable) {
                cameraProvider.unbindAll()
                recorder = null
                videoCapture = null
                liveFallbackReason = when {
                    liveFallbackReason != null -> liveFallbackReason
                    liveBindingError != null -> "当前设备无法同时绑定 Live 与实时指导，已回退普通照片"
                    else -> "当前相机会话不支持 Live 与实时指导，已回退普通照片"
                }
                try {
                    camera = cameraProvider.bindToLifecycle(owner, selector, useCaseGroup(null))
                    activeMode = SuggestedMode.PHOTO
                } catch (fallbackError: Exception) {
                    onError(fallbackError)
                    return null
                }
            }
        } else {
            try {
                camera = cameraProvider.bindToLifecycle(owner, boundSelector, useCaseGroup(null))
            } catch (bindingError: Exception) {
                cameraProvider.unbindAll()
                if (choice is ExtensionChoice.Enabled) {
                    try {
                        camera = cameraProvider.bindToLifecycle(owner, selector, useCaseGroup(null))
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
        aeAfLocked = false
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

    fun setExposure(evStops: Float): Float {
        val activeCamera = camera ?: return 0f
        val state = activeCamera.cameraInfo.exposureState
        val range = state.exposureCompensationRange
        val step = state.exposureCompensationStep.toFloat()
        if (range.lower == 0 && range.upper == 0 || step <= 0f) return 0f
        val index = (evStops / step).roundToInt().coerceIn(range.lower, range.upper)
        activeCamera.cameraControl.setExposureCompensationIndex(index)
        return index * step
    }

    fun setFlash(setting: FlashSetting) {
        flashSetting = setting
        imageCapture?.flashMode = flashMode(setting)
        camera?.cameraControl?.enableTorch(false)
    }

    fun tapToFocus(
        x: Float,
        y: Float,
        lock: Boolean,
        onResult: (success: Boolean, locked: Boolean) -> Unit,
    ) {
        val activeCamera = camera ?: run {
            onResult(false, false)
            return
        }
        val factory = previewView?.meteringPointFactory ?: run {
            onResult(false, false)
            return
        }
        val point = factory.createPoint(x, y)
        val builder = FocusMeteringAction.Builder(
            point,
            FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE,
        )
        if (lock) builder.disableAutoCancel() else builder.setAutoCancelDuration(3, TimeUnit.SECONDS)
        aeAfLocked = false
        val future = activeCamera.cameraControl.startFocusAndMetering(builder.build())
        future.addListener(
            {
                val success = runCatching { future.get().isFocusSuccessful }.getOrDefault(false)
                aeAfLocked = lock && success
                if (lock && !success) activeCamera.cameraControl.cancelFocusAndMetering()
                onResult(success, aeAfLocked)
            },
            mainExecutor,
        )
    }

    fun unlockAeAf() {
        camera?.cameraControl?.cancelFocusAndMetering()
        aeAfLocked = false
    }

    fun prepareQualityCapture(x: Float?, y: Float?, onReady: () -> Unit) {
        val activeCamera = camera
        val view = previewView
        if (activeCamera == null || view == null || aeAfLocked || view.width <= 0 || view.height <= 0) {
            onReady()
            return
        }
        val point = view.meteringPointFactory.createPoint(
            x?.coerceIn(0f, view.width.toFloat()) ?: view.width / 2f,
            y?.coerceIn(0f, view.height.toFloat()) ?: view.height / 2f,
        )
        val action = FocusMeteringAction.Builder(
            point,
            FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE,
        ).setAutoCancelDuration(3, TimeUnit.SECONDS).build()
        val completed = AtomicBoolean(false)
        val finish = Runnable {
            if (completed.compareAndSet(false, true)) onReady()
        }
        view.postDelayed(finish, QUALITY_FOCUS_TIMEOUT_MS)
        val future = activeCamera.cameraControl.startFocusAndMetering(action)
        future.addListener(
            {
                runCatching { future.get() }
                view.removeCallbacks(finish)
                finish.run()
            },
            mainExecutor,
        )
    }

    fun capture(
        spec: CaptureSpec,
        onSaveProgress: (SaveSnapshot) -> Unit,
        onSaved: (CapturedPhoto) -> Unit,
        onSaveError: (Throwable) -> Unit,
        onCaptureError: (Throwable) -> Unit,
    ) {
        val capture = imageCapture
        if (capture == null || captureInProgress) {
            onCaptureError(IllegalStateException("camera is not ready"))
            return
        }
        val minimumFreeBytes = if (spec.livePhotoRequested) MIN_LIVE_CAPTURE_FREE_BYTES else MIN_STILL_CAPTURE_FREE_BYTES
        if (StatFs(context.cacheDir.absolutePath).availableBytes < minimumFreeBytes) {
            onCaptureError(IOException("存储空间不足，无法安全保留原片"))
            return
        }
        captureInProgress = true
        val directory = File(context.cacheDir, "pending-captures")
        val file = runCatching {
            check(directory.exists() || directory.mkdirs()) { "cannot create pending capture directory" }
            File.createTempFile("${spec.captureId.value}-S${spec.sequence}-", ".jpg", directory)
        }.getOrElse {
            captureInProgress = false
            onCaptureError(it)
            return
        }
        val shutterElapsedMs = SystemClock.elapsedRealtime()
        val recordingAtShutter = activeRecording
        val recordingStartedAtShutterMs = recordingStartedElapsedMs
        val liveWindowReadyAtShutter = recordingAtShutter != null &&
            MotionTemporaryPolicy.hasFullShutterWindow(shutterElapsedMs - recordingStartedAtShutterMs)
        capture.takePicture(
            ImageCapture.OutputFileOptions.Builder(file).build(),
            mainExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val liveWillPackage = spec.livePhotoRequested &&
                        livePhotoAvailable &&
                        liveWindowReadyAtShutter &&
                        activeRecording === recordingAtShutter
                    val liveCaptureFallbackReason = when {
                        !spec.livePhotoRequested -> null
                        !livePhotoAvailable -> liveFallbackReason ?: "Live 不可用，已保存普通照片"
                        !liveWindowReadyAtShutter -> "Live 缓冲窗口不足，已保存普通照片"
                        activeRecording !== recordingAtShutter -> "Live 录制分段已切换，已保存普通照片"
                        else -> null
                    }
                    pendingCapture = PendingCapture(
                        file = file,
                        spec = spec.copy(livePhotoRequested = liveWillPackage),
                        onSaved = onSaved,
                        onSaveError = onSaveError,
                        onSaveProgress = onSaveProgress,
                        shutterElapsedMs = shutterElapsedMs,
                        recordingStartedElapsedMs = recordingStartedAtShutterMs,
                    )
                    runCatching { writeJournal(requireNotNull(pendingCapture)) }.onFailure { error ->
                        captureInProgress = false
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
                            videoExecutor.schedule(
                                { runCatching { recordingAtShutter?.stop() } },
                                LIVE_POST_SHUTTER_MS,
                                TimeUnit.MILLISECONDS,
                            )
                        } else {
                            pending.coordinator.complete(SaveStage.SPACE_CHECK)
                            pending.coordinator.fallbackFromMotionPhoto("Live 临时空间不足")
                            pending.warnings += "Live 空间不足，已降级普通照片"
                            writeJournal(pending)
                            runCatching { recordingAtShutter?.stop() }
                            publishPending(onSaved, onSaveError)
                        }
                    } else {
                        liveCaptureFallbackReason?.let { pendingCapture?.warnings?.add(it) }
                        publishPending(onSaved, onSaveError)
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    captureInProgress = false
                    file.delete()
                    if (livePhotoAvailable && activeRecording == null) startRollingRecording()
                    onCaptureError(exception)
                }
            },
        )
    }

    fun retrySave(onSaved: (CapturedPhoto) -> Unit, onSaveError: (Throwable) -> Unit): Boolean {
        if (pendingCapture?.file?.isFile != true || captureInProgress) return false
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

    fun discardPending() {
        pendingCapture?.let { pending ->
            pending.file.delete()
            pending.motionFile?.delete()
            pending.packagedFile?.delete()
            journalStore.delete(pending.toJournal())
        }
        pendingCapture = null
        if (livePhotoAvailable && activeRecording == null) startRollingRecording()
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
    ) {
        saveExecutor.execute {
            val result = runCatching {
                val processed = creativeProcessor.process(context.contentResolver, source, style, edit, quality)
                try {
                    val uri = CaptureSaver.publish(
                        context.contentResolver,
                        processed.file,
                        CaptureIdentity.displayName(captureId, CaptureAssetKind.EDITED, sequence, takenAtMillis),
                        takenAtMillis,
                    )
                    ExportedCopy(uri, processed.wasDownsampled)
                } finally {
                    processed.file.delete()
                }
            }
            mainExecutor.execute {
                result.onSuccess(onSaved).onFailure(onError)
            }
        }
    }

    fun saveEditRecipe(recipe: EditRecipe, onError: (Throwable) -> Unit = {}) {
        saveExecutor.execute {
            val result = runCatching { recipeStore.write(recipe) }
            mainExecutor.execute { result.exceptionOrNull()?.let(onError) }
        }
    }

    fun release() {
        stopRollingRecording(deleteFile = true)
        provider?.unbindAll()
        analyzer?.close()
        analysisExecutor.shutdown()
        saveExecutor.shutdown()
        videoExecutor.shutdown()
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
            focalPresets = QuickFocalPolicy.quickControls(discovered.presets),
            focalCandidates = QuickFocalPolicy.calibrationCandidates(discovered.presets),
            selectedFocalId = currentFocalPreset()?.cameraId,
            availableModes = availableModes(resolveSelector()),
            activeMode = activeMode,
            exposure = exposure,
            zoom = zoom,
            extensionFallback = extensionFallback,
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
        val activeRecorder = recorder ?: return
        if (!livePhotoAvailable || activeRecording != null || pendingCapture != null) return
        val directory = File(context.cacheDir, "motion-recordings")
        val file = runCatching {
            check(directory.exists() || directory.mkdirs()) { "cannot create Motion Photo directory" }
            MotionTemporaryPolicy.expired(directory.listFiles()?.toList().orEmpty(), System.currentTimeMillis())
                .forEach { it.delete() }
            File.createTempFile("motion-", ".mp4", directory)
        }.getOrElse {
            downgradeLive("无法创建 Live 临时文件，已回退普通照片")
            return
        }
        activeVideoFile = file
        recordingStartedElapsedMs = SystemClock.elapsedRealtime()
        try {
            val options = FileOutputOptions.Builder(file)
                .setFileSizeLimit(MotionTemporaryPolicy.MAX_RECORDING_BYTES)
                .setDurationLimitMillis(MotionTemporaryPolicy.MAX_RECORDING_DURATION_MS)
                .build()
            // Deliberately do not call withAudioEnabled(): this recording has no audio track and needs no permission.
            activeRecording = activeRecorder.prepareRecording(context, options).start(mainExecutor, ::onVideoRecordEvent)
        } catch (error: Throwable) {
            file.delete()
            activeVideoFile = null
            activeRecording = null
            downgradeLive("视频编码器启动失败，已回退普通照片")
        }
    }

    private fun onVideoRecordEvent(event: VideoRecordEvent) {
        if (event !is VideoRecordEvent.Finalize) return
        val file = (event.outputOptions as? FileOutputOptions)?.file ?: activeVideoFile
        val durationUs = event.recordingStats.recordedDurationNanos / 1_000L
        activeRecording = null
        activeVideoFile = null
        val pending = pendingCapture
        val limitReached = event.error == VideoRecordEvent.Finalize.ERROR_DURATION_LIMIT_REACHED ||
            event.error == VideoRecordEvent.Finalize.ERROR_FILE_SIZE_LIMIT_REACHED
        when {
            pending?.spec?.livePhotoRequested == true && !pending.coordinator.snapshot.motionPhotoFallback && file != null -> {
                finalizeLiveCapture(pending, file, durationUs, event.takeIf { it.hasError() && !limitReached }?.cause)
            }
            else -> {
                file?.delete()
                if (event.hasError() && !limitReached) {
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
    ) {
        saveExecutor.execute {
            val result = runCatching {
                if (recordingError != null) throw IOException("Live 编码失败", recordingError)
                if (!recordingFile.isFile || durationUs <= 0L) throw IOException("Live 临时视频无有效数据")
                val shutterUs = ((pending.shutterElapsedMs - pending.recordingStartedElapsedMs) * 1_000L)
                    .coerceIn(0L, durationUs)
                val window = MotionClipWindow.aroundShutter(0L, shutterUs, durationUs)
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
                pending.warnings += "Live 打包失败，已可靠降级普通照片"
                writeJournal(pending)
            }
            mainExecutor.execute { publishPending(pending.onSaved, pending.onSaveError) }
        }
    }

    private fun stopRollingRecording(deleteFile: Boolean) {
        if (deleteFile) livePhotoAvailable = false
        val recording = activeRecording
        activeRecording = null
        runCatching { recording?.stop() }
        if (recording == null && deleteFile) activeVideoFile?.delete()
        activeVideoFile = null
    }

    private fun downgradeLive(reason: String) {
        livePhotoAvailable = false
        liveFallbackReason = reason
        stopRollingRecording(deleteFile = true)
        onLiveFallback?.invoke(reason)
    }

    private fun publishPending(onSaved: (CapturedPhoto) -> Unit, onSaveError: (Throwable) -> Unit) {
        val pending = pendingCapture ?: run {
            captureInProgress = false
            onSaveError(IllegalStateException("pending capture missing"))
            return
        }
        saveExecutor.execute {
            val result = runCatching { publishNonDestructive(pending) }
            mainExecutor.execute {
                captureInProgress = false
                result.onSuccess { photo ->
                    pending.file.delete()
                    pending.motionFile?.delete()
                    pending.packagedFile?.delete()
                    pending.derivativeFile?.delete()
                    journalStore.delete(pending.toJournal())
                    pendingCapture = null
                    onSaved(photo)
                    if (livePhotoAvailable) startRollingRecording()
                }.onFailure { error ->
                    val originalUri = pending.originalUri
                    onSaveError(
                        if (originalUri != null) PartialSaveException(error.message ?: "后续保存阶段失败", originalUri, error)
                        else error,
                    )
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
                try {
                    pending.originalUri = CaptureSaver.publish(
                        context.contentResolver,
                        pending.primaryFile(),
                        displayName,
                        pending.spec.takenAtMillis,
                        motionPhoto = pending.isMotionPhoto,
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
                    pending.pendingUri = null
                    writeJournal(pending)
                    throw error
                }
            }
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
                    ),
                )
                pending.recipeWritten = true
            }
        }

        if (pending.derivativeRequested) {
            runStage(pending, SaveStage.DERIVATIVE_GENERATE) {
                if (pending.derivativeFile?.isFile != true) {
                    val processed = creativeProcessor.process(
                        pending.file,
                        pending.spec.style,
                        pending.spec.edit,
                        pending.spec.derivativeQuality,
                        pending.spec.portraitRegion,
                    )
                    pending.derivativeFile = processed.file
                    pending.effectWasDownsampled = processed.wasDownsampled
                    if (processed.portraitConservativeStrengthApplied) {
                        pending.warnings += "已使用人像保守强度"
                    }
                }
            }
            runStage(pending, SaveStage.DERIVATIVE_PUBLISH) {
                if (pending.derivativeUri == null) {
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
                        pending.derivativePendingUri = null
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
        if (!pending.derivativeRequested && !CreativeColorMatrix.isIdentity(CreativeColorMatrix.forSelection(pending.spec.style, pending.spec.edit))) {
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
            pending.coordinator.fail(stage, error.message ?: "保存阶段失败")
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

    private fun writeJournal(pending: PendingCapture) {
        journalStore.write(pending.toJournal())
        mainExecutor.execute { pending.onSaveProgress(pending.coordinator.snapshot) }
    }

    private fun recoverInterruptedSaves() {
        val resolver = context.contentResolver
        val motionDirectory = File(context.cacheDir, "motion-recordings")
        MotionTemporaryPolicy.expired(motionDirectory.listFiles()?.toList().orEmpty(), System.currentTimeMillis())
            .forEach(File::delete)
        journalStore.readAll().forEach { originalRecord ->
            var record = originalRecord
            var recoveryStage: SaveStage? = null
            runCatching {
                val source = File(record.sourcePath)
                val packaged = record.packagedPath?.let(::File)?.takeIf(File::isFile)
                val primary = packaged ?: source.takeIf(File::isFile)
                if (SaveStage.COMPLETE.name in record.completedStages) {
                    cleanupRecoveredRecord(record)
                    journalStore.delete(record)
                    return@runCatching
                }
                val motionPhoto = record.motionPhotoRequested && !record.motionPhotoFallback && packaged != null
                if (
                    record.originalUri != null &&
                    !record.verifiedAssetStages.containsAssetStage(
                        PublishedAssetKind.ORIGINAL,
                        AssetPublishStage.VERIFY_PUBLISHED,
                    )
                ) {
                    recoveryStage = SaveStage.ORIGINAL_PUBLISH
                    val publishedUri = Uri.parse(record.originalUri)
                    runCatching {
                        PublishedAssetVerifier.verifyPublished(
                            resolver,
                            publishedUri,
                            record.displayName,
                            CaptureSaver.RELATIVE_DIR,
                            motionPhoto,
                        )
                    }.onSuccess {
                        record = record.copy(
                            completedStages = record.completedStages + SaveStage.ORIGINAL_PUBLISH.name,
                            verifiedAssetStages = record.verifiedAssetStages + assetStageKey(
                                PublishedAssetKind.ORIGINAL,
                                AssetPublishStage.VERIFY_PUBLISHED,
                            ),
                        )
                        journalStore.write(record)
                    }.onFailure {
                        CaptureSaver.deleteQuietly(resolver, publishedUri)
                        record = record.copy(
                            originalUri = null,
                            completedStages = record.completedStages - SaveStage.ORIGINAL_PUBLISH.name,
                            verifiedAssetStages = record.verifiedAssetStages.withoutAssetStages(PublishedAssetKind.ORIGINAL),
                        )
                        journalStore.write(record)
                    }
                }
                if (record.originalUri == null) {
                    recoveryStage = SaveStage.ORIGINAL_PUBLISH
                    if (primary == null) {
                        CaptureSaver.deleteQuietly(resolver, record.pendingUri?.let(Uri::parse))
                        CaptureSaver.deleteQuietly(resolver, record.derivativePendingUri?.let(Uri::parse))
                        cleanupRecoveredRecord(record)
                        journalStore.delete(record)
                        return@runCatching
                    }
                    val stalePending = record.pendingUri?.let(Uri::parse)
                    val resumed = stalePending?.let { pendingUri ->
                        runCatching {
                            CaptureSaver.resumePending(
                                resolver,
                                pendingUri,
                                primary,
                                record.displayName,
                                motionPhoto = motionPhoto,
                                pendingAlreadyVerified = record.verifiedAssetStages.containsAssetStage(
                                    PublishedAssetKind.ORIGINAL,
                                    AssetPublishStage.VERIFY_PENDING,
                                ),
                                onAssetStage = { stage ->
                                    record = record.copy(
                                        verifiedAssetStages = record.verifiedAssetStages + assetStageKey(
                                            PublishedAssetKind.ORIGINAL,
                                            stage,
                                        ),
                                    )
                                    journalStore.write(record)
                                },
                            )
                        }.onFailure {
                            CaptureSaver.deleteQuietly(resolver, pendingUri)
                            record = record.copy(pendingUri = null)
                            journalStore.write(record)
                        }.getOrNull()
                    }
                    val uri = resumed ?: CaptureSaver.publish(
                        resolver,
                        primary,
                        record.displayName,
                        record.takenAtMillis,
                        motionPhoto = motionPhoto,
                        onAssetStage = { stage ->
                            record = record.copy(
                                verifiedAssetStages = record.verifiedAssetStages + assetStageKey(
                                    PublishedAssetKind.ORIGINAL,
                                    stage,
                                ),
                            )
                            journalStore.write(record)
                        },
                        onPendingCreated = { pendingUri ->
                            record = record.copy(pendingUri = pendingUri.toString())
                            journalStore.write(record)
                        },
                    )
                    record = record.copy(
                        pendingUri = null,
                        originalUri = uri.toString(),
                        completedStages = record.completedStages + SaveStage.ORIGINAL_PUBLISH.name,
                        verifiedAssetStages = record.verifiedAssetStages + assetStageKey(
                            PublishedAssetKind.ORIGINAL,
                            AssetPublishStage.VERIFY_PUBLISHED,
                        ),
                        failedStage = null,
                        error = null,
                    )
                    journalStore.write(record)
                }
                if (SaveStage.RECIPE_WRITE.name !in record.completedStages) {
                    recoveryStage = SaveStage.RECIPE_WRITE
                    val style = runCatching { CreativeStyle.valueOf(record.style) }.getOrDefault(CreativeStyle.ORIGINAL)
                    val edit = EditAdjustment(
                        exposureStops = record.exposureStops,
                        contrast = record.contrast,
                        saturation = record.saturation,
                        temperature = record.temperature,
                        tint = record.tint,
                        fade = record.fade,
                        styleStrength = record.styleStrength,
                    )
                    recipeStore.write(
                        EditRecipe.create(
                            CaptureId(record.captureId),
                            record.sequence,
                            record.takenAtMillis,
                            style,
                            edit,
                            record.motionPhotoRequested && !record.motionPhotoFallback && packaged != null,
                        ),
                    )
                    record = record.copy(completedStages = record.completedStages + SaveStage.RECIPE_WRITE.name)
                    journalStore.write(record)
                }
                if (
                    record.derivativeRequested &&
                    record.derivativeUri != null &&
                    !record.verifiedAssetStages.containsAssetStage(
                        PublishedAssetKind.DERIVATIVE,
                        AssetPublishStage.VERIFY_PUBLISHED,
                    )
                ) {
                    recoveryStage = SaveStage.DERIVATIVE_PUBLISH
                    val publishedUri = Uri.parse(record.derivativeUri)
                    val displayName = CaptureIdentity.displayName(
                        CaptureId(record.captureId),
                        CaptureAssetKind.EFFECT,
                        record.sequence,
                        record.takenAtMillis,
                    )
                    runCatching {
                        PublishedAssetVerifier.verifyPublished(
                            resolver,
                            publishedUri,
                            displayName,
                            CaptureSaver.RELATIVE_DIR,
                            motionPhoto = false,
                        )
                    }.onSuccess {
                        record = record.copy(
                            completedStages = record.completedStages + SaveStage.DERIVATIVE_PUBLISH.name,
                            verifiedAssetStages = record.verifiedAssetStages + assetStageKey(
                                PublishedAssetKind.DERIVATIVE,
                                AssetPublishStage.VERIFY_PUBLISHED,
                            ),
                        )
                        journalStore.write(record)
                    }.onFailure {
                        CaptureSaver.deleteQuietly(resolver, publishedUri)
                        record = record.copy(
                            derivativeUri = null,
                            completedStages = record.completedStages - SaveStage.DERIVATIVE_PUBLISH.name,
                            verifiedAssetStages = record.verifiedAssetStages.withoutAssetStages(PublishedAssetKind.DERIVATIVE),
                        )
                        journalStore.write(record)
                    }
                }
                if (record.derivativeRequested && record.derivativeUri == null && !source.isFile) {
                    recoveryStage = SaveStage.DERIVATIVE_GENERATE
                    record = record.copy(
                        failedStage = SaveStage.DERIVATIVE_GENERATE.name,
                        error = "派生图源文件已丢失；原片和配方已保留，请从原片重新另存效果图",
                    )
                    journalStore.write(record)
                    return@runCatching
                }
                if (record.derivativeRequested && record.derivativeUri == null) {
                    recoveryStage = SaveStage.DERIVATIVE_GENERATE
                    val style = runCatching { CreativeStyle.valueOf(record.style) }.getOrDefault(CreativeStyle.ORIGINAL)
                    val quality = runCatching { DerivativeQuality.valueOf(record.derivativeQuality) }.getOrDefault(DerivativeQuality.FULL)
                    val edit = EditAdjustment(
                        record.exposureStops,
                        record.contrast,
                        record.saturation,
                        record.temperature,
                        record.tint,
                        record.fade,
                        record.styleStrength,
                    )
                    val derivative = record.derivativePath?.let(::File)?.takeIf(File::isFile)
                        ?: creativeProcessor.process(source, style, edit, quality).file
                    record = record.copy(
                        derivativePath = derivative.absolutePath,
                        completedStages = record.completedStages + SaveStage.DERIVATIVE_GENERATE.name,
                    )
                    journalStore.write(record)
                    recoveryStage = SaveStage.DERIVATIVE_PUBLISH
                    val derivativeUri = record.derivativePendingUri?.let(Uri::parse)?.let { pendingUri ->
                        CaptureSaver.resumePending(
                            resolver,
                            pendingUri,
                            derivative,
                            CaptureIdentity.displayName(
                                CaptureId(record.captureId),
                                CaptureAssetKind.EFFECT,
                                record.sequence,
                                record.takenAtMillis,
                            ),
                            pendingAlreadyVerified = record.verifiedAssetStages.containsAssetStage(
                                PublishedAssetKind.DERIVATIVE,
                                AssetPublishStage.VERIFY_PENDING,
                            ),
                            onAssetStage = { stage ->
                                record = record.copy(
                                    verifiedAssetStages = record.verifiedAssetStages + assetStageKey(
                                        PublishedAssetKind.DERIVATIVE,
                                        stage,
                                    ),
                                )
                                journalStore.write(record)
                            },
                        )
                    } ?: CaptureSaver.publish(
                        resolver,
                        derivative,
                        CaptureIdentity.displayName(
                            CaptureId(record.captureId),
                            CaptureAssetKind.EFFECT,
                            record.sequence,
                            record.takenAtMillis,
                        ),
                        record.takenAtMillis,
                        onAssetStage = { stage ->
                            record = record.copy(
                                verifiedAssetStages = record.verifiedAssetStages + assetStageKey(
                                    PublishedAssetKind.DERIVATIVE,
                                    stage,
                                ),
                            )
                            journalStore.write(record)
                        },
                        onPendingCreated = { pendingUri ->
                            record = record.copy(derivativePendingUri = pendingUri.toString())
                            journalStore.write(record)
                        },
                    )
                    derivative.delete()
                    record = record.copy(
                        derivativePendingUri = null,
                        derivativeUri = derivativeUri.toString(),
                        derivativePath = null,
                        completedStages = record.completedStages + SaveStage.DERIVATIVE_PUBLISH.name,
                        verifiedAssetStages = record.verifiedAssetStages + assetStageKey(
                            PublishedAssetKind.DERIVATIVE,
                            AssetPublishStage.VERIFY_PUBLISHED,
                        ),
                    )
                    journalStore.write(record)
                }
                recoveryStage = SaveStage.COMPLETE
                record = record.copy(completedStages = record.completedStages + SaveStage.COMPLETE.name)
                journalStore.write(record)
                cleanupRecoveredRecord(record)
                journalStore.delete(record)
            }.onFailure { error ->
                runCatching {
                    if (recoveryStage == SaveStage.ORIGINAL_PUBLISH && record.originalUri == null) {
                        CaptureSaver.deleteQuietly(resolver, record.pendingUri?.let(Uri::parse))
                        record = record.copy(pendingUri = null)
                    }
                    if (recoveryStage == SaveStage.DERIVATIVE_PUBLISH && record.derivativeUri == null) {
                        CaptureSaver.deleteQuietly(resolver, record.derivativePendingUri?.let(Uri::parse))
                        record = record.copy(derivativePendingUri = null)
                    }
                    journalStore.write(
                        record.copy(
                            failedStage = recoveryStage?.name,
                            error = error.message ?: "恢复保存失败",
                        ),
                    )
                }
            }
        }
    }

    private fun cleanupRecoveredRecord(record: SaveJournal) {
        listOfNotNull(record.sourcePath, record.motionPath, record.packagedPath, record.derivativePath)
            .map(::File)
            .forEach(File::delete)
    }

    private companion object {
        const val MIN_ZOOM_GESTURE_DELTA = 0.005f
        const val MIN_ZOOM_RATIO_DELTA = 0.005f
        const val QUALITY_FOCUS_TIMEOUT_MS = 900L
        const val LIVE_POST_SHUTTER_MS = 1_500L
        const val MIN_STILL_CAPTURE_FREE_BYTES = 24L * 1024L * 1024L
        const val MIN_LIVE_CAPTURE_FREE_BYTES = 64L * 1024L * 1024L
    }

    private data class PendingCapture(
        val file: File,
        val spec: CaptureSpec,
        val onSaved: (CapturedPhoto) -> Unit,
        val onSaveError: (Throwable) -> Unit,
        val onSaveProgress: (SaveSnapshot) -> Unit,
        val shutterElapsedMs: Long,
        val recordingStartedElapsedMs: Long,
        val warnings: MutableList<String> = mutableListOf(),
        var motionFile: File? = null,
        var packagedFile: File? = null,
        var derivativeFile: File? = null,
        var pendingUri: Uri? = null,
        var originalUri: Uri? = null,
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
                derivativeRequested = spec.saveStrategy == SaveStrategy.ORIGINAL_AND_EFFECT &&
                    !CreativeColorMatrix.isIdentity(CreativeColorMatrix.forSelection(spec.style, spec.edit)),
            ),
        ),
    ) {
        val derivativeRequested: Boolean get() = coordinator.snapshot.plan.derivativeRequested
        fun primaryFile(): File = packagedFile?.takeIf(File::isFile) ?: file

        fun toJournal(): SaveJournal = SaveJournal(
            captureId = spec.captureId.value,
            sequence = spec.sequence,
            takenAtMillis = spec.takenAtMillis,
            sourcePath = file.absolutePath,
            motionPath = motionFile?.absolutePath,
            packagedPath = packagedFile?.absolutePath,
            displayName = displayName,
            style = spec.style.name,
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
}

internal enum class LiveVideoTier {
    HD,
    SD,
}

internal val LIVE_VIDEO_TIER_FALLBACK_ORDER: List<LiveVideoTier> =
    listOf(LiveVideoTier.HD, LiveVideoTier.SD)

internal fun stillCaptureOutputFormat(): Int = ImageCapture.OUTPUT_FORMAT_JPEG

private fun LiveVideoTier.toCameraXQuality(): Quality = when (this) {
    LiveVideoTier.HD -> Quality.HD
    LiveVideoTier.SD -> Quality.SD
}

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
