package com.photocoach.app.ui.viewfinder

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PreviewEffectFallbackTest {
    @Test
    fun `render effect failure clears the effect and reports fallback`() {
        var cleared = false

        val applied = applyEffectWithOriginalFallback(
            applyEffect = { error("render effect failed") },
            clearEffect = { cleared = true },
        )

        assertFalse(applied)
        assertTrue(cleared)
    }
}
