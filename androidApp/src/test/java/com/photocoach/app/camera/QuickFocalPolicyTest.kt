package com.photocoach.app.camera

import androidx.camera.core.CameraSelector
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class QuickFocalPolicyTest {
    @Test
    fun discoversOnlyRearCameraXEntriesWithStandardFocalMetadata() {
        val presets = QuickFocalPolicy.discover(
            candidates = listOf(
                candidate("rear-wide", 0.5f, listOf(3.2f)),
                candidate("rear-main", 1f, listOf(6.7f)),
                candidate("rear-tele", 3.2f, listOf(21.4f)),
                candidate("rear-no-metadata", 2f, emptyList()),
                candidate("front", 1f, listOf(5f), CameraSelector.LENS_FACING_FRONT),
            ),
            rearLensFacing = CameraSelector.LENS_FACING_BACK,
        )

        assertEquals(listOf("rear-wide", "rear-main", "rear-tele"), presets.map { it.cameraId })
        assertEquals(listOf("0.5×", "1×", "3.2×"), presets.map { it.label })
        assertTrue(presets.single { it.cameraId == "rear-main" }.isDefault)
        assertEquals(listOf("rear-main"), QuickFocalPolicy.quickControls(presets).map { it.cameraId })
        assertEquals(
            listOf("rear-wide", "rear-tele"),
            QuickFocalPolicy.calibrationCandidates(presets).map { it.cameraId },
        )
        assertFalse(presets.single { it.cameraId == "rear-main" }.isTargetValidated)
    }

    @Test
    fun targetCalibrationPromotesOnlyTheVerifiedCandidateToAQuickControl() {
        val presets = QuickFocalPolicy.discover(
            candidates = listOf(
                candidate("rear-wide", 0.5f, listOf(3.2f)),
                candidate("rear-main", 1f, listOf(6.7f)),
                candidate("rear-tele", 3.2f, listOf(21.4f)),
            ),
            rearLensFacing = CameraSelector.LENS_FACING_BACK,
            targetVerifier = TargetFocalVerifier { candidate, relativeZoom ->
                candidate.focalLengthsMm.single() > 20f && relativeZoom > 3f
            },
        )

        assertEquals(listOf("rear-main", "rear-tele"), QuickFocalPolicy.quickControls(presets).map { it.cameraId })
        assertTrue(presets.single { it.cameraId == "rear-tele" }.isTargetValidated)
        assertEquals("rear-tele", QuickFocalPolicy.preferredTelephoto(presets)?.cameraId)
        assertEquals("rear-main", QuickFocalPolicy.safestSelection(presets, "rear-wide")?.cameraId)
    }

    @Test
    fun unavailableOrStaleSelectionFallsBackToDefaultWithoutInventingAPreset() {
        val presets = QuickFocalPolicy.discover(
            listOf(candidate("rear-main", 1f, listOf(6.7f))),
            CameraSelector.LENS_FACING_BACK,
        )

        assertEquals("rear-main", QuickFocalPolicy.safestSelection(presets, "missing")?.cameraId)
        assertNull(QuickFocalPolicy.preferredTelephoto(presets))
    }

    private fun candidate(
        id: String,
        zoom: Float,
        focalLengths: List<Float>,
        facing: Int = CameraSelector.LENS_FACING_BACK,
    ) = CameraCandidate(id, facing, zoom, focalLengths)
}
