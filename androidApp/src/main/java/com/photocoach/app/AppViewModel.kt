package com.photocoach.app

import android.app.Application
import com.photocoach.app.beauty.BeautyPreset
import com.photocoach.app.beauty.BeautyCompatibilityPolicy
import com.photocoach.app.beauty.BeautyPreviewState
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.core.net.toUri
import com.photocoach.app.analysis.AnalyzerExtras
import com.photocoach.app.analysis.FaceFocusSignalState
import com.photocoach.app.analysis.DeviceMotionStabilityTracker
import com.photocoach.app.analysis.OverlayGeometry
import com.photocoach.app.camera.FlashSetting
import com.photocoach.app.camera.CameraCapabilities
import com.photocoach.app.camera.CameraModePreference
import com.photocoach.app.camera.CameraSettingsStore
import com.photocoach.app.camera.CameraUserSettings
import com.photocoach.app.camera.CaptureAspectRatio
import com.photocoach.app.camera.CapturePriority
import com.photocoach.app.camera.CaptureTimer
import com.photocoach.app.camera.ExposureCapability
import com.photocoach.app.camera.DerivativeQuality
import com.photocoach.app.camera.SaveStrategy
import com.photocoach.app.camera.SaveSnapshot
import com.photocoach.app.camera.SaveStage
import com.photocoach.app.camera.ThermalLevel
import com.photocoach.app.camera.ThermalPolicy
import com.photocoach.app.camera.QuickFocalPreset
import com.photocoach.app.camera.ZoomCapability
import com.photocoach.app.camera.CapturedPhoto
import com.photocoach.app.camera.CaptureSpec
import com.photocoach.app.camera.PartialSaveException
import com.photocoach.app.camera.ExportedCopy
import com.photocoach.app.creative.BurstSession
import com.photocoach.app.creative.CreativeStyle
import com.photocoach.app.creative.EditAdjustment
import com.photocoach.app.creative.EditRecipe
import com.photocoach.app.creative.CaptureId
import com.photocoach.app.creative.ParameterCoach
import com.photocoach.app.creative.ParameterContext
import com.photocoach.app.creative.ParameterSuggestion
import com.photocoach.app.creative.PhotoQualityScore
import com.photocoach.app.creative.CreativeSceneTag
import com.photocoach.app.creative.StyleDiscovery
import com.photocoach.app.creative.StylePreferenceStore
import com.photocoach.app.creative.StyleProfiles
import com.photocoach.app.creative.StyleRecommendationEngine
import com.photocoach.app.creative.StyleRecommendationInput
import com.photocoach.app.creative.NormalizedFaceRegion
import com.photocoach.app.research.ResearchEvent
import com.photocoach.app.research.ResearchEventLogger
import com.photocoach.coach.CoachEngine
import com.photocoach.coach.CoachOutput
import com.photocoach.coach.Cue
import com.photocoach.coach.Channel
import com.photocoach.coach.Audience
import com.photocoach.coach.CueId
import com.photocoach.coach.GuidanceSession
import com.photocoach.coach.GuidanceSnapshot
import com.photocoach.coach.GuidanceStage
import com.photocoach.coach.SceneStartParams
import com.photocoach.coach.ShotIntent
import com.photocoach.coach.Signals
import com.photocoach.coach.SuggestedMode
import com.photocoach.coach.PoseCategory
import com.photocoach.coach.PoseGuidanceReducer
import com.photocoach.coach.PoseGuidanceState
import com.photocoach.coach.PhotoTechniqueEngine
import com.photocoach.coach.TechniqueCapabilities
import com.photocoach.coach.TechniqueCategory
import kotlin.math.roundToInt
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val engine = CoachEngine.loadDefault()
    private val guidance = GuidanceSession()
    private val eventLogger = ResearchEventLogger(File(application.filesDir, "research/p-minus-one-events.jsonl"))
    private val settingsStore = CameraSettingsStore(application)
    private val stylePreferenceStore = StylePreferenceStore(application)
    private val initialSettings = settingsStore.load()
    private val initialStylePreferences = stylePreferenceStore.load()
    private val sessionStartedAtMs = now()
    private val sensorManager = application.getSystemService(SensorManager::class.java)
    private val gravity = FloatArray(3)
    private val motionStabilityTracker = DeviceMotionStabilityTracker()
    @Volatile private var handheldStable: Boolean = true
    private var lastEvalAtMs = 0L
    private var lastSignals = Signals()
    private var lastSceneAppliedRound = -1
    private var evUserLocked = false
    private var zoomUserLocked = false
    private var controlQuietUntilMs = 0L
    private val faceFocusSignal = FaceFocusSignalState()
    private var lastLoggedStage = ""
    private val creativeCapture = CreativeCaptureSession()
    private val poseGuidance = PoseGuidanceReducer()
    private val analyzedFrames = MutableSharedFlow<AnalyzedFrame>(
        replay = 1,
        extraBufferCapacity = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private val _ui = MutableStateFlow(
        ViewfinderUi(
            guidance = guidance.snapshot(),
            voiceEnabled = initialSettings.voiceEnabled,
            subjectCaptionsEnabled = initialSettings.subjectCaptionsEnabled,
            gridEnabled = initialSettings.gridEnabled,
            levelEnabled = initialSettings.levelEnabled,
            captureTimer = initialSettings.timer,
            aspectRatio = initialSettings.aspectRatio,
            capturePriority = initialSettings.capturePriority,
            modePreference = initialSettings.modePreference,
            creativeStyle = initialSettings.creativeStyle,
            creativeStyleStrength = StyleProfiles.forStyle(initialSettings.creativeStyle).suggestedStrength,
            styleFavorites = initialStylePreferences.favorites,
            styleRecent = initialStylePreferences.recent,
            threeShotBurstEnabled = initialSettings.threeShotBurstEnabled,
            saveStrategy = initialSettings.saveStrategy,
            derivativeQuality = initialSettings.derivativeQuality,
            livePhotoEnabled = initialSettings.livePhotoEnabled,
            beautyPreset = initialSettings.beautyPreset,
        ),
    )
    val ui: StateFlow<ViewfinderUi> = _ui

    @Volatile
    var tiltDegrees: Float = 0f
        private set

    private val tiltListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return
            gravity[0] = event.values[0]
            gravity[1] = event.values[1]
            gravity[2] = event.values[2]
            tiltDegrees = Math.toDegrees(kotlin.math.atan2(gravity[0], gravity[1]).toDouble()).toFloat()
            handheldStable = motionStabilityTracker.update(event.values[0], event.values[1], event.values[2])
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    init {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
            sensorManager.registerListener(tiltListener, it, SensorManager.SENSOR_DELAY_UI)
        }
        viewModelScope.launch(Dispatchers.Main.immediate) {
            analyzedFrames.collect(::reduceFrame)
        }
        viewModelScope.launch(Dispatchers.Main.immediate) {
            while (isActive) {
                delay(100)
                val before = guidance.snapshot()
                guidance.tick(now())
                val after = guidance.snapshot()
                if (before != after) {
                    if (after.roundId != before.roundId) resetPhotoControls()
                    emitGuidance()
                    logStageIfChanged()
                }
            }
        }
    }

    fun extras(hasTelephotoPreset: Boolean): AnalyzerExtras = AnalyzerExtras(
        tiltDegrees = tiltDegrees,
        hasTelephotoPreset = hasTelephotoPreset,
        focusOnFace = faceFocusSignal.focusOnFace,
        handheldStable = handheldStable,
    )

    fun onCameraReady(capabilities: CameraCapabilities) {
        guidance.onCameraReady(now())
        val preferredMode = _ui.value.modePreference.requestedMode
        val unsupportedPreference = preferredMode != null && preferredMode !in capabilities.availableModes
        _ui.update {
            it.copy(
                guidance = guidance.snapshot(),
                focalPresets = capabilities.focalPresets,
                selectedFocalId = capabilities.selectedFocalId,
                exposureCapability = capabilities.exposure,
                zoomCapability = capabilities.zoom,
                availableModes = capabilities.availableModes,
                activeMode = capabilities.activeMode,
                evStops = capabilities.exposure.clamp(it.evStops),
                modePreference = if (unsupportedPreference) CameraModePreference.AUTO else it.modePreference,
                focusIndicator = null,
                aeAfLocked = false,
                livePhotoAvailable = capabilities.livePhotoAvailable,
                liveFallbackReason = capabilities.livePhotoFallbackReason,
                cameraError = null,
                controlMessage = when {
                    unsupportedPreference -> "所选模式当前不可用，已回到自动"
                    capabilities.extensionFallback -> "该模式不能保留实时指导，已回到普通拍照"
                    it.livePhotoEnabled && !capabilities.livePhotoAvailable ->
                        capabilities.livePhotoFallbackReason ?: "Live 当前不可用，已回退普通照片"
                    else -> it.controlMessage
                },
            )
        }
        if (unsupportedPreference) persistSettings()
        recordEvent("camera_ready")
    }

    fun onFrame(signals: Signals, overlay: OverlayGeometry) {
        analyzedFrames.tryEmit(AnalyzedFrame(signals, overlay))
    }

    private fun reduceFrame(frame: AnalyzedFrame) {
        val signals = frame.signals
        val overlay = frame.overlay
        lastSignals = signals
        val nowMs = now()
        if (signals.faceCount == 1) {
            val autoIntent = if (
                signals.hasLargeEnvironment && signals.faceRatio in 0.05f..0.12f
            ) {
                ShotIntent.PERSON_WITH_SCENERY
            } else {
                ShotIntent.CLOSE_UP
            }
            guidance.autoSelectIntent(autoIntent)
        }
        val analysisInterval = maxOf(ANALYSIS_INTERVAL_MS, ThermalPolicy.forLevel(_ui.value.thermalLevel).analysisIntervalMs)
        if (nowMs - lastEvalAtMs < analysisInterval) {
            _ui.update { it.copy(overlay = overlay, guidance = guidance.snapshot()) }
            return
        }
        lastEvalAtMs = nowMs
        val snapshot = guidance.snapshot()
        val currentUi = _ui.value
        val baseRaw = engine.evaluate(signals, snapshot.intent)
        val technique = if (currentUi.p1TechniquesEnabled) PhotoTechniqueEngine.suggest(
            signals,
            TechniqueCapabilities(
                calibratedTelephotoLabel = currentUi.focalPresets.firstOrNull { it.isQuickControlAvailable && !it.isDefault }?.label,
                burstEnabled = currentUi.threeShotBurstEnabled,
            ),
        ) else null
        val raw = if (technique == null) baseRaw else baseRaw.copy(
            cues = (baseRaw.cues + Cue(
                id = CueId.P1_TECHNIQUE,
                text = technique.text,
                audience = technique.audience,
                channel = when (technique.category) {
                    TechniqueCategory.LIGHT, TechniqueCategory.NIGHT -> Channel.LIGHT
                    TechniqueCategory.MOTION -> Channel.POSE
                    else -> Channel.COMPOSITION
                },
                priority = 75,
            )).distinctBy(Cue::id).sortedByDescending(Cue::priority).take(3),
        )
        val filtered = if (nowMs < controlQuietUntilMs) {
            raw.copy(
                cues = raw.cues.filterNot {
                    it.id in setOf(CueId.FOCUS_FACE, CueId.LOWER_EXPOSURE, CueId.MOVE_CLOSER)
                },
            )
        } else {
            raw
        }
        guidance.onCandidates(filtered, signals, nowMs)
        val poseState = if (_ui.value.selectedPoseCategory != null) poseGuidance.update(signals, nowMs) else PoseGuidanceState.Disabled
        maybeQueueSceneStart(filtered)
        val parameterSuggestions = ParameterCoach.suggest(
            signals = signals,
            intent = snapshot.intent,
            context = ParameterContext(
                capabilities = CameraCapabilities(
                    focalPresets = currentUi.focalPresets,
                    selectedFocalId = currentUi.selectedFocalId,
                    availableModes = currentUi.availableModes,
                    activeMode = currentUi.activeMode,
                    exposure = currentUi.exposureCapability,
                    zoom = currentUi.zoomCapability,
                    livePhotoAvailable = currentUi.livePhotoAvailable,
                    livePhotoFallbackReason = currentUi.liveFallbackReason,
                ),
                currentEvStops = currentUi.evStops,
                timer = currentUi.captureTimer,
                capturePriority = currentUi.capturePriority,
                burstEnabled = currentUi.threeShotBurstEnabled,
                gridEnabled = currentUi.gridEnabled,
                levelEnabled = currentUi.levelEnabled,
                aspectRatio = currentUi.aspectRatio,
                saveStrategy = currentUi.saveStrategy,
                aeAfLocked = currentUi.aeAfLocked,
            ),
        )
        val styleDiscovery = StyleRecommendationEngine.discover(
            StyleRecommendationInput(
                scene = when {
                    signals.meanLuma?.let { it < 60f } == true -> CreativeSceneTag.NIGHT
                    signals.faceCount == 1 -> CreativeSceneTag.PORTRAIT
                    signals.hasLargeEnvironment -> CreativeSceneTag.TRAVEL
                    else -> CreativeSceneTag.UNKNOWN
                },
                meanLuma = signals.meanLuma,
                faceCount = signals.faceCount,
                highlightRatio = signals.highlightRatio,
                recent = currentUi.styleRecent,
                favorites = currentUi.styleFavorites,
            ),
        )
        _ui.update {
            it.copy(
                coach = filtered,
                overlay = overlay,
                guidance = guidance.snapshot(),
                lensWarning = if (signals.lensObscured) {
                    "镜头可能被挡住或弄脏，请检查"
                } else {
                    null
                },
                parameterSuggestions = parameterSuggestions,
                styleDiscovery = styleDiscovery,
                poseCueText = (poseState as? PoseGuidanceState.CueActive)?.cue?.text,
            )
        }
        logStageIfChanged()
    }

    fun selectIntent(intent: ShotIntent) {
        guidance.selectIntent(intent, now())
        // Intent changes start a fresh observation. Do not silence MOVE_CLOSER here:
        // the 3-second global quiet period previously outlasted the 1.5-second
        // observation window and incorrectly ended at Ready.
        lastEvalAtMs = 0L
        emitGuidance()
        recordEvent("intent_selected", intent.name.lowercase())
    }

    fun skip() {
        if (guidance.skip(now())) {
            emitGuidance()
            recordEvent("guidance_skipped")
            logStageIfChanged()
        }
    }

    fun requestOptional() {
        if (guidance.requestOptional(now())) {
            emitGuidance()
            recordEvent("optional_requested")
            logStageIfChanged()
        }
    }

    fun cueForSpeech() = guidance.takeCueForSpeech(now())

    fun onPlaybackFinished() {
        reduceOnMain {
            guidance.onPlaybackFinished(now())
            emitGuidance()
        }
    }

    fun onPlaybackStarting() {
        reduceOnMain {
            guidance.onPlaybackStarting()
            emitGuidance()
        }
    }

    fun onPlaybackUnavailable() {
        reduceOnMain {
            val active = guidance.snapshot().stage
            val isSubject = when (active) {
                is GuidanceStage.Action -> active.cue.audience == Audience.SUBJECT
                is GuidanceStage.Optional -> active.cue.audience == Audience.SUBJECT
                else -> false
            }
            if (isSubject && !_ui.value.subjectCaptionsEnabled) {
                guidance.onSubjectChannelsUnavailable()
            } else {
                guidance.onPlaybackUnavailable()
            }
            emitGuidance()
        }
    }

    fun markTtsFailed() {
        reduceOnMain { _ui.update { it.copy(ttsFailed = true) } }
    }

    fun dismissTtsFailure() {
        _ui.update { it.copy(ttsFailed = false) }
    }

    fun setPromptSettings(voiceEnabled: Boolean, subjectCaptionsEnabled: Boolean) {
        _ui.update {
            it.copy(
                voiceEnabled = voiceEnabled,
                subjectCaptionsEnabled = subjectCaptionsEnabled,
                ttsFailed = if (voiceEnabled) it.ttsFailed else false,
            )
        }
        if (!subjectCaptionsEnabled) guidance.clearRetainedSubjectCue()
        if (!voiceEnabled && isActiveSubjectPrompt()) {
            if (subjectCaptionsEnabled) {
                guidance.onPlaybackUnavailable()
            } else {
                guidance.onSubjectChannelsUnavailable()
            }
        }
        emitGuidance()
        persistSettings()
    }

    fun setVoiceEnabled(enabled: Boolean) {
        setPromptSettings(enabled, _ui.value.subjectCaptionsEnabled)
    }

    fun setSubjectCaptionsEnabled(enabled: Boolean) {
        setPromptSettings(_ui.value.voiceEnabled, enabled)
    }

    fun setGridEnabled(enabled: Boolean) {
        _ui.update { it.copy(gridEnabled = enabled) }
        persistSettings()
    }

    fun setLevelEnabled(enabled: Boolean) {
        _ui.update { it.copy(levelEnabled = enabled) }
        persistSettings()
    }

    fun setCaptureTimer(timer: CaptureTimer) {
        _ui.update { it.copy(captureTimer = timer) }
        persistSettings()
    }

    fun setAspectRatio(aspectRatio: CaptureAspectRatio) {
        _ui.update { it.copy(aspectRatio = aspectRatio) }
        persistSettings()
    }

    fun setCapturePriority(priority: CapturePriority) {
        _ui.update { it.copy(capturePriority = priority) }
        persistSettings()
    }

    fun setModePreference(preference: CameraModePreference) {
        if (_ui.value.beautyPreset != BeautyPreset.OFF && preference != CameraModePreference.PHOTO) {
            showControlMessage("自然上镜仅支持普通模式，请先关闭自然上镜")
            return
        }
        val requested = preference.requestedMode
        if (requested != null && requested !in _ui.value.availableModes) {
            showControlMessage("当前镜头不支持这个模式")
            return
        }
        _ui.update { it.copy(modePreference = preference) }
        persistSettings()
    }

    fun resetCameraSettings() {
        val defaults = settingsStore.reset()
        if (!defaults.subjectCaptionsEnabled) guidance.clearRetainedSubjectCue()
        _ui.update {
            it.copy(
                voiceEnabled = defaults.voiceEnabled,
                subjectCaptionsEnabled = defaults.subjectCaptionsEnabled,
                gridEnabled = defaults.gridEnabled,
                levelEnabled = defaults.levelEnabled,
                captureTimer = defaults.timer,
                aspectRatio = defaults.aspectRatio,
                capturePriority = defaults.capturePriority,
                modePreference = defaults.modePreference,
                creativeStyle = defaults.creativeStyle,
                threeShotBurstEnabled = defaults.threeShotBurstEnabled,
                saveStrategy = defaults.saveStrategy,
                derivativeQuality = defaults.derivativeQuality,
                livePhotoEnabled = defaults.livePhotoEnabled,
                beautyPreset = defaults.beautyPreset,
                beautyPreviewWarning = null,
                flashSetting = FlashSetting.OFF,
                countdownSeconds = null,
                controlMessage = "相机设置已恢复默认",
                ttsFailed = false,
            )
        }
        emitGuidance()
    }

    fun cameraSettings(): CameraUserSettings = _ui.value.toCameraUserSettings()

    fun setCreativeStyle(style: CreativeStyle) {
        if (_ui.value.guidance.stage is GuidanceStage.Capturing) return
        val preferences = stylePreferenceStore.recordUse(style)
        _ui.update { state ->
            val result = state.creativeResult
            val suggested = state.styleDiscovery.recommendations.firstOrNull { it.style == style }?.suggestedStrength
                ?: StyleProfiles.forStyle(style).suggestedStrength
            state.copy(
                creativeStyle = style,
                creativeStyleStrength = suggested,
                styleRecent = preferences.recent,
                styleFavorites = preferences.favorites,
                creativeResult = result?.copy(message = null),
                controlMessage = "已选择${style.label}，建议强度 ${(suggested * 100).roundToInt()}%；原片仍会保留",
            )
        }
        persistSettings()
    }

    fun setCreativeStyleStrength(strength: Float) {
        if (_ui.value.guidance.stage is GuidanceStage.Capturing) return
        val clamped = strength.coerceIn(0f, 1f)
        _ui.update {
            it.copy(
                creativeStyleStrength = if (it.creativeStyle == CreativeStyle.ORIGINAL) 0f else clamped,
                controlMessage = if (it.creativeStyle == CreativeStyle.ORIGINAL) {
                    "原图不应用风格强度"
                } else {
                    "已将${it.creativeStyle.label}强度设为 ${(clamped * 100).roundToInt()}%；原片仍会保留"
                },
            )
        }
    }

    fun selectPoseCategory(category: PoseCategory?) {
        if (_ui.value.guidance.stage is GuidanceStage.Capturing) return
        if (category == null) poseGuidance.disable() else poseGuidance.select(category, now())
        _ui.update { it.copy(selectedPoseCategory = category, poseCueText = null,
            controlMessage = category?.let { "已选择${it.label()}姿势；作为可选单人灵感，不增加必做步骤" } ?: "姿势灵感已关闭") }
    }

    fun setP1TechniquesEnabled(enabled: Boolean) {
        _ui.update { it.copy(p1TechniquesEnabled = enabled,
            controlMessage = if (enabled) "摄影技巧已开启；只使用可观察证据和已公开能力" else "摄影技巧已关闭") }
    }

    fun toggleCurrentStyleFavorite() {
        val style = _ui.value.creativeStyle
        if (style == CreativeStyle.ORIGINAL) { showControlMessage("原图始终排第一，无需收藏"); return }
        val favorite = style !in _ui.value.styleFavorites
        val preferences = stylePreferenceStore.setFavorite(style, favorite)
        _ui.update { it.copy(styleFavorites = preferences.favorites, styleRecent = preferences.recent,
            controlMessage = if (favorite) "已收藏${style.label}" else "已取消收藏${style.label}") }
    }

    fun setThreeShotBurstEnabled(enabled: Boolean) {
        if (_ui.value.guidance.stage is GuidanceStage.Capturing) return
        _ui.update {
            it.copy(
                threeShotBurstEnabled = enabled,
                controlMessage = if (enabled) "三张连拍已开启：一次固定拍 3 张" else "三张连拍已关闭",
            )
        }
        persistSettings()
    }

    fun setSaveStrategy(strategy: SaveStrategy) {
        if (_ui.value.guidance.stage is GuidanceStage.Capturing) return
        _ui.update {
            it.copy(
                saveStrategy = strategy,
                controlMessage = if (strategy == SaveStrategy.ORIGINAL_WITH_RECIPE) {
                    "默认只保存原片和编辑配方"
                } else {
                    "将自动保存原片和兼容 SDR 效果副本"
                },
            )
        }
        persistSettings()
    }

    fun setDerivativeQuality(quality: DerivativeQuality) {
        if (_ui.value.guidance.stage is GuidanceStage.Capturing) return
        _ui.update { it.copy(derivativeQuality = quality, controlMessage = "效果副本：${quality.label}") }
        persistSettings()
    }

    fun setLivePhotoEnabled(enabled: Boolean) {
        if (_ui.value.guidance.stage is GuidanceStage.Capturing) return
        if (enabled && _ui.value.beautyPreset != BeautyPreset.OFF) {
            showControlMessage("Live 与自然上镜互斥，请先关闭自然上镜")
            return
        }
        _ui.update {
            it.copy(
                livePhotoEnabled = enabled,
                controlMessage = if (enabled) "正在准备无声 Live；不可用时会保存普通照片" else "Live 已关闭",
            )
        }
        persistSettings()
    }

    fun setBeautyPreset(preset: BeautyPreset) {
        if (_ui.value.guidance.stage is GuidanceStage.Capturing) return
        val state = _ui.value
        val rejection = BeautyCompatibilityPolicy.rejection(preset, state.livePhotoEnabled,
            state.modePreference == CameraModePreference.PHOTO)
        if (rejection != null) { showControlMessage(rejection); return }
        _ui.update { it.copy(beautyPreset = preset, beautyPreviewWarning = null,
            beautyPreviewState = if (preset == BeautyPreset.OFF) BeautyPreviewState.OFF else BeautyPreviewState.WAITING_FACE,
            controlMessage = if (preset == BeautyPreset.OFF) "自然上镜已关闭" else
                "自然上镜·${preset.label}：原片仍保留，效果按保存策略另存；仅本机处理") }
        persistSettings()
    }

    fun onBeautyFallback(reason: String) {
        _ui.update { it.copy(beautyPreset = BeautyPreset.OFF, beautyPreviewState = BeautyPreviewState.OFF,
            beautyPreviewWarning = reason, controlMessage = reason) }
        persistSettings()
    }

    fun onBeautyPreviewState(state: BeautyPreviewState) {
        _ui.update { if (it.beautyPreset == BeautyPreset.OFF) it else it.copy(beautyPreviewState = state) }
    }

    fun onThermalLevel(level: ThermalLevel) {
        val policy = ThermalPolicy.forLevel(level)
        _ui.update {
            it.copy(
                thermalLevel = level,
                thermalMessage = when (level) {
                    ThermalLevel.NORMAL -> null
                    ThermalLevel.LIGHT -> "设备轻微升温，已降低创意预览负载"
                    ThermalLevel.MODERATE -> "设备升温，已降低姿势与背景分析频率"
                    ThermalLevel.SEVERE -> "设备温度较高，已暂停新 Live 和连拍"
                    ThermalLevel.CRITICAL -> "设备温度过高，已暂停非必要创意处理"
                    ThermalLevel.UNKNOWN -> "无法读取热状态，创意功能按保守策略运行"
                },
                controlMessage = if (level == ThermalLevel.NORMAL) it.controlMessage else when {
                    !policy.allowNewLive || !policy.allowNewBurst -> "温度降级中；普通预览、快门和原片保存继续可用"
                    else -> it.controlMessage
                },
            )
        }
    }

    fun toggleFlash() {
        val setting = if (_ui.value.flashSetting == FlashSetting.OFF) FlashSetting.AUTO else FlashSetting.OFF
        _ui.update { it.copy(flashSetting = setting, controlMessage = null) }
    }

    fun onFocusStarted(x: Float, y: Float, lockRequested: Boolean) {
        faceFocusSignal.onTap(
            tappedFace = _ui.value.overlay?.faceRects?.any { it.contains(x, y) } == true,
        )
        val generation = (_ui.value.focusIndicator?.generation ?: 0) + 1
        _ui.update {
            it.copy(
                focusIndicator = FocusIndicator(x, y, FocusStatus.FOCUSING, lockRequested, generation),
                aeAfLocked = false,
                showEv = true,
            )
        }
        controlQuietUntilMs = now() + CONTROL_QUIET_MS
    }

    fun onFocusResult(success: Boolean, locked: Boolean) {
        _ui.update { state ->
            state.copy(
                focusIndicator = state.focusIndicator?.copy(
                    status = if (success) FocusStatus.SUCCESS else FocusStatus.FAILED,
                ),
                aeAfLocked = locked,
                controlMessage = when {
                    !success -> "对焦失败，请再点一次"
                    locked -> "对焦和曝光已锁定"
                    else -> "已对焦"
                },
            )
        }
    }

    fun onFocusUnlocked() {
        _ui.update {
            it.copy(
                focusIndicator = null,
                aeAfLocked = false,
                showEv = false,
                controlMessage = "已恢复自动对焦和曝光",
            )
        }
    }

    fun hideFocusControls(generation: Int) {
        if (_ui.value.focusIndicator?.generation != generation) return
        _ui.update {
            it.copy(
                showEv = false,
                focusIndicator = if (it.aeAfLocked) it.focusIndicator else null,
            )
        }
    }

    fun applyUserEv(stops: Float) {
        evUserLocked = true
        controlQuietUntilMs = now() + CONTROL_QUIET_MS
        val snapped = (stops * 10f).roundToInt() / 10f
        _ui.update { it.copy(evStops = it.exposureCapability.clamp(snapped)) }
    }

    fun onExposureApplied(stops: Float) {
        _ui.update { it.copy(evStops = it.exposureCapability.clamp(stops)) }
    }

    fun onUserFocalChanged(preset: QuickFocalPreset) {
        zoomUserLocked = true
        controlQuietUntilMs = now() + CONTROL_QUIET_MS
        _ui.update {
            it.copy(
                selectedFocalId = preset.cameraId,
                controlMessage = "已切到 ${preset.label}",
            )
        }
    }

    fun onUserPinchZoomChanged(zoomRatio: Float) {
        zoomUserLocked = true
        controlQuietUntilMs = now() + CONTROL_QUIET_MS
        val rounded = (zoomRatio * 10f).roundToInt() / 10f
        _ui.update { it.copy(controlMessage = "${rounded}x") }
    }

    fun onSceneApplied(preset: QuickFocalPreset?, evStops: Float?) {
        _ui.update {
            it.copy(
                selectedFocalId = preset?.cameraId ?: it.selectedFocalId,
                evStops = evStops ?: it.evStops,
                controlMessage = when {
                    preset != null && preset.cameraId != it.selectedFocalId -> "已切到 ${preset.label}"
                    evStops != null -> "已设置起始曝光"
                    else -> it.controlMessage
                },
            )
        }
    }

    fun setCountdown(seconds: Int?) {
        _ui.update { it.copy(countdownSeconds = seconds) }
    }

    fun dismissControlMessage() {
        _ui.update { it.copy(controlMessage = null) }
    }

    fun showControlMessage(message: String) {
        _ui.update { it.copy(controlMessage = message) }
    }

    fun onLiveFallback(reason: String) {
        _ui.update {
            it.copy(
                livePhotoAvailable = false,
                liveFallbackReason = reason,
                controlMessage = reason,
            )
        }
    }

    fun beginCapture(): Boolean {
        val accepted = guidance.onShutter(now())
        if (accepted) {
            val thermalPolicy = ThermalPolicy.forLevel(_ui.value.thermalLevel)
            creativeCapture.begin(
                expectedCount = if (_ui.value.threeShotBurstEnabled && thermalPolicy.allowNewBurst) BurstSession.SHOT_COUNT else 1,
                style = _ui.value.creativeStyle,
                settings = cameraSettings(),
                styleStrength = _ui.value.creativeStyleStrength,
                liveRequested = _ui.value.livePhotoEnabled && thermalPolicy.allowNewLive,
            )
            _ui.update {
                it.copy(
                    guidance = guidance.snapshot(),
                    shutterPulse = it.shutterPulse + 1,
                    burstProgress = if (creativeCapture.isBurst) 0 else null,
                    creativeResult = null,
                    creativeResultVisible = false,
                    saveStatusText = "正在捕获原片",
                    savePartialSuccess = false,
                )
            }
            recordEvent("shutter", "accepted")
        }
        return accepted
    }

    fun activeCaptureStyle(): CreativeStyle = creativeCapture.style

    fun activeCaptureSpec(): CaptureSpec? {
        val state = _ui.value
        val overlay = state.overlay
        val face = overlay?.faceRects?.singleOrNull()
        val portraitRegion = if (
            lastSignals.faceCount == 1 && lastSignals.faceReliable && face != null &&
            overlay.canvasWidth > 0 && overlay.canvasHeight > 0
        ) runCatching {
            NormalizedFaceRegion(
                (face.left / overlay.canvasWidth).coerceIn(0f, 1f),
                (face.top / overlay.canvasHeight).coerceIn(0f, 1f),
                (face.right / overlay.canvasWidth).coerceIn(0f, 1f),
                (face.bottom / overlay.canvasHeight).coerceIn(0f, 1f),
            )
        }.getOrNull() else null
        return creativeCapture.nextCaptureSpec(portraitRegion)
    }

    /** Returns true when the Activity should submit the next shot in the same explicit batch. */
    fun onPhotoCaptured(photo: CapturedPhoto): Boolean {
        if (
            _ui.value.guidance.stage !is GuidanceStage.Capturing ||
            creativeCapture.isComplete
        ) {
            return false
        }
        val item = creativeCapture.record(photo)
        photo.warning?.let(::showControlMessage)
        if (creativeCapture.hasMoreShots) {
            _ui.update { it.copy(burstProgress = creativeCapture.capturedCount) }
            return true
        }
        val photos = creativeCapture.photos
        val recommendedId = creativeCapture.recommendedId(item.id)
        val recommended = photos.first { it.id == recommendedId }
        val runnerUp = photos
            .filterNot { it.id == recommendedId }
            .maxByOrNull { it.score.total }
        val reason = recommended.score.reasonComparedWith(runnerUp?.score)
        val liveRequested = creativeCapture.requestedLivePhoto
        val saveOutcomeText = when {
            recommended.isMotionPhoto -> "Live 已保存到系统相册"
            liveRequested -> "Live 未生成，普通照片已保存"
            recommended.beautyPreset != BeautyPreset.OFF && recommended.displayUri == recommended.originalUri ->
                "原片已保存；美颜效果需另存副本"
            recommended.beautyPreset != BeautyPreset.OFF -> "原片与效果副本已保存"
            else -> null
        }
        guidance.onSaved(recommended.displayUri, now())
        _ui.update {
            it.copy(
                guidance = guidance.snapshot(),
                recentPhoto = recommended.displayUri,
                burstProgress = null,
                saveStatusText = saveOutcomeText,
                savePartialSuccess = false,
                creativeResult = CreativeResultUi(
                    photos = photos,
                    recommendedId = recommendedId,
                    selectedId = recommendedId,
                    recommendationReason = reason,
                    isBurst = creativeCapture.isBurst,
                    message = buildList {
                        addAll(photos.mapNotNull(CreativePhotoUi::warning))
                        if (photos.any(CreativePhotoUi::effectWasDownsampled)) {
                            add("效果副本为控制内存已适度降采样")
                        }
                        add("预览为近似效果，导出可能有细微差异")
                    }.distinct().joinToString("；"),
                ),
                creativeResultVisible = false,
            )
        }
        val saveEvent = when {
            creativeCapture.isBurst -> "burst_complete"
            recommended.isMotionPhoto -> "live_success"
            liveRequested -> "jpeg_fallback"
            else -> "success"
        }
        recordEvent("save", saveEvent)
        return false
    }

    fun onSaved(uri: Uri) {
        guidance.onSaved(uri.toString(), now())
        _ui.update {
            it.copy(
                guidance = guidance.snapshot(),
                recentPhoto = uri.toString(),
                saveStatusText = null,
                savePartialSuccess = false,
            )
        }
        recordEvent("save", "success")
    }

    fun selectCreativePhoto(id: String) {
        val result = _ui.value.creativeResult ?: return
        val selected = result.photos.firstOrNull { it.id == id } ?: return
        val editing = creativeCapture.resetEditing()
        _ui.update {
            it.copy(
                recentPhoto = selected.displayUri,
                creativeResult = result.copy(
                    selectedId = id,
                    edit = editing.edit,
                    canUndo = editing.canUndo,
                    canRedo = editing.canRedo,
                    canReset = editing.canReset,
                    compareOriginal = false,
                    message = if (id == result.recommendedId) "已选择推荐照片" else "已按你的选择切换；三张照片都保留",
                ),
            )
        }
    }

    fun openCreativeResult() {
        if (_ui.value.creativeResult == null) return
        _ui.update { it.copy(creativeResultVisible = true) }
    }

    fun updateCreativeEdit(edit: EditAdjustment) {
        val result = _ui.value.creativeResult ?: return
        val editing = creativeCapture.updateEdit(edit)
        _ui.update {
            it.copy(creativeResult = result.copy(edit = editing.edit, canUndo = editing.canUndo, canRedo = editing.canRedo, canReset = editing.canReset, message = null))
        }
    }

    fun undoCreativeEdit() {
        val result = _ui.value.creativeResult ?: return
        val editing = creativeCapture.undoEdit()
        _ui.update {
            it.copy(creativeResult = result.copy(edit = editing.edit, canUndo = editing.canUndo, canRedo = editing.canRedo, canReset = editing.canReset, message = "已撤销上一步"))
        }
    }

    fun redoCreativeEdit() {
        val result = _ui.value.creativeResult ?: return
        val editing = creativeCapture.redoEdit()
        _ui.update {
            it.copy(creativeResult = result.copy(edit = editing.edit, canUndo = editing.canUndo, canRedo = editing.canRedo, canReset = editing.canReset, message = "已重做下一步"))
        }
    }

    fun resetCreativeEdit() {
        val result = _ui.value.creativeResult ?: return
        val editing = creativeCapture.resetEdit()
        _ui.update {
            it.copy(creativeResult = result.copy(edit = editing.edit, canUndo = editing.canUndo, canRedo = editing.canRedo, canReset = editing.canReset, message = "已重置编辑"))
        }
    }

    fun setCompareOriginal(enabled: Boolean) {
        val result = _ui.value.creativeResult ?: return
        _ui.update { it.copy(creativeResult = result.copy(compareOriginal = enabled)) }
    }

    fun beginCreativeExport(): CreativeExportRequest? {
        val result = _ui.value.creativeResult ?: return null
        if (result.exportInProgress) return null
        if (!ThermalPolicy.forLevel(_ui.value.thermalLevel).allowCreativeExport) {
            _ui.update { it.copy(creativeResult = result.copy(message = "设备温度过高，暂不开始新的效果导出；原片不受影响")) }
            return null
        }
        _ui.update { it.copy(creativeResult = result.copy(exportInProgress = true, message = "正在另存副本")) }
        return CreativeExportRequest(
            source = result.selectedPhoto.originalUri.toUri(),
            edit = result.edit,
            captureId = CaptureId(result.selectedPhoto.captureId),
            sequence = result.selectedPhoto.sequence,
            takenAtMillis = creativeCapture.takenAtMillis,
            beautyPreset = result.selectedPhoto.beautyPreset,
            beautyEngineVersion = result.selectedPhoto.beautyEngineVersion,
        )
    }

    fun currentEditRecipe(): EditRecipe? {
        val result = _ui.value.creativeResult ?: return null
        val selected = result.selectedPhoto
        return EditRecipe.create(
            captureId = CaptureId(selected.captureId),
            sequence = selected.sequence,
            takenAtMillis = creativeCapture.takenAtMillis,
            style = _ui.value.creativeStyle,
            edit = result.edit,
            sourceIsMotionPhoto = selected.isMotionPhoto,
            beautyPreset = selected.beautyPreset,
            beautyEngineVersion = selected.beautyEngineVersion,
        )
    }

    fun onCreativeExported(copy: ExportedCopy) {
        val result = _ui.value.creativeResult ?: return
        _ui.update {
            it.copy(
                recentPhoto = copy.uri.toString(),
                creativeResult = result.copy(
                    exportInProgress = false,
                    message = listOfNotNull(
                        if (copy.wasDownsampled) "已另存副本（为控制内存已适度降采样），原片未改动" else "已另存副本，原片未改动",
                        copy.warning,
                    ).joinToString("；"),
                ),
            )
        }
    }

    fun onCreativeExportFailed(error: Throwable) {
        val result = _ui.value.creativeResult ?: return
        _ui.update {
            it.copy(creativeResult = result.copy(exportInProgress = false, message = error.message ?: "另存副本失败，原片未改动"))
        }
    }

    fun dismissCreativeResult() {
        _ui.update { it.copy(creativeResultVisible = false) }
    }

    fun onSaveFailed(error: Throwable, retryAvailable: Boolean) {
        val partial = error as? PartialSaveException
        if (partial != null) {
            _ui.update {
                it.copy(
                    recentPhoto = partial.originalUri.toString(),
                    savePartialSuccess = true,
                    saveStatusText = "原片已保存；只需重试失败阶段",
                )
            }
        }
        if (creativeCapture.isBurst) {
            val completed = creativeCapture.capturedCount + if (partial != null) 1 else 0
            val message = "三张连拍在第 ${completed + 1} 张中断，已保留 $completed 张：${error.message ?: "保存失败"}"
            creativeCapture.failBurst(message)
            guidance.onSaveFailed(message, retryAvailable)
            emitGuidance()
            recordEvent("save", if (retryAvailable) "burst_failed_retryable" else "burst_failed")
            return
        }
        guidance.onSaveFailed(error.message ?: "保存失败，请重试", retryAvailable)
        emitGuidance()
        recordEvent("save", if (retryAvailable) "failed_retryable" else "capture_failed")
    }

    fun onSaveProgress(snapshot: SaveSnapshot) {
        val nextStage = snapshot.nextStage
        val status = when {
            snapshot.failedStage != null && snapshot.isPartialSuccess ->
                "原片已保存；${snapshot.failedStage.label()}失败，可只重试此阶段"
            snapshot.failedStage != null -> "${snapshot.failedStage.label()}失败"
            snapshot.isComplete -> "保存完成"
            nextStage != null -> "正在${nextStage.label()}"
            else -> "正在保存"
        }
        _ui.update { it.copy(saveStatusText = status, savePartialSuccess = snapshot.isPartialSuccess) }
    }

    fun beginRetrySave(): Boolean {
        val accepted = guidance.onRetrySave()
        if (accepted) {
            creativeCapture.resumeBurstAfterRetry()
            emitGuidance()
            recordEvent("save_retry", "started")
        }
        return accepted
    }

    fun abandonSaveFailure() {
        guidance.abandonSaveFailure(now())
        resetPhotoControls()
        emitGuidance()
        recordEvent("save_failure_abandoned")
    }

    fun markCameraError(message: String?) {
        if (message == null) {
            guidance.onCameraReady(now())
        } else {
            guidance.onCameraUnavailable()
        }
        _ui.update { it.copy(guidance = guidance.snapshot(), cameraError = message) }
        if (message != null) recordEvent("camera_error", "visible")
    }

    fun currentEv(): Float = _ui.value.evStops

    fun requestedMode(): SuggestedMode =
        _ui.value.modePreference.requestedMode ?: _ui.value.sceneApply?.mode ?: SuggestedMode.PHOTO

    private fun maybeQueueSceneStart(output: CoachOutput) {
        val snapshot = guidance.snapshot()
        if (snapshot.stage is GuidanceStage.Observing || snapshot.roundId == lastSceneAppliedRound) return
        lastSceneAppliedRound = snapshot.roundId
        val params = output.startParams
        val generation = (_ui.value.sceneApply?.generation ?: 0) + 1
        _ui.update {
            it.copy(
                sceneApply = SceneApplyRequest(
                    mode = params.mode,
                    preferTelephoto = if (zoomUserLocked) null else params.preferTelephoto,
                    evStops = if (evUserLocked) null else params.evBias,
                    generation = generation,
                ),
            )
        }
    }

    private fun resetPhotoControls() {
        evUserLocked = false
        zoomUserLocked = false
        lastSceneAppliedRound = -1
        faceFocusSignal.reset()
    }

    private fun emitGuidance() {
        _ui.update { it.copy(guidance = guidance.snapshot()) }
    }

    private fun reduceOnMain(block: () -> Unit) {
        viewModelScope.launch(Dispatchers.Main.immediate) { block() }
    }

    private fun isActiveSubjectPrompt(): Boolean = when (val current = guidance.snapshot().stage) {
        is GuidanceStage.Action -> current.cue.audience == Audience.SUBJECT
        is GuidanceStage.Optional -> current.cue.audience == Audience.SUBJECT
        else -> false
    }

    private fun logStageIfChanged() {
        val stageName = stageName(guidance.snapshot().stage)
        if (stageName == lastLoggedStage) return
        lastLoggedStage = stageName
        recordEvent("stage")
    }

    private fun recordEvent(type: String, result: String? = null) {
        val snapshot = guidance.snapshot()
        val event = ResearchEvent(
            type = type,
            occurredAtMs = System.currentTimeMillis(),
            elapsedMs = now() - sessionStartedAtMs,
            intent = snapshot.intent.name.lowercase(),
            roundId = snapshot.roundId,
            stage = stageName(snapshot.stage),
            cueId = snapshot.currentCue?.id?.name?.lowercase(),
            result = result,
        )
        viewModelScope.launch(Dispatchers.IO) { eventLogger.record(event) }
    }

    private fun persistSettings() {
        settingsStore.save(_ui.value.toCameraUserSettings())
    }

    private fun stageName(stage: GuidanceStage): String = when (stage) {
        GuidanceStage.Initializing -> "initializing"
        is GuidanceStage.Observing -> "observing"
        is GuidanceStage.Action -> "action_${stage.step.number}"
        is GuidanceStage.Ready -> "ready"
        is GuidanceStage.Optional -> "optional"
        GuidanceStage.Capturing -> "capturing"
        is GuidanceStage.Saved -> "saved"
        is GuidanceStage.SaveFailed -> "save_failed"
    }

    private fun now(): Long = SystemClock.elapsedRealtime()

    override fun onCleared() {
        sensorManager.unregisterListener(tiltListener)
        motionStabilityTracker.reset()
        super.onCleared()
    }

    companion object {
        private const val ANALYSIS_INTERVAL_MS = 300L
        private const val CONTROL_QUIET_MS = 3_000L
    }
}

private fun PoseCategory.label(): String = when (this) {
    PoseCategory.CLOSE_UP -> "特写"
    PoseCategory.HALF_BODY -> "半身"
    PoseCategory.FULL_BODY -> "全身"
    PoseCategory.SEATED -> "坐姿"
    PoseCategory.WALKING -> "走动"
    PoseCategory.SOLO_INTERACTION -> "单人互动"
}

private data class AnalyzedFrame(
    val signals: Signals,
    val overlay: OverlayGeometry,
)

private fun ViewfinderUi.toCameraUserSettings(): CameraUserSettings = CameraUserSettings(
    voiceEnabled = voiceEnabled,
    subjectCaptionsEnabled = subjectCaptionsEnabled,
    gridEnabled = gridEnabled,
    levelEnabled = levelEnabled,
    timer = captureTimer,
    aspectRatio = aspectRatio,
    capturePriority = capturePriority,
    modePreference = modePreference,
    creativeStyle = creativeStyle,
    threeShotBurstEnabled = threeShotBurstEnabled,
    saveStrategy = saveStrategy,
    derivativeQuality = derivativeQuality,
    livePhotoEnabled = livePhotoEnabled,
    beautyPreset = beautyPreset,
)

private fun SaveStage.label(): String = when (this) {
    SaveStage.SPACE_CHECK -> "检查空间"
    SaveStage.MOTION_PACKAGE -> "打包 Live"
    SaveStage.ORIGINAL_PUBLISH -> "发布原片"
    SaveStage.RECIPE_WRITE -> "保存编辑配方"
    SaveStage.DERIVATIVE_GENERATE -> "生成兼容 SDR 副本"
    SaveStage.DERIVATIVE_PUBLISH -> "发布效果副本"
    SaveStage.COMPLETE -> "完成保存"
}
