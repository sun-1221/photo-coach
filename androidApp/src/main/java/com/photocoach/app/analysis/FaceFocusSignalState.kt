package com.photocoach.app.analysis

internal class FaceFocusSignalState {
    var observedAtMs: Long? = null
        private set
    var focusOnFace: Boolean = true
        private set
    var faceMetered: Boolean = false
        private set
    private var tappedFace = false

    fun onTap(tappedFace: Boolean) {
        observedAtMs = null
        this.tappedFace = tappedFace
        focusOnFace = false
        faceMetered = false
    }

    fun onResult(success: Boolean, nowMs: Long? = null) {
        observedAtMs = nowMs
        focusOnFace = tappedFace && success
        faceMetered = focusOnFace
    }

    fun reset() {
        observedAtMs = null
        // Unknown autofocus state is not evidence that the face needs refocusing.
        focusOnFace = true
        faceMetered = false
        tappedFace = false
    }
}
