package com.photocoach.app.camera

import androidx.camera.extensions.ExtensionMode
import com.photocoach.coach.SuggestedMode

sealed class ExtensionChoice {
    data object Standard : ExtensionChoice()
    data class Enabled(val mode: Int) : ExtensionChoice()
}

object ExtensionPolicy {
    fun extensionMode(requested: SuggestedMode): Int? = when (requested) {
        SuggestedMode.PORTRAIT -> ExtensionMode.BOKEH
        SuggestedMode.HDR -> ExtensionMode.HDR
        SuggestedMode.NIGHT -> ExtensionMode.NIGHT
        SuggestedMode.PHOTO -> null
    }

    fun resolve(
        requested: SuggestedMode,
        isAvailable: (mode: Int) -> Boolean,
        isAnalysisSupported: (mode: Int) -> Boolean,
    ): ExtensionChoice {
        val mode = extensionMode(requested) ?: return ExtensionChoice.Standard
        if (mode == ExtensionMode.FACE_RETOUCH) return ExtensionChoice.Standard
        return if (isAvailable(mode) && isAnalysisSupported(mode)) {
            ExtensionChoice.Enabled(mode)
        } else {
            ExtensionChoice.Standard
        }
    }
}
