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

class AppViewModel @JvmOverloads constructor(application: Application,
    private val researchProtocol: com.photocoach.coach.ResearchProtocol = com.photocoach.coach.ResearchProtocol(
        com.photocoach.coach.ResearchCondition.valueOf(BuildConfig.RESEARCH_CONDITION),
        com.photocoach.coach.ResearchScene.valueOf(BuildConfig.RESEARCH_SCENE), BuildConfig.RESEARCH_CONFIG_ID),
    private val eventLogger: ResearchEventLogger = ResearchEventLogger(File(application.filesDir, "research/p-minus-one-events.jsonl")),
    private val elapsedRealtime: () -> Long = SystemClock::elapsedRealtime,
) : AndroidViewModel(application) {
    private val engine = CoachEngine.loadDefault()
    private val guidance = GuidanceSession(initialIntent = researchProtocol.intent, staticResearchCues = researchProtocol.staticCues(), researchPreparationRequired = researchProtocol.enabled, onCueEnded = { cue, reason, _ ->
        recordEvent("cue_ended", reason, cueOverride = cue.sourceId ?: cue.id.name.lowercase())
    })
    private var droppedResearchEvents = 0L
    private val preferences = ViewfinderPreferenceController(application)
    private val initialSettings = if (researchProtocol.enabled) CameraUserSettings.DEFAULT.copy(modePreference = CameraModePreference.PHOTO) else preferences.initialCameraSettings
    private val initialStylePreferences = preferences.initialStylePreferences
    private val sessionStartedAtMs = now()
    private val researchSessionId = java.util.UUID.randomUUID().toString()
    private var cameraReadyRecorded = false
    private var exitRecorded = false
    private var trackedRoundId = -1
    private var roundOperableAtMs = 0L
    private var roundPublished = false
    private var roundTimeoutRecorded = false
    private val captureEvents = com.photocoach.app.research.CaptureEventLedger()
    private val researchEvents = kotlinx.coroutines.channels.Channel<ResearchEvent>(256)
    private val researchWriter = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + Dispatchers.IO)
    private val sensorManager = application.getSystemService(SensorManager::class.java)
    private val gravity = FloatArray(3)
    @Volatile private var displayRotation = 0
    @Volatile private var sensorObservedAtMs: Long? = null
    fun setDisplayRotation(rotation: Int) { displayRotation = rotation }
    private val motionStabilityTracker = DeviceMotionStabilityTracker()
    @Volatile private var handheldStable: Boolean = true
    private var lastEvalAtMs = 0L
    private var lastSignals = Signals()
    internal var acceptedAnalysisFrames: Long = 0
        private set
    private val deliveredAnalysisFrameCount = java.util.concurrent.atomic.AtomicLong()
    internal val deliveredAnalysisFrames: Long get() = deliveredAnalysisFrameCount.get()
    @Volatile internal var lastAnalysisAgeMs: Long? = null
        private set
    private var lastSceneAppliedRound = -1
    private var confirmedEvStops = 0f
    private var zoomUserLocked = false
    private var controlQuietUntilMs = 0L
    private val faceFocusSignal = FaceFocusSignalState()
    private var lastLoggedStage = ""
    private val creativeCapture = CreativeCaptureSession()
    private val releasedOriginals = mutableSetOf<String>()
    private val recoveryInbox = com.photocoach.app.camera.RecoveryInbox()
    private val poseGuidance = PoseGuidanceReducer()
    private val analyzedFrames = MutableSharedFlow<AnalyzedFrame>(
        replay = 1,
        extraBufferCapacity = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private val _ui = MutableStateFlow(
        ViewfinderUi(
            guidance = guidance.snapshot(),
            researchMode = researchProtocol.enabled,
            staticResearch = researchProtocol.condition == com.photocoach.coach.ResearchCondition.STATIC,
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
            tiltDegrees = com.photocoach.app.analysis.displayRoll(gravity[0], gravity[1], gravity[2], displayRotation)
            handheldStable = motionStabilityTracker.update(event.values[0], event.values[1], event.values[2])
            sensorObservedAtMs = if (motionStabilityTracker.isObserved) now() else null
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    init {
        researchWriter.launch {
            for (event in researchEvents) {
                if (!eventLogger.record(event)) _ui.update { it.copy(researchEvidenceIncomplete = true) }
            }
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
            sensorManager.registerListener(tiltListener, it, SensorManager.SENSOR_DELAY_UI)
        }
        viewModelScope.launch(Dispatchers.Main.immediate) {
            analyzedFrames.collect(::reduceFrame)
        }
        viewModelScope.launch(Dispatchers.Main.immediate) {
            while (isActive) {
                delay(100)
                if (_ui.value.researchRoundPreparing) continue
                val before = guidance.snapshot()
                guidance.tick(now())
                  val after = guidance.snapshot()
                  observeResearchRound(after)
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
        faceMetered = faceFocusSignal.faceMetered,
        handheldStable = handheldStable,
        sensorAtMs = sensorObservedAtMs,
        focusAtMs = faceFocusSignal.observedAtMs,
    )

    fun onCameraReady(capabilities: CameraCapabilities) {
        frameSequence.activate(capabilities.analysisSessionId)
        guidance.onAnalysisSession(capabilities.analysisSessionId)
        faceFocusSignal.reset()
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
        observeResearchRound(guidance.snapshot())
        recordEvent(if (cameraReadyRecorded) "camera_rebound" else "camera_ready")
        cameraReadyRecorded = true
    }

    fun onFrame(signals: Signals, overlay: OverlayGeometry) {
        deliveredAnalysisFrameCount.incrementAndGet()
        lastAnalysisAgeMs = signals.observedAtMs?.let { now() - it }
        analyzedFrames.tryEmit(AnalyzedFrame(signals, overlay))
    }

    private val frameSequence = com.photocoach.coach.FrameSequenceGate()
    private fun reduceFrame(frame: AnalyzedFrame) {
        if (_ui.value.researchRoundPreparing) return
        val signals = frame.signals
        val overlay = frame.overlay
        if (!frameSequence.accept(signals.analysisSessionId, signals.captureTimestampNs, signals.observedAtMs, now())) return
        acceptedAnalysisFrames++
        lastSignals = signals
        val nowMs = now()
        if (!researchProtocol.enabled && signals.faceCount == 1) {
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
        val technique = if (!researchProtocol.enabled && currentUi.p1TechniquesEnabled) PhotoTechniqueEngine.suggest(
            signals,
            TechniqueCapabilities(
                calibratedTelephotoLabel = currentUi.focalPresets.firstOrNull { it.isQuickControlAvailable && !it.isDefault }?.label,
                burstEnabled = currentUi.threeShotBurstEnabled,
                intent = snapshot.intent,
            ),
        ) else null
        val raw = if (technique == null) baseRaw else baseRaw.copy(
            cues = (baseRaw.cues + Cue(
                id = technique.cueId,
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
        val poseState = if (!researchProtocol.enabled && _ui.value.selectedPoseCategory != null) poseGuidance.update(signals, nowMs) else PoseGuidanceState.Disabled
        val poseCandidate = (poseState as? PoseGuidanceState.CueActive)?.cue
        guidance.offerOptionalPose(poseCandidate?.text, poseCandidate?.id)
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
                    afLockSupported = currentUi.lockState.af != com.photocoach.app.camera.LockStatus.UNSUPPORTED,
                    aeLockSupported = currentUi.lockState.ae != com.photocoach.app.camera.LockStatus.UNSUPPORTED,
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
        if (researchProtocol.enabled) return
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

    private var playbackEpoch = 0L
    fun playbackToken(): String {
        val s = guidance.snapshot()
        val shown = when(val stage = s.stage) { is GuidanceStage.Action -> stage.shownAtMs; is GuidanceStage.Optional -> stage.shownAtMs; else -> -1L }
        return "${s.roundId}/${s.currentCue?.id}/${s.currentCue?.text}/$shown/$playbackEpoch"
    }

    fun onResearchParametersApplied(generation: Int?) {
        if (!_ui.value.researchRoundPreparing || generation != _ui.value.sceneApply?.generation) return
        guidance.completeResearchPreparation(now())
        _ui.update { it.copy(researchRoundPreparing = false, guidance = guidance.snapshot()) }
        roundOperableAtMs = now()
        recordEvent("round_operable")
    }
    fun setParameterPanelOpen(open: Boolean) {
        if (researchProtocol.enabled) return
        playbackEpoch++
        guidance.setOutputPaused(open, now())
        _ui.update { it.copy(parameterPanelOpen = open, guidance = guidance.snapshot()) }
    }

    fun onPlaybackFinished(token: String? = null) {
        reduceOnMain {
            if (token != null && (token != playbackToken() || _ui.value.parameterPanelOpen || !_ui.value.voiceEnabled)) return@reduceOnMain
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

    fun onPlaybackUnavailable(token: String? = null) {
        reduceOnMain {
            if (token != null && (token != playbackToken() || _ui.value.parameterPanelOpen || !_ui.value.voiceEnabled)) return@reduceOnMain
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
        playbackEpoch++
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
        val defaults = preferences.resetCameraSettings()
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
                evStops = 0f,
                exposurePending = false,
                controlMessage = "相机设置已恢复默认",
                ttsFailed = false,
            )
        }
        emitGuidance()
    }

    fun cameraSettings(): CameraUserSettings = preferences.cameraSettings(_ui.value).let {
        if (!researchProtocol.enabled) it else it.copy(creativeStyle = CreativeStyle.ORIGINAL,
            threeShotBurstEnabled = false, livePhotoEnabled = false, beautyPreset = BeautyPreset.OFF,
            saveStrategy = SaveStrategy.ORIGINAL_WITH_RECIPE)
    }

    fun setCreativeStyle(style: CreativeStyle) {
        if (researchProtocol.enabled) return
        if (_ui.value.guidance.stage is GuidanceStage.Capturing) return
        val updatedPreferences = preferences.recordStyleUse(style)
        _ui.update { state ->
            val result = state.creativeResult
            val suggested = state.styleDiscovery.recommendations.firstOrNull { it.style == style }?.suggestedStrength
                ?: StyleProfiles.forStyle(style).suggestedStrength
            state.copy(
                creativeStyle = style,
                creativeStyleStrength = suggested,
                styleRecent = updatedPreferences.recent,
                styleFavorites = updatedPreferences.favorites,
                creativeResult = result?.copy(message = null),
                controlMessage = "已选择${style.label}，建议强度 ${(suggested * 100).roundToInt()}%；原片仍会保留",
            )
        }
        persistSettings()
    }

    fun setCreativeStyleStrength(strength: Float) {
        if (researchProtocol.enabled) return
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
        if (researchProtocol.enabled) return
        if (_ui.value.guidance.stage is GuidanceStage.Capturing) return
        if (category == null) poseGuidance.disable() else poseGuidance.select(category, now())
        _ui.update { it.copy(selectedPoseCategory = category, poseCueText = null,
            controlMessage = category?.let { "已选择${it.label()}姿势；作为可选单人灵感，不增加必做步骤" } ?: "姿势灵感已关闭") }
    }

    fun setP1TechniquesEnabled(enabled: Boolean) {
        if (researchProtocol.enabled) return
        _ui.update { it.copy(p1TechniquesEnabled = enabled,
            controlMessage = if (enabled) "摄影技巧已开启；只使用可观察证据和已公开能力" else "摄影技巧已关闭") }
    }

    fun toggleCurrentStyleFavorite() {
        if (researchProtocol.enabled) return
        val style = _ui.value.creativeStyle
        if (style == CreativeStyle.ORIGINAL) { showControlMessage("原图始终排第一，无需收藏"); return }
        val favorite = style !in _ui.value.styleFavorites
        val updatedPreferences = preferences.setStyleFavorite(style, favorite)
        _ui.update { it.copy(styleFavorites = updatedPreferences.favorites, styleRecent = updatedPreferences.recent,
            controlMessage = if (favorite) "已收藏${style.label}" else "已取消收藏${style.label}") }
    }

    fun setThreeShotBurstEnabled(enabled: Boolean) {
        if (researchProtocol.enabled) return
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
        if (researchProtocol.enabled) return
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
        if (researchProtocol.enabled) return
        if (_ui.value.guidance.stage is GuidanceStage.Capturing) return
        _ui.update { it.copy(derivativeQuality = quality, controlMessage = "效果副本：${quality.label}") }
        persistSettings()
    }

    fun setLivePhotoEnabled(enabled: Boolean) {
        if (researchProtocol.enabled) return
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
        if (researchProtocol.enabled) return
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
        if (!policy.preserveShutter && creativeCapture.isBurst) creativeCapture.failBurst("系统热保护已暂停批次；降温后需主动继续")
        _ui.update {
            it.copy(
                thermalLevel = level,
                thermalMessage = when (level) {
                    ThermalLevel.NORMAL -> null
                    ThermalLevel.LIGHT -> "设备轻微升温，已降低创意预览负载"
                    ThermalLevel.MODERATE -> "设备升温，已降低姿势与背景分析频率"
                    ThermalLevel.SEVERE -> "设备温度较高，已暂停新 Live 和连拍"
                    ThermalLevel.CRITICAL -> "设备温度过高，已暂停非必要创意处理"
                    ThermalLevel.EMERGENCY, ThermalLevel.SHUTDOWN -> "系统热保护已停止新拍摄；已捕获照片继续保存，降温后请主动拍摄"
                    ThermalLevel.UNKNOWN -> "无法读取热状态，创意功能按保守策略运行"
                },
                controlMessage = if (level == ThermalLevel.NORMAL) it.controlMessage else when {
                    !policy.preserveShutter -> "系统热保护：暂不能拍摄，已捕获原片保留保存"
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
        faceFocusSignal.onResult(success, now())
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

    fun onLockState(state: com.photocoach.app.camera.CameraLockState) {
        _ui.update { it.copy(lockState = state, aeAfLocked = state.bothConfirmed, controlMessage = state.text) }
    }

    fun onFocusUnlocked() {
        faceFocusSignal.reset()
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
        controlQuietUntilMs = now() + CONTROL_QUIET_MS
        val snapped = (stops * 10f).roundToInt() / 10f
        _ui.update { it.copy(evStops = it.exposureCapability.clamp(snapped), exposurePending = true, exposureFailed = false,
            controlMessage = "正在调整曝光") }
    }

    fun onExposureSessionStarted() {
        confirmedEvStops = 0f
        _ui.update { it.copy(exposurePending = it.exposureCapability.supported, exposureFailed = false) }
    }

    fun onExposureResult(result: Result<Float>) {
        result.fold(onSuccess = { stops ->
            confirmedEvStops = stops
            _ui.update { it.copy(evStops = it.exposureCapability.clamp(stops), exposurePending = false, exposureFailed = false,
                controlMessage = if (it.exposurePending && it.controlMessage == "正在调整曝光") "曝光已调整" else it.controlMessage) }
        }, onFailure = {
            _ui.update { it.copy(evStops = it.exposureCapability.clamp(confirmedEvStops), exposurePending = false, exposureFailed = true,
                controlMessage = "曝光调整失败，当前曝光未确认；请重试") }
        })
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

    fun onSceneApplied(preset: QuickFocalPreset?) {
        _ui.update {
            it.copy(
                selectedFocalId = preset?.cameraId ?: it.selectedFocalId,
                controlMessage = when {
                    preset != null && preset.cameraId != it.selectedFocalId -> "已切到 ${preset.label}"
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
        if (_ui.value.researchRoundPreparing) return false
        if (!ThermalPolicy.forLevel(_ui.value.thermalLevel).preserveShutter) return false
        val accepted = guidance.onShutter(now())
        if (!accepted && researchProtocol.enabled) emitGuidance()
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
                      remainingBurstShots = 0,
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
    fun recordCaptureAccepted(spec: CaptureSpec) { captureEvents.accepted(spec.captureId.value); recordEvent("capture_accepted", spec.captureId.value) }

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
        return creativeCapture.nextCaptureSpec(portraitRegion)?.let { spec ->
            val context = guidance.snapshot()
            captureEvents.register(spec.captureId.value, context.roundId, context.intent.name.lowercase(), stageName(context.stage))
            spec.copy(researchContext = if(researchProtocol.enabled) com.photocoach.app.research.ResearchCaptureContext(
                researchSessionId,context.roundId,context.intent.name.lowercase(),researchProtocol.condition.name,
                researchProtocol.scene.name,researchProtocol.configurationId,BuildConfig.VERSION_NAME) else null)
        }
    }

    /** Returns true when the Activity should submit the next shot in the same explicit batch. */
    fun onPhotoCaptured(photo: CapturedPhoto): Boolean {
        if (
            !creativeCapture.owns(photo.captureId) ||
            creativeCapture.isComplete
        ) {
            return false
        }
        val item = creativeCapture.record(photo)
        photo.warning?.let(::showControlMessage)
        recordOriginalPublished(photo.captureId.value)
        if (creativeCapture.hasMoreShots && !creativeCapture.paused) {
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
        if (photo.captureId.value !in releasedOriginals) guidance.onSaved(recommended.displayUri, now())
        _ui.update {
            it.copy(
                guidance = guidance.snapshot(),
                recentPhoto = recommended.displayUri,
                burstProgress = null,
                remainingBurstShots = if (creativeCapture.paused) creativeCapture.remainingCount else 0,
                saveStatusText = saveOutcomeText,
                savePartialSuccess = false,
                creativeResult = CreativeResultUi(
                    photos = photos,
                    recommendedId = recommendedId,
                    selectedId = recommendedId,
                    recommendationReason = reason,
                    isBurst = creativeCapture.isBurst,
                    edit = creativeCapture.resetEditing(recommendedId).edit,
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
        val capturedContext = captureEvents.context(photo.captureId.value)
        recordEvent("save", saveEvent, capturedContext?.roundId, capturedContext?.intent, capturedContext?.stage,
            captureOverride = photo.captureId.value)
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
        val editing = creativeCapture.resetEditing(id)
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
        val derivativeId = com.photocoach.app.creative.CaptureIdentity.create().value
        _ui.update { it.copy(creativeResult = result.copy(exportInProgress = true, exportingId = derivativeId, message = "正在另存新副本")) }
        return CreativeExportRequest(
            source = result.selectedPhoto.originalUri.toUri(),
            derivativeId = derivativeId,
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

    fun beginExportRetry(): String? {
        val result = _ui.value.creativeResult ?: return null
        if (result.exportInProgress) return null
        val id = result.failedExportId ?: return null
        _ui.update { it.copy(creativeResult = result.copy(exportInProgress = true, exportingId = id)) }
        return id
    }

    fun onCreativeExported(copy: ExportedCopy, derivativeId: String? = _ui.value.creativeResult?.exportingId) {
        val result = _ui.value.creativeResult ?: return
        if (result.exportingId != derivativeId) return
        _ui.update {
            it.copy(
                recentPhoto = copy.uri.toString(),
                creativeResult = result.copy(
                    exportInProgress = false,
                    exportingId = null,
                    failedExportId = if (result.failedExportId == derivativeId) null else result.failedExportId,
                    message = listOfNotNull(
                        if (copy.wasDownsampled) "已另存副本（为控制内存已适度降采样），原片未改动" else "已另存副本，原片未改动",
                        copy.warning,
                    ).joinToString("；"),
                ),
            )
        }
    }

    fun onCreativeExportFailed(error: Throwable, derivativeId: String? = _ui.value.creativeResult?.exportingId) {
        val result = _ui.value.creativeResult ?: return
        if (result.exportingId != derivativeId) return
        _ui.update {
            it.copy(creativeResult = result.copy(exportInProgress = false, exportingId = null, failedExportId = derivativeId,
                message = error.message ?: "另存副本失败；可重试原配方或明确另存新副本"))
        }
    }

    fun dismissCreativeResult() {
        _ui.update { it.copy(creativeResultVisible = false) }
    }

    fun onSaveFailed(error: Throwable, retryAvailable: Boolean, captureId: String?) {
        if (captureId in releasedOriginals) {
            val context = captureId?.let(captureEvents::context)
            recordEvent("background_save_failed", captureId, context?.roundId, context?.intent, context?.stage, captureOverride = captureId)
            _ui.update { current ->
                if (captureId == creativeCapture.activeCaptureId) current.copy(savePartialSuccess = true,
                    saveStatusText = "原片已保存；后台副本失败，请在恢复记录中重试",
                    controlMessage = "原片已保存；后台副本失败，请在恢复记录中重试")
                else current.copy(controlMessage = "较早照片的原片已保存；后台副本失败，请在恢复记录中重试")
            }
            return
        }
        if (captureId != null && captureId != creativeCapture.activeCaptureId &&
            (creativeCapture.activeCaptureId != null || creativeCapture.photos.lastOrNull()?.captureId != captureId)) return
        if (retryAvailable) creativeCapture.markSourceCaptured() else creativeCapture.captureFailed()
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
            val message = "连拍已暂停：已捕获 ${creativeCapture.sourceCount} 张 / 已保存 $completed 张：${error.message ?: "保存失败"}"
            creativeCapture.failBurst(message)
            guidance.onSaveFailed(message, retryAvailable)
            _ui.update { it.copy(remainingBurstShots = if (retryAvailable) 0 else creativeCapture.remainingCount) }
            emitGuidance()
            recordCaptureFailure(captureId, if (retryAvailable) "burst_failed_retryable" else "burst_failed")
            return
        }
        guidance.onSaveFailed(error.message ?: "保存失败，请重试", retryAvailable)
        emitGuidance()
        recordCaptureFailure(captureId, if (retryAvailable) "failed_retryable" else "capture_failed")
    }

    private fun recordCaptureFailure(captureId: String?, result: String) {
        val context = captureId?.let(captureEvents::context)
        recordEvent(if(captureEvents.wasAccepted(captureId)) "save" else "capture_rejected", result,
            context?.roundId, context?.intent, context?.stage, captureOverride = captureId)
    }

    private fun recordOriginalPublished(captureId: String) {
        captureEvents.published(captureId)?.let { context ->
            if (context.roundId == trackedRoundId) roundPublished = true
            recordEvent("original_published", captureId, context.roundId, context.intent, context.stage)
        }
    }

    fun onSaveProgress(progress: com.photocoach.app.camera.CaptureSaveProgress) {
        val snapshot = progress.snapshot
        if (SaveStage.ORIGINAL_PUBLISH in snapshot.completed) recordOriginalPublished(progress.captureId)
        if (progress.captureId != creativeCapture.activeCaptureId) return
        if (progress.captureReleased && SaveStage.ORIGINAL_PUBLISH in snapshot.completed &&
            !creativeCapture.isBurst && releasedOriginals.add(progress.captureId)) {
            guidance.onSaved(progress.originalUri, now())
        }
        creativeCapture.markSourceCaptured()
        val nextStage = snapshot.nextStage
        val status = when {
            snapshot.failedStage != null && snapshot.isPartialSuccess ->
                "原片已保存；${snapshot.failedStage.label()}失败，可只重试此阶段"
            snapshot.failedStage != null -> "${snapshot.failedStage.label()}失败"
            snapshot.isComplete -> "保存完成"
            SaveStage.ORIGINAL_PUBLISH in snapshot.completed -> "原片已保存到系统相册；正在${nextStage?.label() ?: "完成保存"}"
            nextStage != null -> "正在${nextStage.label()}"
            else -> "正在保存"
        }
        _ui.update { it.copy(guidance = guidance.snapshot(), saveStatusText = status, savePartialSuccess = snapshot.isPartialSuccess,
            recentPhoto = progress.originalUri ?: it.recentPhoto) }
    }

    fun beginRetrySave(): Boolean {
        val accepted = guidance.onRetrySave()
        if (accepted) {
            creativeCapture.resumeBurstAfterRetry()
            emitGuidance()
            val id = creativeCapture.activeCaptureId
            val context = id?.let(captureEvents::context)
            recordEvent("save_retry", "started", context?.roundId, context?.intent, context?.stage, captureOverride = id)
        }
        return accepted
    }

    fun onInterruptedSaves(records: List<com.photocoach.app.camera.SaveJournal>) {
        recoveryInbox.refresh(records)
        _ui.update { it.copy(interruptedSaveCount = records.size, recoveryRecords = records, recoveryBusy = false) }
    }

    fun showRecovery(open: Boolean) { recoveryInbox.show(open); _ui.update { it.copy(recoveryPanelOpen = open) } }
    fun beginRecoveryRetry(key: String): Boolean {
        if (!recoveryInbox.beginRetry(key)) return false
        val original = _ui.value.recoveryRecords.singleOrNull { it.key == key }
        if(original?.researchContext==null) recordEvent("recovery_context_unavailable", "legacy recovery has no anonymous original-session association")
        _ui.update { it.copy(recoveryBusy = true) }
        return true
    }

    fun abandonSaveFailure() {
        recordEvent("save_failure_abandoned", captureOverride = creativeCapture.activeCaptureId)
        guidance.abandonSaveFailure(now())
        resetPhotoControls()
        emitGuidance()
        _ui.update { it.copy(remainingBurstShots = 0) }
    }

    fun continueBurst(): Boolean {
        if (_ui.value.cameraError != null || !ThermalPolicy.forLevel(_ui.value.thermalLevel).preserveShutter || !creativeCapture.continueRemaining()) return false
        guidance.abandonSaveFailure(now())
        if (!guidance.onShutter(now())) return false
        _ui.update { it.copy(guidance = guidance.snapshot(), remainingBurstShots = 0) }
        return true
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
        if (researchProtocol.enabled || lastSceneAppliedRound >= 0) return
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
                    generation = generation,
                ),
            )
        }
    }

    private fun resetPhotoControls() {
        // Manual exposure survives successive photos in this camera session.
        // User focal choice and once-per-session scene application survive photo rounds.
        faceFocusSignal.reset()
    }

    private fun emitGuidance() {
        _ui.update { it.copy(guidance = guidance.snapshot()) }
        observeResearchRound(guidance.snapshot())
        logStageIfChanged()
    }

    private fun observeResearchRound(snapshot: com.photocoach.coach.GuidanceSnapshot) {
        if (snapshot.stage is GuidanceStage.Initializing) return
        if (trackedRoundId != snapshot.roundId) {
            trackedRoundId = snapshot.roundId
            roundOperableAtMs = now()
            roundPublished = false
            roundTimeoutRecorded = false
            if (researchProtocol.enabled) {
                val defaults = CameraUserSettings.DEFAULT
                confirmedEvStops = 0f
                _ui.update { it.copy(researchRoundPreparing = true, evStops = 0f,
                    flashSetting = FlashSetting.OFF, modePreference = CameraModePreference.PHOTO,
                    captureTimer = defaults.timer, aspectRatio = defaults.aspectRatio, capturePriority = defaults.capturePriority,
                    gridEnabled = defaults.gridEnabled, levelEnabled = defaults.levelEnabled,
                    sceneApply = SceneApplyRequest(SuggestedMode.PHOTO, false, (it.sceneApply?.generation ?: 0) + 1)) }
                return
            }
            recordEvent("round_operable")
        }
        if (_ui.value.researchRoundPreparing) return
        if (!roundPublished && !roundTimeoutRecorded && now() - roundOperableAtMs >= 30_000L) {
            roundTimeoutRecorded = true
            recordEvent("round_timeout", "original_not_published_within_30s")
        }
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
        val snapshot = guidance.snapshot()
        val stageName = "${snapshot.roundId}:${stageName(snapshot.stage)}:${snapshot.currentCue?.sourceId ?: snapshot.currentCue?.id}:${snapshot.currentCue?.text}"
        if (stageName == lastLoggedStage) return
        lastLoggedStage = stageName
        recordEvent("stage")
    }

    private fun recordEvent(type: String, result: String? = null, roundOverride: Int? = null,
        intentOverride: String? = null, stageOverride: String? = null, cueOverride: String? = null, captureOverride: String? = null) {
        if (!researchProtocol.enabled) return
        val snapshot = guidance.snapshot()
        val event = ResearchEvent(
            type = type,
            occurredAtMs = System.currentTimeMillis(),
            elapsedMs = now() - sessionStartedAtMs,
            intent = intentOverride ?: snapshot.intent.name.lowercase(),
            roundId = roundOverride ?: snapshot.roundId,
            stage = stageOverride ?: stageName(snapshot.stage),
            cueId = cueOverride ?: snapshot.currentCue?.let { it.sourceId ?: it.id.name.lowercase() },
            result = result,
            sessionId = researchSessionId,
            condition = researchProtocol.condition.name, scene = researchProtocol.scene.name,
            configurationId = researchProtocol.configurationId, buildVersion = BuildConfig.VERSION_NAME,
            captureId = captureOverride ?: if (type == "capture_accepted" || type == "original_published") result else null,
            previousDroppedEvents = droppedResearchEvents,
        )
        if (!researchEvents.trySend(event).isSuccess) {
            droppedResearchEvents++
            _ui.update { it.copy(researchEvidenceIncomplete = true) }
        }
    }

    fun recordExit() {
        if (exitRecorded) return
        exitRecorded = true
        recordEvent("session_exit", "user_exit")
    }

    private fun persistSettings() {
        if (!researchProtocol.enabled) preferences.save(_ui.value)
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

    private fun now(): Long = elapsedRealtime()

    override fun onCleared() {
        researchEvents.close()
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

private fun SaveStage.label(): String = when (this) {
    SaveStage.SPACE_CHECK -> "检查空间"
    SaveStage.MOTION_PACKAGE -> "打包 Live"
    SaveStage.ORIGINAL_PUBLISH -> "发布原片"
    SaveStage.RECIPE_WRITE -> "保存编辑配方"
    SaveStage.DERIVATIVE_GENERATE -> "生成兼容 SDR 副本"
    SaveStage.DERIVATIVE_PUBLISH -> "发布效果副本"
    SaveStage.COMPLETE -> "完成保存"
}
