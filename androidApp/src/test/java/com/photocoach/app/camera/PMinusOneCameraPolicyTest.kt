package com.photocoach.app.camera

import java.io.File
import java.util.Date
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PMinusOneCameraPolicyTest {
    @Test
    fun manifestRequestsCameraOnly() {
        val manifest = File("src/main/AndroidManifest.xml").readText()
        assertTrue(manifest.contains("android.permission.CAMERA"))
        listOf(
            "android.permission.INTERNET",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_MEDIA_IMAGES",
            "android.permission.RECORD_AUDIO",
            "android.permission.ACCESS_FINE_LOCATION",
        ).forEach { assertFalse(manifest.contains(it), "P-1 must not request $it") }
    }

    @Test
    fun flashOffersOffAndAutoOnly() {
        assertEquals(listOf(FlashSetting.OFF, FlashSetting.AUTO), FlashSetting.entries)
    }

    @Test
    fun mediaStoreNameAndDirectoryAreStable() {
        assertTrue(CaptureSaver.newDisplayName(Date(0)).matches(Regex("IMG_\\d{8}_\\d{6}_\\d{3}\\.jpg")))
        assertEquals("DCIM/拍照教练", CaptureSaver.RELATIVE_DIR)
    }
}
