package com.photocoach.app.camera

import androidx.camera.extensions.ExtensionMode
import com.photocoach.coach.SuggestedMode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ExtensionPolicyTest {
    @Test
    fun requiresBothAvailabilityAndAnalysis() {
        val onlyAvailable = ExtensionPolicy.resolve(
            SuggestedMode.HDR,
            isAvailable = { true },
            isAnalysisSupported = { false },
        )
        assertEquals(ExtensionChoice.Standard, onlyAvailable)

        val both = ExtensionPolicy.resolve(
            SuggestedMode.HDR,
            isAvailable = { it == ExtensionMode.HDR },
            isAnalysisSupported = { it == ExtensionMode.HDR },
        )
        assertEquals(ExtensionChoice.Enabled(ExtensionMode.HDR), both)
    }

    @Test
    fun photoAndFaceRetouchStayStandard() {
        assertEquals(
            ExtensionChoice.Standard,
            ExtensionPolicy.resolve(SuggestedMode.PHOTO, { true }, { true }),
        )
        val portrait = ExtensionPolicy.resolve(
            SuggestedMode.PORTRAIT,
            isAvailable = { it == ExtensionMode.BOKEH },
            isAnalysisSupported = { it == ExtensionMode.BOKEH },
        )
        assertTrue(portrait is ExtensionChoice.Enabled)
        assertEquals(ExtensionMode.BOKEH, (portrait as ExtensionChoice.Enabled).mode)

        val night = ExtensionPolicy.resolve(
            SuggestedMode.NIGHT,
            isAvailable = { it == ExtensionMode.NIGHT },
            isAnalysisSupported = { it == ExtensionMode.NIGHT },
        )
        assertEquals(ExtensionChoice.Enabled(ExtensionMode.NIGHT), night)
    }
}
