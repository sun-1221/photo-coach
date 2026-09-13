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
        val factory = javax.xml.parsers.DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
        val manifest = factory.newDocumentBuilder().parse(File("build/intermediates/merged_manifests/debug/processDebugManifest/AndroidManifest.xml"))
        val permissions = manifest.getElementsByTagName("uses-permission")
        val names = (0 until permissions.length).map {
            (permissions.item(it) as org.w3c.dom.Element).getAttributeNS("http://schemas.android.com/apk/res/android", "name")
        }.toSet()
        assertEquals(setOf("android.permission.CAMERA"), names.filter { it.startsWith("android.permission.") }.toSet())
        assertTrue("com.photocoach.app.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION" in names)
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
