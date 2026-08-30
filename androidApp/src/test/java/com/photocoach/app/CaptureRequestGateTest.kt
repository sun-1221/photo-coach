package com.photocoach.app

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CaptureRequestGateTest {
    @Test
    fun `schedule and execution both reject capture or result states`() {
        val gate = CaptureRequestGate()

        assertNull(gate.trySchedule(available.copy(captureInProgress = true)))
        assertNull(gate.trySchedule(available.copy(resultVisible = true)))

        val delayed = gate.trySchedule(available)
        assertNotNull(delayed)
        assertFalse(gate.tryExecute(delayed!!, available.copy(captureInProgress = true)))

        val afterCapture = gate.trySchedule(available)
        assertNotNull(afterCapture)
        assertFalse(gate.tryExecute(afterCapture!!, available.copy(resultVisible = true)))
    }

    @Test
    fun `only one request can be pending and cancellation invalidates its delayed token`() {
        val gate = CaptureRequestGate()
        val first = gate.trySchedule(available)
        assertNotNull(first)

        assertNull(gate.trySchedule(available))
        assertTrue(gate.cancelPending())
        assertFalse(gate.tryExecute(first!!, available))

        val replacement = gate.trySchedule(available)
        assertNotNull(replacement)
        assertTrue(gate.tryExecute(replacement!!, available))
        assertFalse(gate.hasPendingRequest)
    }

    @Test
    fun `save failure and unavailable camera block a new batch`() {
        val gate = CaptureRequestGate()

        assertNull(gate.trySchedule(available.copy(saveFailureVisible = true)))
        assertNull(gate.trySchedule(available.copy(cameraAvailable = false)))
        assertNull(gate.trySchedule(available.copy(shutterEnabled = false)))
    }

    private companion object {
        val available = CaptureAdmissionState(
            cameraAvailable = true,
            shutterEnabled = true,
            captureInProgress = false,
            saveFailureVisible = false,
            resultVisible = false,
        )
    }
}
