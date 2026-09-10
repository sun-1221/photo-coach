package com.photocoach.app.analysis

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FaceFocusSignalStateTest {
    @Test
    fun onlySuccessfulFaceMeteringCompletesTheAction() {
        val state = FaceFocusSignalState()
        state.onTap(tappedFace = true)
        assertFalse(state.focusOnFace)
        assertFalse(state.faceMetered)
        state.onResult(success = false)
        assertFalse(state.faceMetered)
        state.onTap(tappedFace = true)
        state.onResult(success = true)
        assertTrue(state.focusOnFace)
        assertTrue(state.faceMetered)
        state.onTap(tappedFace = false)
        state.onResult(success = true)
        assertFalse(state.focusOnFace)
        assertFalse(state.faceMetered)
        state.reset()
        assertFalse(state.faceMetered)
    }

    @Test
    fun unknownInitialStateDoesNotCreateAFakeFocusFaceCue() {
        val state = FaceFocusSignalState()

        assertTrue(state.focusOnFace)
    }

    @Test
    fun explicitTapUpdatesStateAndNewPhotoReturnsToUnknownSatisfiedState() {
        val state = FaceFocusSignalState()
        state.onTap(tappedFace = false)
        assertFalse(state.focusOnFace)

        state.reset()
        assertTrue(state.focusOnFace)
    }
}
