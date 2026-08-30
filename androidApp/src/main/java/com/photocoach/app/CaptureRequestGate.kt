package com.photocoach.app

data class CaptureAdmissionState(
    val cameraAvailable: Boolean,
    val shutterEnabled: Boolean,
    val captureInProgress: Boolean,
    val saveFailureVisible: Boolean,
    val resultVisible: Boolean,
) {
    val blocked: Boolean
        get() = !cameraAvailable ||
            !shutterEnabled ||
            captureInProgress ||
            saveFailureVisible ||
            resultVisible
}

/**
 * Owns one pending user capture request. The caller must check admission both when a request is
 * scheduled and when a delayed request executes; invalidated tokens can never start a later batch.
 */
class CaptureRequestGate {
    private var nextToken = 0L
    private var pendingToken: Long? = null

    val hasPendingRequest: Boolean
        get() = pendingToken != null

    fun trySchedule(state: CaptureAdmissionState): Long? {
        if (state.blocked || pendingToken != null) return null
        return (++nextToken).also { pendingToken = it }
    }

    fun tryExecute(token: Long, state: CaptureAdmissionState): Boolean {
        if (pendingToken != token) return false
        pendingToken = null
        return !state.blocked
    }

    fun cancelPending(): Boolean {
        if (pendingToken == null) return false
        pendingToken = null
        nextToken++
        return true
    }
}
