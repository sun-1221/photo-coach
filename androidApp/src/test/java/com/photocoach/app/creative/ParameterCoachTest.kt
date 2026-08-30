package com.photocoach.app.creative

import com.photocoach.app.camera.CameraCapabilities
import com.photocoach.app.camera.CaptureAspectRatio
import com.photocoach.app.camera.CapturePriority
import com.photocoach.app.camera.CaptureTimer
import com.photocoach.app.camera.ExposureCapability
import com.photocoach.app.camera.QuickFocalPreset
import com.photocoach.app.camera.QuickFocalVerification
import com.photocoach.app.camera.SaveStrategy
import com.photocoach.coach.CoarseScene
import com.photocoach.coach.ShotIntent
import com.photocoach.coach.Signals
import com.photocoach.coach.SuggestedMode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ParameterCoachTest {
    @Test
    fun `priority is stable limited to three and EV uses exact reported step`() {
        val suggestions = ParameterCoach.suggest(
            Signals(
                faceCount = 1,
                faceRatio = 0.08f,
                faceDarkerThanScene = true,
                skyOverexposed = true,
                focusOnFace = false,
                coarseScene = CoarseScene.INDOOR,
                lensObscured = true,
            ),
            ShotIntent.CLOSE_UP,
            context(exposure = ExposureCapability(-2f, 2f, 0.1f)),
        )

        assertEquals(3, suggestions.size)
        assertEquals(suggestions.sortedByDescending(ParameterSuggestion::priority), suggestions)
        assertEquals(ParameterTarget.COMPOSITION, suggestions.first().target)
        val ev = suggestions.single { it.target == ParameterTarget.EV }
        assertEquals(0.3f, assertInstanceOf(ParameterAction.SetEv::class.java, ev.action).stops, 0.0001f)
        assertTrue(ev.text.contains("每格 0.1"))
        val allText = suggestions.joinToString { it.text }
        listOf("ISO", "快门速度", "白平衡", "开尔文").forEach { assertFalse(allText.contains(it), it) }
    }

    @Test
    fun `validated focal and supported extension produce one tap actions`() {
        val tele = QuickFocalPreset("tele", "3.2×", 3.2f, 75f, false, QuickFocalVerification.TARGET_VERIFIED)
        val capabilities = CameraCapabilities(
            focalPresets = listOf(tele),
            availableModes = setOf(SuggestedMode.PHOTO, SuggestedMode.PORTRAIT),
        )
        val suggestions = ParameterCoach.suggest(
            Signals(faceCount = 1, faceRatio = 0.08f, focusOnFace = true),
            ShotIntent.CLOSE_UP,
            context(capabilities = capabilities),
        )

        assertTrue(suggestions.any { it.action == ParameterAction.SetMode(SuggestedMode.PORTRAIT) })
        assertTrue(suggestions.any { it.action == ParameterAction.SetFocal("tele") })
    }

    @Test
    fun `unverified telephoto becomes physical distance advice with reason and no fake action`() {
        val suggestion = ParameterCoach.suggest(
            Signals(faceCount = 1, faceRatio = 0.08f),
            ShotIntent.CLOSE_UP,
            context(),
        ).single { it.target == ParameterTarget.FOCAL_LENGTH_DISTANCE }

        assertTrue(suggestion.reason.contains("没有已验收长焦"))
        assertTrue(suggestion.actionText.contains("向前走一步"))
        assertEquals(null, suggestion.action)
    }

    private fun context(
        capabilities: CameraCapabilities = CameraCapabilities(),
        exposure: ExposureCapability? = null,
    ) = ParameterContext(
        capabilities = if (exposure == null) capabilities else capabilities.copy(exposure = exposure),
        currentEvStops = 0f,
        timer = CaptureTimer.OFF,
        capturePriority = CapturePriority.FOCUS,
        burstEnabled = false,
        gridEnabled = true,
        levelEnabled = true,
        aspectRatio = CaptureAspectRatio.FOUR_THREE,
        saveStrategy = SaveStrategy.ORIGINAL_WITH_RECIPE,
        aeAfLocked = false,
    )
}
