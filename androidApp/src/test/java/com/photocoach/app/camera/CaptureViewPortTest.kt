package com.photocoach.app.camera

import android.view.Surface
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CaptureViewPortTest {
    @Test
    fun portraitRotationUsesPortraitDimensionsForEveryCaptureRatio() {
        assertEquals(3 to 4, captureViewPortDimensions(CaptureAspectRatio.FOUR_THREE, Surface.ROTATION_0))
        assertEquals(9 to 16, captureViewPortDimensions(CaptureAspectRatio.SIXTEEN_NINE, Surface.ROTATION_180))
    }

    @Test
    fun landscapeRotationUsesLandscapeDimensionsForEveryCaptureRatio() {
        assertEquals(4 to 3, captureViewPortDimensions(CaptureAspectRatio.FOUR_THREE, Surface.ROTATION_90))
        assertEquals(16 to 9, captureViewPortDimensions(CaptureAspectRatio.SIXTEEN_NINE, Surface.ROTATION_270))
    }
}
