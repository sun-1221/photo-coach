package com.photocoach.app.camera

enum class LockStatus(val label: String) {
    UNSUPPORTED("不支持"), IDLE("未锁定"), REQUESTING("请求中"),
    CONFIRMED("已锁定"), RELEASING("解除中"), FAILED("未确认，请重试解除"),
}

data class CameraLockState(val af: LockStatus = LockStatus.UNSUPPORTED, val ae: LockStatus = LockStatus.UNSUPPORTED) {
    val bothConfirmed get() = af == LockStatus.CONFIRMED && ae == LockStatus.CONFIRMED
    val canRelease get() = listOf(af, ae).any { it != LockStatus.IDLE && it != LockStatus.UNSUPPORTED }
    val text get() = "对焦：${af.label}；曝光：${ae.label}"
}

/** Ignores obsolete requests; an accepted request alone never confirms exposure lock. */
internal class LockConfirmation {
    var generation = 0L; private set
    var state = CameraLockState(); private set
    private var requested = false
    private var applied = false
    private var afterFrame = Long.MAX_VALUE
    fun reset(af: Boolean, ae: Boolean) {
        generation++
        state = CameraLockState(if (af) LockStatus.IDLE else LockStatus.UNSUPPORTED,
            if (ae) LockStatus.IDLE else LockStatus.UNSUPPORTED)
        applied = false
    }
    fun begin(lock: Boolean): Long {
        generation++; requested = lock; applied = false
        fun next(s: LockStatus) = if (s == LockStatus.UNSUPPORTED) s else if (lock) LockStatus.REQUESTING else LockStatus.RELEASING
        state = CameraLockState(next(state.af), next(state.ae))
        return generation
    }
    fun focus(token: Long, success: Boolean) {
        if (token != generation || state.af == LockStatus.UNSUPPORTED) return
        state = state.copy(af = if (!success) LockStatus.FAILED else if (requested) LockStatus.CONFIRMED else LockStatus.IDLE)
    }
    fun exposureApplied(token: Long, success: Boolean, frame: Long) {
        if (token != generation || state.ae == LockStatus.UNSUPPORTED) return
        applied = success; afterFrame = frame
        if (!success) state = state.copy(ae = LockStatus.FAILED)
    }
    fun result(token: Long, frame: Long, requestLocked: Boolean?, resultLocked: Boolean?, aeLocked: Boolean?) {
        if (token != generation || !applied || frame <= afterFrame || requestLocked != requested) return
        if (resultLocked == requested && aeLocked == requested) state = state.copy(ae = if (requested) LockStatus.CONFIRMED else LockStatus.IDLE)
    }
    fun timeout(token: Long) {
        if (token != generation) return
        fun expired(s: LockStatus) = if (s == LockStatus.REQUESTING || s == LockStatus.RELEASING) LockStatus.FAILED else s
        state = CameraLockState(expired(state.af), expired(state.ae))
        applied = false
        generation++ // Late focus futures and request acknowledgements belong to the expired request.
    }
}
