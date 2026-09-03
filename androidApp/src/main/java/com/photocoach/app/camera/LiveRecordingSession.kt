package com.photocoach.app.camera

/** Main-thread ownership gate. A retired encoder must never finalize a newer session. */
internal class LiveRecordingSession {
    private var generation = 0L
    private var active: Long? = null
    var startedElapsedMs: Long? = null
        private set

    fun begin(): Long {
        generation += 1
        active = generation
        startedElapsedMs = null
        return generation
    }

    fun owns(token: Long): Boolean = active == token

    fun started(token: Long, elapsedMs: Long) {
        if (owns(token)) startedElapsedMs = elapsedMs
    }

    fun finish(token: Long): Boolean {
        if (!owns(token)) return false
        invalidate()
        return true
    }

    fun invalidate() {
        active = null
        startedElapsedMs = null
    }
}
