package com.photocoach.app.creative

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class CreativeFallbackTest {
    @Test
    fun `effect failure retains original and a visible warning`() {
        val fallback = CreativeFallback.choose(original = "original", effect = null, failureMessage = "decode failed")
        assertEquals("original", fallback.display)
        assertEquals("decode failed", fallback.warning)
    }

    @Test
    fun `successful effect is selected without warning`() {
        val result = CreativeFallback.choose(original = "original", effect = "copy", failureMessage = null)
        assertEquals("copy", result.display)
        assertNull(result.warning)
    }
}
