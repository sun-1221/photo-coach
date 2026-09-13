package com.photocoach.app.camera

import android.content.Context
import com.photocoach.app.beauty.BeautyPreset
import androidx.core.content.edit
import com.photocoach.coach.SuggestedMode
import com.photocoach.app.creative.CreativeStyle

enum class CaptureTimer(val seconds: Int, val label: String) {
    OFF(0, "关闭"),
    THREE_SECONDS(3, "3 秒"),
    TEN_SECONDS(10, "10 秒"),
}

enum class CaptureAspectRatio(val label: String) {
    FOUR_THREE("4:3"),
    SIXTEEN_NINE("16:9"),
}

enum class CapturePriority(val label: String) {
    FOCUS("画质优先"),
    SPEED("速度优先"),
}

enum class SaveStrategy(val label: String) {
    ORIGINAL_WITH_RECIPE("原片优先（效果按需另存）"),
    ORIGINAL_AND_EFFECT("原片 + 效果自动保存"),
}

enum class DerivativeQuality(val label: String) {
    FULL("完整质量兼容副本"),
    SPACE_SAVER("省空间兼容副本"),
}

enum class CameraModePreference(val label: String, val requestedMode: SuggestedMode?) {
    AUTO("自动", null),
    PHOTO("普通", SuggestedMode.PHOTO),
    PORTRAIT("人像", SuggestedMode.PORTRAIT),
    HDR("HDR", SuggestedMode.HDR),
    NIGHT("夜景", SuggestedMode.NIGHT),
}

data class CameraUserSettings(
    val voiceEnabled: Boolean = true,
    val subjectCaptionsEnabled: Boolean = true,
    val gridEnabled: Boolean = true,
    val levelEnabled: Boolean = true,
    val timer: CaptureTimer = CaptureTimer.OFF,
    val aspectRatio: CaptureAspectRatio = CaptureAspectRatio.FOUR_THREE,
    val capturePriority: CapturePriority = CapturePriority.FOCUS,
    val modePreference: CameraModePreference = CameraModePreference.AUTO,
    val creativeStyle: CreativeStyle = CreativeStyle.ORIGINAL,
    val threeShotBurstEnabled: Boolean = false,
    val saveStrategy: SaveStrategy = SaveStrategy.ORIGINAL_WITH_RECIPE,
    val derivativeQuality: DerivativeQuality = DerivativeQuality.FULL,
    val livePhotoEnabled: Boolean = false,
    val beautyPreset: BeautyPreset = BeautyPreset.OFF,
) {
    companion object {
        val DEFAULT = CameraUserSettings()

        fun restore(
            voiceEnabled: Boolean?,
            subjectCaptionsEnabled: Boolean?,
            gridEnabled: Boolean?,
            levelEnabled: Boolean?,
            timer: String?,
            aspectRatio: String?,
            capturePriority: String?,
            modePreference: String?,
            creativeStyle: String? = null,
            threeShotBurstEnabled: Boolean? = null,
            saveStrategy: String? = null,
            derivativeQuality: String? = null,
            livePhotoEnabled: Boolean? = null,
            beautyPreset: String? = null,
        ): CameraUserSettings = CameraUserSettings(
            voiceEnabled = voiceEnabled ?: DEFAULT.voiceEnabled,
            subjectCaptionsEnabled = subjectCaptionsEnabled ?: DEFAULT.subjectCaptionsEnabled,
            gridEnabled = gridEnabled ?: DEFAULT.gridEnabled,
            levelEnabled = levelEnabled ?: DEFAULT.levelEnabled,
            timer = enumValueOrDefault(timer, DEFAULT.timer),
            aspectRatio = enumValueOrDefault(aspectRatio, DEFAULT.aspectRatio),
            capturePriority = enumValueOrDefault(capturePriority, DEFAULT.capturePriority),
            modePreference = enumValueOrDefault(modePreference, DEFAULT.modePreference),
            creativeStyle = enumValueOrDefault(creativeStyle, DEFAULT.creativeStyle),
            threeShotBurstEnabled = threeShotBurstEnabled ?: DEFAULT.threeShotBurstEnabled,
            saveStrategy = enumValueOrDefault(saveStrategy, DEFAULT.saveStrategy),
            derivativeQuality = enumValueOrDefault(derivativeQuality, DEFAULT.derivativeQuality),
            livePhotoEnabled = livePhotoEnabled ?: DEFAULT.livePhotoEnabled,
            beautyPreset = BeautyPreset.restore(beautyPreset),
        )

        private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String?, fallback: T): T =
            enumValues<T>().firstOrNull { it.name == value } ?: fallback
    }
}

class CameraSettingsStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun load(): CameraUserSettings = CameraUserSettings.restore(
        voiceEnabled = preferences.booleanOrNull(KEY_VOICE_ENABLED),
        subjectCaptionsEnabled = preferences.booleanOrNull(KEY_CAPTIONS_ENABLED),
        gridEnabled = preferences.booleanOrNull(KEY_GRID_ENABLED),
        levelEnabled = preferences.booleanOrNull(KEY_LEVEL_ENABLED),
        timer = preferences.getString(KEY_TIMER, null),
        aspectRatio = preferences.getString(KEY_ASPECT_RATIO, null),
        capturePriority = preferences.getString(KEY_CAPTURE_PRIORITY, null),
        modePreference = preferences.getString(KEY_MODE_PREFERENCE, null),
        creativeStyle = preferences.getString(KEY_CREATIVE_STYLE, null),
        threeShotBurstEnabled = preferences.booleanOrNull(KEY_THREE_SHOT_BURST),
        saveStrategy = preferences.getString(KEY_SAVE_STRATEGY, null),
        derivativeQuality = preferences.getString(KEY_DERIVATIVE_QUALITY, null),
        livePhotoEnabled = preferences.booleanOrNull(KEY_LIVE_PHOTO),
        beautyPreset = preferences.getString(KEY_BEAUTY, null),
    )

    fun save(settings: CameraUserSettings) {
        preferences.edit {
            putBoolean(KEY_VOICE_ENABLED, settings.voiceEnabled)
            putBoolean(KEY_CAPTIONS_ENABLED, settings.subjectCaptionsEnabled)
            putBoolean(KEY_GRID_ENABLED, settings.gridEnabled)
            putBoolean(KEY_LEVEL_ENABLED, settings.levelEnabled)
            putString(KEY_TIMER, settings.timer.name)
            putString(KEY_ASPECT_RATIO, settings.aspectRatio.name)
            putString(KEY_CAPTURE_PRIORITY, settings.capturePriority.name)
            putString(KEY_MODE_PREFERENCE, settings.modePreference.name)
            putString(KEY_CREATIVE_STYLE, settings.creativeStyle.name)
            putBoolean(KEY_THREE_SHOT_BURST, settings.threeShotBurstEnabled)
            putString(KEY_SAVE_STRATEGY, settings.saveStrategy.name)
            putString(KEY_DERIVATIVE_QUALITY, settings.derivativeQuality.name)
            putBoolean(KEY_LIVE_PHOTO, settings.livePhotoEnabled)
            putString(KEY_BEAUTY, settings.beautyPreset.name)
        }
    }

    fun reset(): CameraUserSettings = CameraUserSettings.DEFAULT.also(::save)

    private fun android.content.SharedPreferences.booleanOrNull(key: String): Boolean? =
        if (contains(key)) getBoolean(key, false) else null

    private companion object {
        // Shared with the existing consent preferences; reset writes only camera keys and never clears consent.
        const val PREFS = "photo_coach"
        const val KEY_VOICE_ENABLED = "voice_enabled"
        const val KEY_CAPTIONS_ENABLED = "subject_captions_enabled"
        const val KEY_GRID_ENABLED = "grid_enabled"
        const val KEY_LEVEL_ENABLED = "level_enabled"
        const val KEY_TIMER = "capture_timer"
        const val KEY_ASPECT_RATIO = "capture_aspect_ratio"
        const val KEY_CAPTURE_PRIORITY = "capture_priority"
        const val KEY_MODE_PREFERENCE = "mode_preference"
        const val KEY_CREATIVE_STYLE = "creative_style"
        const val KEY_THREE_SHOT_BURST = "three_shot_burst"
        const val KEY_SAVE_STRATEGY = "save_strategy"
        const val KEY_DERIVATIVE_QUALITY = "derivative_quality"
        const val KEY_LIVE_PHOTO = "live_photo_enabled"
        const val KEY_BEAUTY = "beauty_preset"
    }
}
