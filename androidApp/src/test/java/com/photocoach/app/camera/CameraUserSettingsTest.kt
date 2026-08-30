package com.photocoach.app.camera

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class CameraUserSettingsTest {
    @Test
    fun defaultsAreSafeAndRecoverable() {
        assertEquals(CameraUserSettings.DEFAULT, CameraUserSettings.restore(null, null, null, null, null, null, null, null))
        assertEquals(CaptureTimer.OFF, CameraUserSettings.DEFAULT.timer)
        assertEquals(CaptureAspectRatio.FOUR_THREE, CameraUserSettings.DEFAULT.aspectRatio)
        assertEquals(CapturePriority.FOCUS, CameraUserSettings.DEFAULT.capturePriority)
        assertEquals(CameraModePreference.AUTO, CameraUserSettings.DEFAULT.modePreference)
        assertEquals(SaveStrategy.ORIGINAL_WITH_RECIPE, CameraUserSettings.DEFAULT.saveStrategy)
        assertEquals(DerivativeQuality.FULL, CameraUserSettings.DEFAULT.derivativeQuality)
        assertFalse(CameraUserSettings.DEFAULT.livePhotoEnabled)
    }

    @Test
    fun validValuesRestoreAndUnknownValuesFallBackIndependently() {
        val restored = CameraUserSettings.restore(
            voiceEnabled = false,
            subjectCaptionsEnabled = false,
            gridEnabled = false,
            levelEnabled = false,
            timer = CaptureTimer.THREE_SECONDS.name,
            aspectRatio = "unsupported",
            capturePriority = CapturePriority.SPEED.name,
            modePreference = CameraModePreference.NIGHT.name,
        )

        assertFalse(restored.voiceEnabled)
        assertFalse(restored.subjectCaptionsEnabled)
        assertFalse(restored.gridEnabled)
        assertFalse(restored.levelEnabled)
        assertEquals(CaptureTimer.THREE_SECONDS, restored.timer)
        assertEquals(CaptureAspectRatio.FOUR_THREE, restored.aspectRatio)
        assertEquals(CapturePriority.SPEED, restored.capturePriority)
        assertEquals(CameraModePreference.NIGHT, restored.modePreference)
    }
}
