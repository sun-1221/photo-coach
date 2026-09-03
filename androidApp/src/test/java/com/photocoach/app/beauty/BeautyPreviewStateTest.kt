package com.photocoach.app.beauty

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class BeautyPreviewStateTest {
    private val mask = BeautyMask(BeautyTransform(),
        List(4) { BeautyEllipse(.5f, .5f, .1f, .1f) },
        List(4) { BeautyEllipse(.2f, .2f, .1f, .1f) })
    private val frame = BeautyFaceFrame(1_000_000_000L, 1, BeautyTransform(), mask)
    private fun state(frame: BeautyFaceFrame? = this.frame, time: Long = 1_100_000_000L,
        transform: BeautyTransform? = BeautyTransform(), enabled: Boolean = true,
        preset: BeautyPreset = BeautyPreset.NATURAL) =
        BeautyPreviewState.resolve(preset, enabled, frame, transform, time)

    @Test fun selectedPresetDoesNotImplyThatBeautyIsActive() {
        assertEquals(BeautyPreviewState.WAITING_FACE, state(frame = null))
        assertEquals(BeautyPreviewState.WAITING_FACE, state(frame = frame.copy(faceCount = 0)))
        assertEquals(BeautyPreviewState.MULTIPLE_FACES, state(frame = frame.copy(faceCount = 2)))
        assertEquals(BeautyPreviewState.UNSUPPORTED_FACE, state(frame = frame.copy(mask = null)))
    }

    @Test fun staleFutureAndInvalidCoordinateFramesNeverReportActive() {
        assertEquals(BeautyPreviewState.STALE_FRAME, state(time = 1_300_000_000L))
        assertEquals(BeautyPreviewState.STALE_FRAME, state(time = 999_000_000L))
        assertEquals(BeautyPreviewState.INVALID_TRANSFORM, state(transform = null))
        assertEquals(BeautyPreviewState.INVALID_TRANSFORM, state(transform = BeautyTransform(a = Float.NaN)))
        assertEquals(BeautyPreviewState.ACTIVE, state())
    }

    @Test fun offAndThermalPauseOverrideAValidFace() {
        assertEquals(BeautyPreviewState.OFF, state(preset = BeautyPreset.OFF))
        assertEquals(BeautyPreviewState.THERMAL_PAUSED, state(enabled = false))
    }
}
