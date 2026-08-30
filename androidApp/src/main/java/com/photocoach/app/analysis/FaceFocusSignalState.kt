package com.photocoach.app.analysis

internal class FaceFocusSignalState {
    var focusOnFace: Boolean = true
        private set

    fun onTap(tappedFace: Boolean) {
        focusOnFace = tappedFace
    }

    fun reset() {
        // Unknown autofocus state is not evidence that the face needs refocusing.
        focusOnFace = true
    }
}
