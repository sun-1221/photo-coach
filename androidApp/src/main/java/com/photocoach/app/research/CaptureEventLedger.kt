package com.photocoach.app.research

/** Capture identity, not callback arrival order, owns publication evidence. */
internal class CaptureEventLedger {
    data class Context(val roundId: Int, val intent: String, val stage: String)
    private val rounds = mutableMapOf<String, Context>()
    private val published = mutableSetOf<String>()
    private val accepted = mutableSetOf<String>()
    fun accepted(captureId:String) { accepted.add(captureId) }
    fun wasAccepted(captureId:String?):Boolean = captureId in accepted
    fun register(captureId: String, roundId: Int, intent: String = "unknown", stage: String = "capturing") {
        rounds.putIfAbsent(captureId, Context(roundId, intent, stage))
    }
    fun published(captureId: String): Context? {
        val round = rounds[captureId] ?: return null
        return if (published.add(captureId)) round else null
    }
    fun context(captureId: String): Context? = rounds[captureId]
}
