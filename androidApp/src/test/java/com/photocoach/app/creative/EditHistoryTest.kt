package com.photocoach.app.creative

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class EditHistoryTest {
    @Test
    fun `seven parameter history supports undo redo and reset`() {
        val history = EditHistory()
        history.update(EditAdjustment(exposureStops = 0.3f))
        history.update(EditAdjustment(exposureStops = 0.3f, temperature = 0.4f))

        assertEquals(EditAdjustment(exposureStops = 0.3f), history.undo())
        assertTrue(history.canRedo)
        assertEquals(EditAdjustment(exposureStops = 0.3f, temperature = 0.4f), history.redo())
        assertEquals(EditAdjustment(), history.reset())
        assertFalse(history.canReset)
        assertFalse(history.canRedo)
    }

    @Test
    fun `all values are clamped and a new edit clears redo`() {
        val history = EditHistory()
        val clamped = history.update(EditAdjustment(9f, -9f, 9f, -9f, 9f, 9f, 4f))
        assertEquals(EditAdjustment(1f, -0.5f, 1f, -1f, 1f, 1f, 1f), clamped)
        history.undo()
        assertTrue(history.canRedo)
        history.update(EditAdjustment(tint = 0.2f))
        assertFalse(history.canRedo)
    }

    @Test
    fun `continuous updates of one slider undo as one adjustment`() {
        val history = EditHistory()
        history.update(EditAdjustment(fade = 0.1f))
        history.update(EditAdjustment(fade = 0.2f))
        history.update(EditAdjustment(fade = 0.4f))
        assertEquals(EditAdjustment(), history.undo())
    }
}
