package com.photocoach.app.camera

/**
 * Defers a camera rebind while a capture/save operation owns the camera pipeline.
 */
class ThermalRebindGate {
    private var pending = false

    fun onThermalChanged(cameraOperationInProgress: Boolean): Boolean {
        if (cameraOperationInProgress) {
            pending = true
            return false
        }
        pending = false
        return true
    }

    fun onCameraOperationSettled(): Boolean {
        if (!pending) return false
        pending = false
        return true
    }
}
