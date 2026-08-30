package com.photocoach.app.creative

import com.photocoach.app.camera.CameraCapabilities
import com.photocoach.app.camera.CaptureAspectRatio
import com.photocoach.app.camera.CapturePriority
import com.photocoach.app.camera.CaptureTimer
import com.photocoach.app.camera.SaveStrategy
import com.photocoach.coach.CoarseScene
import com.photocoach.coach.ShotIntent
import com.photocoach.coach.Signals
import com.photocoach.coach.SuggestedMode
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

enum class ParameterTarget {
    EV,
    FOCUS_METERING,
    AE_AF_LOCK,
    CAMERA_MODE,
    FOCAL_LENGTH_DISTANCE,
    STABILITY_TIMER,
    CAPTURE_PREFERENCE,
    BURST,
    COMPOSITION,
    ASPECT_RATIO,
    OUTPUT,
}

sealed interface ParameterAction {
    data class SetEv(val stops: Float) : ParameterAction
    data class FocusOnFace(val lockAfterFocus: Boolean = false) : ParameterAction
    data object UnlockAeAf : ParameterAction
    data class SetMode(val mode: SuggestedMode) : ParameterAction
    data class SetFocal(val cameraId: String) : ParameterAction
    data class SetTimer(val timer: CaptureTimer) : ParameterAction
    data class SetCapturePriority(val priority: CapturePriority) : ParameterAction
    data class SetBurstEnabled(val enabled: Boolean) : ParameterAction
    data class SetCompositionAids(val grid: Boolean, val level: Boolean) : ParameterAction
    data class SetAspectRatio(val ratio: CaptureAspectRatio) : ParameterAction
    data class SetSaveStrategy(val strategy: SaveStrategy) : ParameterAction
}

data class ParameterSuggestion(
    val target: ParameterTarget,
    val title: String,
    val reason: String,
    val actionText: String,
    val action: ParameterAction?,
    val priority: Int,
) {
    val text: String get() = "$reason；$actionText"
}

data class ParameterContext(
    val capabilities: CameraCapabilities,
    val currentEvStops: Float,
    val timer: CaptureTimer,
    val capturePriority: CapturePriority,
    val burstEnabled: Boolean,
    val gridEnabled: Boolean,
    val levelEnabled: Boolean,
    val aspectRatio: CaptureAspectRatio,
    val saveStrategy: SaveStrategy,
    val aeAfLocked: Boolean,
)

object ParameterCoach {
    fun suggest(
        signals: Signals,
        intent: ShotIntent,
        context: ParameterContext,
    ): List<ParameterSuggestion> = buildList {
        if (signals.lensObscured) {
            add(physical(ParameterTarget.COMPOSITION, "检查镜头", "画面持续偏暗且细节很少", "检查镜头是否被挡住，并轻擦镜片", 120))
        }
        if (signals.subjectCutOff) {
            add(physical(ParameterTarget.COMPOSITION, "保全人物", "人物靠近画面边缘并可能被切到", "后退半步，确认头顶和脚边都留有余量", 112))
        }
        if (signals.faceCount == 1 && !signals.focusOnFace) {
            add(
                actionable(
                    ParameterTarget.FOCUS_METERING,
                    "对焦并测光",
                    "当前对焦或测光没有落在脸上",
                    "一键点脸",
                    ParameterAction.FocusOnFace(),
                    106,
                ),
            )
        }
        if (context.aeAfLocked && (signals.faceCount != 1 || !signals.focusOnFace)) {
            add(
                actionable(
                    ParameterTarget.AE_AF_LOCK,
                    "解除对焦和曝光锁定",
                    "人物位置或测光目标已经变化",
                    "一键恢复自动对焦和测光",
                    ParameterAction.UnlockAeAf,
                    104,
                ),
            )
        }

        val evDirection = when {
            signals.faceDarkerThanScene -> 1
            signals.skyOverexposed -> -1
            else -> 0
        }
        exactEvSuggestion(evDirection, context)?.let(::add)

        preferredMode(signals, context.capabilities)?.let { mode ->
            if (mode != context.capabilities.activeMode) {
                add(
                    actionable(
                        ParameterTarget.CAMERA_MODE,
                        "切换${mode.label()}",
                        mode.reason(signals),
                        "一键切到${mode.label()}",
                        ParameterAction.SetMode(mode),
                        92,
                    ),
                )
            }
        }

        if (signals.faceCount == 1 && signals.focusOnFace && signals.poseAvailable && !context.aeAfLocked) {
            add(
                actionable(
                    ParameterTarget.AE_AF_LOCK,
                    "锁住对焦和曝光",
                    "人物位置稳定，锁定可避免重新构图时焦点漂移",
                    "一键点脸并锁定",
                    ParameterAction.FocusOnFace(lockAfterFocus = true),
                    88,
                ),
            )
        }

        if (intent == ShotIntent.CLOSE_UP && signals.faceRatio < 0.12f) {
            val telephoto = context.capabilities.focalPresets
                .filter { it.isTargetValidated && it.relativeZoom > 1.05f }
                .minByOrNull { it.relativeZoom }
            add(
                if (telephoto != null) {
                    actionable(
                        ParameterTarget.FOCAL_LENGTH_DISTANCE,
                        "调整焦段与距离",
                        "人物特写里脸占比偏小",
                        "一键切到 ${telephoto.label}，再按画面前后微调距离",
                        ParameterAction.SetFocal(telephoto.cameraId),
                        82,
                    )
                } else {
                    physical(
                        ParameterTarget.FOCAL_LENGTH_DISTANCE,
                        "走近一点",
                        "人物特写里脸占比偏小，当前没有已验收长焦",
                        "保持当前焦段，向前走一步",
                        82,
                    )
                },
            )
        } else if (intent == ShotIntent.PERSON_WITH_SCENERY && signals.faceRatio < 0.05f) {
            add(physical(ParameterTarget.FOCAL_LENGTH_DISTANCE, "靠近人物", "人物在环境中偏小", "向前走半步，同时把地标留完整", 82))
        }

        val needsStability = signals.coarseScene == CoarseScene.INDOOR || signals.faceDarkerThanScene
        if (needsStability) {
            add(
                if (context.timer == CaptureTimer.OFF) {
                    actionable(
                        ParameterTarget.STABILITY_TIMER,
                        "稳定拍摄",
                        "光线偏弱，按快门时的晃动更明显",
                        "架稳手机并一键启用 3 秒倒计时",
                        ParameterAction.SetTimer(CaptureTimer.THREE_SECONDS),
                        78,
                    )
                } else {
                    physical(ParameterTarget.STABILITY_TIMER, "靠稳手机", "光线偏弱，手抖更容易影响清晰度", "双手夹紧，靠墙或栏杆后再拍", 78)
                },
            )
        }

        if (abs(signals.tiltDegrees) > 3f) {
            add(
                if (!context.levelEnabled) {
                    actionable(
                        ParameterTarget.COMPOSITION,
                        "放平画面",
                        "手机倾斜 ${formatNumber(abs(signals.tiltDegrees))}°",
                        "一键打开水平仪，再沿水平线放平",
                        ParameterAction.SetCompositionAids(context.gridEnabled, true),
                        76,
                    )
                } else {
                    physical(ParameterTarget.COMPOSITION, "放平画面", "手机倾斜 ${formatNumber(abs(signals.tiltDegrees))}°", "沿水平线把手机向反方向转正", 76)
                },
            )
        }

        if (signals.poseAvailable && context.capturePriority != CapturePriority.SPEED) {
            add(
                actionable(
                    ParameterTarget.CAPTURE_PREFERENCE,
                    "抓住动作",
                    "人物动作正在变化",
                    "一键改为拍摄优先",
                    ParameterAction.SetCapturePriority(CapturePriority.SPEED),
                    68,
                ),
            )
        } else if (!signals.poseAvailable && context.capturePriority != CapturePriority.FOCUS) {
            add(
                actionable(
                    ParameterTarget.CAPTURE_PREFERENCE,
                    "优先合焦",
                    "人物相对静止",
                    "一键改为对焦优先",
                    ParameterAction.SetCapturePriority(CapturePriority.FOCUS),
                    66,
                ),
            )
        }

        if (signals.poseAvailable && !context.burstEnabled) {
            add(actionable(ParameterTarget.BURST, "保留动作变化", "动作连续变化时单张容易错过瞬间", "一键开启明确三张连拍", ParameterAction.SetBurstEnabled(true), 60))
        }
        if (intent == ShotIntent.CLOSE_UP && context.aspectRatio != CaptureAspectRatio.FOUR_THREE) {
            add(actionable(ParameterTarget.ASPECT_RATIO, "保留更多裁切空间", "人物特写后期常需要微调构图", "一键改为 4:3", ParameterAction.SetAspectRatio(CaptureAspectRatio.FOUR_THREE), 54))
        }
        if (context.saveStrategy != SaveStrategy.ORIGINAL_WITH_RECIPE) {
            add(actionable(ParameterTarget.OUTPUT, "原片优先", "默认只存原片和配方更省空间", "一键改为效果按需另存", ParameterAction.SetSaveStrategy(SaveStrategy.ORIGINAL_WITH_RECIPE), 48))
        }
    }
        .sortedByDescending(ParameterSuggestion::priority)
        .distinctBy(ParameterSuggestion::target)
        .take(MAX_SUGGESTIONS)

    private fun exactEvSuggestion(direction: Int, context: ParameterContext): ParameterSuggestion? {
        val exposure = context.capabilities.exposure
        if (direction == 0 || !exposure.supported) return null
        val steps = (TARGET_EV_DELTA / exposure.stepStops).roundToInt().coerceAtLeast(1)
        val target = exposure.clamp(context.currentEvStops + direction * steps * exposure.stepStops)
        if (abs(target - context.currentEvStops) < exposure.stepStops / 2f) return null
        val reason = if (direction > 0) "人物脸部比环境暗" else "天空高光偏亮"
        return actionable(
            ParameterTarget.EV,
            "精确调整曝光",
            reason,
            "一键设为 EV ${formatSigned(target)}（每格 ${formatNumber(exposure.stepStops)}）",
            ParameterAction.SetEv(target),
            100,
        )
    }

    private fun preferredMode(signals: Signals, capabilities: CameraCapabilities): SuggestedMode? = when {
        signals.faceDarkerThanScene && signals.skyOverexposed && SuggestedMode.HDR in capabilities.availableModes -> SuggestedMode.HDR
        signals.coarseScene == CoarseScene.INDOOR && SuggestedMode.NIGHT in capabilities.availableModes -> SuggestedMode.NIGHT
        signals.faceCount == 1 && SuggestedMode.PORTRAIT in capabilities.availableModes -> SuggestedMode.PORTRAIT
        else -> null
    }

    private fun SuggestedMode.label(): String = when (this) {
        SuggestedMode.PHOTO -> "普通模式"
        SuggestedMode.PORTRAIT -> "人像模式"
        SuggestedMode.HDR -> "HDR"
        SuggestedMode.NIGHT -> "夜景模式"
    }

    private fun SuggestedMode.reason(signals: Signals): String = when (this) {
        SuggestedMode.HDR -> "人物偏暗且天空偏亮，当前相机支持保留实时分析的 HDR"
        SuggestedMode.NIGHT -> "室内光线偏弱，当前相机支持保留实时分析的夜景模式"
        SuggestedMode.PORTRAIT -> if (signals.faceCount == 1) "检测到单人，当前相机支持保留实时分析的人像模式" else "当前相机支持人像模式"
        SuggestedMode.PHOTO -> "普通模式最稳妥"
    }

    private fun actionable(
        target: ParameterTarget,
        title: String,
        reason: String,
        actionText: String,
        action: ParameterAction,
        priority: Int,
    ) = ParameterSuggestion(target, title, reason, actionText, action, priority)

    private fun physical(
        target: ParameterTarget,
        title: String,
        reason: String,
        actionText: String,
        priority: Int,
    ) = ParameterSuggestion(target, title, reason, actionText, null, priority)

    private fun formatSigned(value: Float): String = if (value >= 0f) "+${formatNumber(value)}" else formatNumber(value)

    private fun formatNumber(value: Float): String {
        return String.format(Locale.US, "%.3f", value)
            .trimEnd('0')
            .trimEnd('.')
    }

    private const val TARGET_EV_DELTA = 0.3f
    private const val MAX_SUGGESTIONS = 3
}
