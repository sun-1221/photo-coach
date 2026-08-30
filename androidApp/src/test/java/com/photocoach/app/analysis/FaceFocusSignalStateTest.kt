package com.photocoach.app.analysis

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FaceFocusSignalStateTest {
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
