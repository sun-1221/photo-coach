package com.photocoach.app

/** Capture identity is fixed before scheduling; callback arrival must never select the current photo. */
internal class CaptureSaveFailureHandler(
    private val captureId: String?,
    private val isActive: () -> Boolean,
    private val report: (Throwable, Boolean, String?) -> Unit,
    private val refreshRecovery: () -> Unit,
    private val settled: () -> Unit,
) {
    fun saveError(error: Throwable) = fail(error, isActive())
    fun captureError(error: Throwable) = fail(error, false)
    private fun fail(error: Throwable, retryAvailable: Boolean) {
        report(error, retryAvailable, captureId)
        if (isActive()) {
            refreshRecovery()
            settled()
        }
    }
}

/** Shared production retry wiring, including synchronous rejection and asynchronous save failure. */
internal fun <T> retryCapturedSave(
    captureId: String?,
    begin: () -> Boolean,
    retry: (String, (T) -> Unit, (Throwable) -> Unit) -> Boolean,
    saved: (T) -> Unit,
    failureFor: (String?) -> CaptureSaveFailureHandler,
) {
    val failure = failureFor(captureId)
    if (!begin()) return
    if (captureId == null) {
        failure.captureError(IllegalStateException("待保存照片不可用"))
        return
    }
    if (!retry(captureId, saved, failure::saveError)) {
        failure.captureError(IllegalStateException("待保存照片不可用"))
    }
}
