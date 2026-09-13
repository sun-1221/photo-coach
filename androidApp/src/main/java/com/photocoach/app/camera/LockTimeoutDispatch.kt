package com.photocoach.app.camera

/** The same scheduled callback is used by CameraLocks and deterministic scheduler tests. */
internal fun scheduleLockTimeout(confirmation: LockConfirmation, token: Long, delayMs: Long,
    schedule: (Long, () -> Unit) -> Unit, notify: (CameraLockState) -> Unit) {
    schedule(delayMs) {
        if (token == confirmation.generation) {
            confirmation.timeout(token)
            notify(confirmation.state) // timeout invalidates old futures; notify the resulting state afterwards.
        }
    }
}
