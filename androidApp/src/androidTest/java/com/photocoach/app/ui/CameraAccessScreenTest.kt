package com.photocoach.app.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.photocoach.app.ui.consent.CameraConsentScreen
import com.photocoach.app.ui.theme.PhotoCoachTheme
import com.photocoach.app.ui.viewfinder.PermissionDeniedScreen
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CameraAccessScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun consentKeepsAuthorizationAsThePrimaryAction() {
        val agreed = AtomicInteger()
        compose.setContent {
            PhotoCoachTheme {
                CameraConsentScreen(
                    deniedOnce = false,
                    onAgree = agreed::incrementAndGet,
                    onDeny = {},
                )
            }
        }

        compose.onNodeWithText("同意并继续").assertIsDisplayed().performClick()
        compose.onNodeWithText("暂不使用").assertIsDisplayed()
        compose.runOnIdle { assertEquals(1, agreed.get()) }
    }

    @Test
    fun retryableDenialUsesReauthorizationAsPrimary() {
        val retried = AtomicInteger()
        val settings = AtomicInteger()
        compose.setContent {
            PhotoCoachTheme {
                PermissionDeniedScreen(
                    canRequestAgain = true,
                    onRetry = retried::incrementAndGet,
                    onSettings = settings::incrementAndGet,
                )
            }
        }

        compose.onNodeWithText("重新授权").assertIsDisplayed().performClick()
        compose.onNodeWithText("打开设置").assertIsDisplayed().performClick()
        compose.runOnIdle {
            assertEquals(1, retried.get())
            assertEquals(1, settings.get())
        }
    }

    @Test
    fun permanentDenialUsesSettingsAsTheOnlyRecoveryAction() {
        val retried = AtomicInteger()
        val settings = AtomicInteger()
        compose.setContent {
            PhotoCoachTheme {
                PermissionDeniedScreen(
                    canRequestAgain = false,
                    onRetry = retried::incrementAndGet,
                    onSettings = settings::incrementAndGet,
                )
            }
        }

        compose.onNodeWithText("打开设置").assertIsDisplayed().performClick()
        compose.onAllNodesWithText("重新授权").assertCountEquals(0)
        compose.runOnIdle {
            assertEquals(0, retried.get())
            assertEquals(1, settings.get())
        }
    }
}
