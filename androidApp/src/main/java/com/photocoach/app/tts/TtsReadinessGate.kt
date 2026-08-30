package com.photocoach.app.tts

internal enum class TtsOfferResult {
    PLAY_NOW,
    QUEUED,
    UNAVAILABLE,
}

internal class TtsReadinessGate<T : Any> {
    private var state = State.INITIALIZING
    private var pending: T? = null

    @Synchronized
    fun offer(request: T): TtsOfferResult = when (state) {
        State.INITIALIZING -> {
            pending = request
            TtsOfferResult.QUEUED
        }
        State.READY -> TtsOfferResult.PLAY_NOW
        State.FAILED -> TtsOfferResult.UNAVAILABLE
    }

    @Synchronized
    fun markReady(): T? {
        if (state != State.INITIALIZING) return null
        state = State.READY
        return pending.also { pending = null }
    }

    @Synchronized
    fun markFailed(): T? {
        state = State.FAILED
        return pending.also { pending = null }
    }

    @Synchronized
    fun cancelPending(): T? = pending.also { pending = null }

    private enum class State { INITIALIZING, READY, FAILED }
}
