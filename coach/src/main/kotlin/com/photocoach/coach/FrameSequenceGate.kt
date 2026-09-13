package com.photocoach.coach

/** Capture identity is independent of callback delivery time. No reused or out-of-order frame counts. */
class FrameSequenceGate(private val maximumGapMs: Long = 1_500L) {
    private var session: Long? = null
    private var timestamp: Long? = null
    var restarted: Boolean = false; private set
    fun activate(sessionId: Long) { session = sessionId; timestamp = null; restarted = true }
    fun accept(sessionId: Long?, captureNs: Long?, observedMs: Long?, nowMs: Long): Boolean {
        restarted = false
        if (sessionId == null || captureNs == null) return true // legacy platform-free callers
        if (session != null && sessionId < session!!) return false
        if (session != sessionId) { session = sessionId; timestamp = null; restarted = true }
        if (timestamp != null && captureNs <= timestamp!!) return false
        val gap = timestamp?.let { (captureNs - it) / 1_000_000L }
        timestamp = captureNs
        if (observedMs == null || nowMs - observedMs !in 0..maximumGapMs) { restarted = true; return false }
        if (gap != null && gap > maximumGapMs) restarted = true
        return true
    }
}
