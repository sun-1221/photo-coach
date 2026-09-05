package com.photocoach.app

import android.content.Context
import com.photocoach.app.camera.CameraSettingsStore
import com.photocoach.app.camera.CameraUserSettings
import com.photocoach.app.creative.CreativeStyle
import com.photocoach.app.creative.StylePreferenceStore
import com.photocoach.app.creative.StylePreferences

/** Single persistence boundary for camera settings and non-identifying style preferences. */
internal class ViewfinderPreferenceController(context: Context) {
    private val cameraSettings = CameraSettingsStore(context)
    private val stylePreferences = StylePreferenceStore(context)

    val initialCameraSettings: CameraUserSettings = cameraSettings.load()
    val initialStylePreferences: StylePreferences = stylePreferences.load()

    fun save(state: ViewfinderUi) {
        cameraSettings.save(cameraSettings(state))
    }

    fun cameraSettings(state: ViewfinderUi): CameraUserSettings = state.toCameraUserSettings()

    fun resetCameraSettings(): CameraUserSettings = cameraSettings.reset()

    fun recordStyleUse(style: CreativeStyle): StylePreferences = stylePreferences.recordUse(style)

    fun setStyleFavorite(style: CreativeStyle, favorite: Boolean): StylePreferences =
        stylePreferences.setFavorite(style, favorite)
}

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
