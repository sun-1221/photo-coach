package com.photocoach.app.tts

/** Main-thread ownership, acquired before speak/onStart and revoked before stop. */
internal class SpeechCompletionGate {
    private var active: String? = null
    private var callback: ((Boolean) -> Unit)? = null
    fun begin(id: String, completed: (Boolean) -> Unit) { active = id; callback = completed }
    fun cancel() { active = null; callback = null }
    fun complete(id: String?, failed: Boolean) {
        if (id == null || id != active) return
        val finished = callback
        cancel()
        finished?.invoke(failed)
    }
}
