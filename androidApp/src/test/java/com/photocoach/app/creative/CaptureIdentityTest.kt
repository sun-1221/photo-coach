package com.photocoach.app.creative

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CaptureIdentityTest {
    private val id = CaptureId("abc123def4567890")

    @Test
    fun `captureId names link original effect recipe and burst without collisions`() {
        val original1 = CaptureIdentity.displayName(id, CaptureAssetKind.ORIGINAL, 1, 1_700_000_000_000L)
        val original2 = CaptureIdentity.displayName(id, CaptureAssetKind.ORIGINAL, 2, 1_700_000_000_000L)
        val effect = CaptureIdentity.displayName(id, CaptureAssetKind.EFFECT, 1, 1_700_000_000_000L)

        assertTrue(original1.contains("abc123def4567890"))
        assertTrue(effect.contains("abc123def4567890"))
        assertTrue(original1.contains("_S01_ORIG"))
        assertTrue(original2.contains("_S02_ORIG"))
        assertTrue(effect.contains("_S01_EFFECT"))
        assertNotEquals(original1, original2)
        assertEquals("abc123def4567890_S01.json", CaptureIdentity.recipeFileName(id))
    }

    @Test
    fun `motion photo filename follows official MP suffix pattern`() {
        val name = CaptureIdentity.motionPhotoDisplayName(id, 3, 1_700_000_000_000L)
        assertTrue(name.startsWith("MVIMG_"))
        assertTrue(name.contains("_S03MP.JPG"))
        assertTrue(CaptureIdentity.motionPhotoNamePattern.matches(name))
    }
}
