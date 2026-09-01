package com.photocoach.app

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class ViewfinderActivityTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun manualZoomRespectsTheBoundCameraCapability() {
        if (compose.activity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(
                compose.activity.packageName,
                Manifest.permission.CAMERA,
            )
        }
        if (compose.onAllNodesWithText("同意并继续").fetchSemanticsNodes().isNotEmpty()) {
            compose.onNodeWithText("同意并继续").performClick()
        }
        compose.waitUntil(timeoutMillis = 10_000) {
            compose.onAllNodesWithText("可以拍了").fetchSemanticsNodes().isNotEmpty()
        }
        Thread.sleep(2_500)

        val appliedRatio = AtomicReference<Float?>()
        compose.activity.runOnUiThread { appliedRatio.set(compose.activity.zoomBy(2f)) }

        if (appliedRatio.get() == null) {
            assertTrue(
                "unsupported emulator zoom should not show a false ratio",
                compose.onAllNodesWithTag("control_message").fetchSemanticsNodes().isEmpty(),
            )
            return
        }

        compose.waitUntil(timeoutMillis = 2_000) {
            compose.onAllNodesWithTag("control_message").fetchSemanticsNodes().isNotEmpty()
        }
        val message = compose.onNodeWithTag("control_message")
        message.assertIsDisplayed()
        val ratioText = message.fetchSemanticsNode().config[SemanticsProperties.Text].single().text
        assertTrue("zoom message was $ratioText", ratioText.removeSuffix("x").toFloat() > 1f)
    }
}
