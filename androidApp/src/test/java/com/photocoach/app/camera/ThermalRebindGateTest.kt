package com.photocoach.app.camera

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ThermalRebindGateTest {
    @Test
    fun `thermal rebind is deferred until camera operation settles`() {
        val gate = ThermalRebindGate()

        assertFalse(gate.onThermalChanged(cameraOperationInProgress = true))
        assertTrue(gate.onCameraOperationSettled())
        assertFalse(gate.onCameraOperationSettled())
    }

    @Test
    fun `thermal rebind proceeds immediately while camera is idle`() {
        val gate = ThermalRebindGate()

        assertTrue(gate.onThermalChanged(cameraOperationInProgress = false))
        assertFalse(gate.onCameraOperationSettled())
    }
}
