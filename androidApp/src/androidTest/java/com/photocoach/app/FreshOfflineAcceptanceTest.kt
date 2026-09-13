package com.photocoach.app

import android.Manifest
import android.content.pm.PackageManager
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Run separately after pm clear, with emulator networking disabled. */
class FreshOfflineAcceptanceTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    private fun systemPermissionButton(suffix:String) {
        fun find(node:AccessibilityNodeInfo?):AccessibilityNodeInfo? {
            if(node==null)return null
            if(node.viewIdResourceName?.endsWith(":id/$suffix")==true)return node
            repeat(node.childCount){index -> find(node.getChild(index))?.let {return it}}
            return null
        }
        var button:AccessibilityNodeInfo?=null
        compose.waitUntil(10000){button=find(InstrumentationRegistry.getInstrumentation().uiAutomation.rootInActiveWindow);button!=null}
        assertTrue(button!!.performAction(AccessibilityNodeInfo.ACTION_CLICK))
    }
    @Test fun decliningAppConsentDoesNotRequestCameraPermission() {
        compose.mainClock.autoAdvance=false
        val activity=compose.activity
        val vm=ViewModelProvider(activity)[AppViewModel::class.java]
        compose.onNodeWithText("暂不使用").performClick();compose.mainClock.advanceTimeBy(100)
        compose.waitUntil(5000){activity.isFinishing || activity.isDestroyed}
        assertEquals(PackageManager.PERMISSION_DENIED,activity.checkSelfPermission(Manifest.permission.CAMERA))
        assertEquals(0L,vm.deliveredAnalysisFrames)
    }
    @Test fun deniedSystemPermissionCanBeExplicitlyRetried() {
        compose.mainClock.autoAdvance=false
        compose.onNodeWithText("同意并继续").performClick();compose.mainClock.advanceTimeBy(100)
        systemPermissionButton("permission_deny_button")
        compose.waitUntil(10000){compose.mainClock.advanceTimeByFrame();compose.onAllNodesWithText("重新授权").fetchSemanticsNodes().isNotEmpty()}
        assertEquals(PackageManager.PERMISSION_DENIED,compose.activity.checkSelfPermission(Manifest.permission.CAMERA))
        compose.onNodeWithText("重新授权").performClick();compose.mainClock.advanceTimeBy(100)
        systemPermissionButton("permission_allow_foreground_only_button")
        val vm=ViewModelProvider(compose.activity)[AppViewModel::class.java]
        compose.waitUntil(30000){compose.mainClock.advanceTimeByFrame();vm.deliveredAnalysisFrames>0 && vm.ui.value.cameraError==null}
        compose.mainClock.advanceTimeBy(100);compose.onNodeWithTag("shutter").assertIsEnabled()
    }
    @Test fun consentPrecedesPermissionAndFreshOfflineCameraPublishesOriginal() {
        compose.mainClock.autoAdvance=false
        assertEquals(PackageManager.PERMISSION_DENIED,compose.activity.checkSelfPermission(Manifest.permission.CAMERA))
        compose.onNodeWithText("同意并继续").assertIsDisplayed().performClick()
        compose.mainClock.advanceTimeBy(100)
        val automation=InstrumentationRegistry.getInstrumentation().uiAutomation
        fun find(node:AccessibilityNodeInfo?):AccessibilityNodeInfo? {
            if(node==null)return null
            if(node.viewIdResourceName?.endsWith(":id/permission_allow_foreground_only_button")==true)return node
            repeat(node.childCount){index -> find(node.getChild(index))?.let {return it}}
            return null
        }
        var button:AccessibilityNodeInfo?=null
        compose.waitUntil(10000){button=find(automation.rootInActiveWindow);button!=null}
        assertTrue(button!!.performAction(AccessibilityNodeInfo.ACTION_CLICK))
        val vm=ViewModelProvider(compose.activity)[AppViewModel::class.java]
        compose.waitUntil(30000){compose.mainClock.advanceTimeByFrame();vm.deliveredAnalysisFrames>0 && vm.ui.value.cameraError==null}
        compose.mainClock.advanceTimeBy(100)
        assertEquals(PackageManager.PERMISSION_GRANTED,compose.activity.checkSelfPermission(Manifest.permission.CAMERA))
        compose.onNodeWithTag("shutter").assertIsEnabled().performClick()
        compose.waitUntil(20000){compose.mainClock.advanceTimeByFrame();vm.ui.value.recentPhoto!=null}
        val uri=android.net.Uri.parse(vm.ui.value.recentPhoto!!)
        try {
            compose.activity.contentResolver.openInputStream(uri).use {input ->
                val bitmap=android.graphics.BitmapFactory.decodeStream(input);assertNotNull(bitmap);bitmap!!.recycle()}
            val evidence=java.io.File(compose.activity.getExternalFilesDir(null),"acceptance-evidence").apply {mkdirs()}
            java.io.File(evidence,"fresh-offline-camera.txt").writeText("permissionBeforeConsent=denied\npermissionAfterConsent=granted\ndeliveredAnalysisFrames=${vm.deliveredAnalysisFrames}\nacceptedQualityFrames=${vm.acceptedAnalysisFrames}\nrealtime=${compose.activity.cameraTimestampsRealtime}\noriginalDecode=true\n")
        } finally {compose.activity.contentResolver.delete(uri,null,null)}
    }
}
