package com.photocoach.app.beauty

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModelStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.photocoach.app.AppViewModel
import com.photocoach.app.camera.CameraCapabilities
import com.photocoach.app.camera.CameraModePreference
import com.photocoach.app.camera.CameraSettingsStore
import com.photocoach.app.camera.DerivativeQuality
import com.photocoach.app.camera.SaveStrategy
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BeautyStateTest {
    private fun withModel(block: (AppViewModel, Application) -> Unit) {
        val application=ApplicationProvider.getApplicationContext<Application>()
        val settings=CameraSettingsStore(application)
        val previous=settings.load()
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            settings.reset()
            val store=ViewModelStore()
            val model=AppViewModel(application)
            store.put("beauty-test",model)
            try { block(model,application) }
            finally { store.clear(); settings.save(previous) }
        }
    }

    @Test fun settingsPersistAndResetWithoutClearingOtherConsent() = withModel { model,app ->
        val preferences=app.getSharedPreferences("photo_coach",Context.MODE_PRIVATE)
        val key="beauty_test_other_consent"
        preferences.edit().putBoolean(key,true).commit()
        try {
            model.setModePreference(CameraModePreference.PHOTO)
            model.setBeautyPreset(BeautyPreset.NATURAL)
            assertEquals(BeautyPreset.NATURAL,CameraSettingsStore(app).load().beautyPreset)
            model.resetCameraSettings()
            assertEquals(BeautyPreset.OFF,CameraSettingsStore(app).load().beautyPreset)
            assertTrue(preferences.getBoolean(key,false))
        } finally { preferences.edit().remove(key).commit() }
    }

    @Test fun incompatibleModesRejectWithoutSilentlyChangingSelection() = withModel { model,_ ->
        model.onCameraReady(CameraCapabilities())
        model.setBeautyPreset(BeautyPreset.NATURAL)
        assertEquals(BeautyPreset.OFF,model.ui.value.beautyPreset)
        assertEquals(CameraModePreference.AUTO,model.ui.value.modePreference)
        model.setModePreference(CameraModePreference.PHOTO)
        model.setLivePhotoEnabled(true)
        model.setBeautyPreset(BeautyPreset.NATURAL)
        assertEquals(BeautyPreset.OFF,model.ui.value.beautyPreset)
        assertTrue(model.ui.value.livePhotoEnabled)
        model.setLivePhotoEnabled(false)
        model.setBeautyPreset(BeautyPreset.NATURAL)
        model.setModePreference(CameraModePreference.AUTO)
        model.setLivePhotoEnabled(true)
        assertEquals(CameraModePreference.PHOTO,model.ui.value.modePreference)
        assertFalse(model.ui.value.livePhotoEnabled)
        assertTrue(model.ui.value.guidance.shutterEnabled)
    }

    @Test fun captureFreezesRecipeEvenWhenPreviewFailsOrSettingsReset() = withModel { model,_ ->
        model.onCameraReady(CameraCapabilities())
        model.setModePreference(CameraModePreference.PHOTO)
        model.setBeautyPreset(BeautyPreset.SOFT)
        model.setSaveStrategy(SaveStrategy.ORIGINAL_AND_EFFECT)
        model.setDerivativeQuality(DerivativeQuality.SPACE_SAVER)
        assertTrue(model.beginCapture())
        val captured=requireNotNull(model.activeCaptureSpec())
        model.setBeautyPreset(BeautyPreset.OFF)
        assertEquals(BeautyPreset.SOFT,model.ui.value.beautyPreset)
        model.onBeautyFallback("测试 GPU 失败")
        model.resetCameraSettings()
        val retry=requireNotNull(model.activeCaptureSpec())
        assertEquals(captured.captureId,retry.captureId)
        assertEquals(BeautyPreset.SOFT,retry.beautyPreset)
        assertEquals(1,retry.beautyEngineVersion)
        assertEquals(SaveStrategy.ORIGINAL_AND_EFFECT,retry.saveStrategy)
        assertEquals(DerivativeQuality.SPACE_SAVER,retry.derivativeQuality)
    }
}
