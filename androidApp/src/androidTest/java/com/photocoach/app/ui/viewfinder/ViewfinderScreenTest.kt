package com.photocoach.app.ui.viewfinder

import android.app.Application
import android.os.SystemClock
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pinch
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.photocoach.app.AppViewModel
import com.photocoach.app.beauty.BeautyPreset
import com.photocoach.app.ViewfinderUi
import com.photocoach.app.CreativePhotoUi
import com.photocoach.app.CreativeResultUi
import com.photocoach.app.analysis.OverlayGeometry
import com.photocoach.app.camera.CameraCapabilities
import com.photocoach.app.camera.CameraModePreference
import com.photocoach.app.camera.QuickFocalPreset
import com.photocoach.app.camera.QuickFocalVerification
import com.photocoach.app.camera.ExposureCapability
import com.photocoach.app.camera.CaptureTimer
import com.photocoach.app.creative.CreativeStyle
import com.photocoach.app.creative.ParameterSuggestion
import com.photocoach.app.creative.ParameterTarget
import com.photocoach.app.creative.ParameterAction
import com.photocoach.app.creative.PhotoQualityScore
import com.photocoach.app.ui.theme.PhotoCoachTheme
import com.photocoach.coach.Audience
import com.photocoach.coach.Channel
import com.photocoach.coach.Cue
import com.photocoach.coach.CueId
import com.photocoach.coach.GuidanceSnapshot
import com.photocoach.coach.GuidanceStage
import com.photocoach.coach.RequiredStep
import com.photocoach.coach.ShotIntent
import com.photocoach.coach.Signals
import java.util.concurrent.atomic.AtomicReference
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ViewfinderScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun requiredStepKeepsSkipAndShutterReachable() {
        render(
            ui(
                GuidanceStage.Action(
                    step = RequiredStep.SHOOTER,
                    cue = shooterCue,
                    shownAtMs = 0,
                ),
                shutterEnabled = true,
            ),
        )

        compose.onNodeWithText("1/2").assertIsDisplayed()
        compose.onNodeWithText("走近一步").assertIsDisplayed()
        compose.onNodeWithTag("skip").assertIsDisplayed()
        compose.onNodeWithTag("shutter").assertIsDisplayed().assertIsEnabled()
    }

    @Test
    fun stableAnalyzerSignalsReachTheVisibleGuidanceCard() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = AppViewModel(application)
        val overlay = OverlayGeometry(emptyList(), emptyList(), showSilhouette = false)
        val signals = Signals(faceCount = 1, faceRatio = 0.03f)
        viewModel.onCameraReady(CameraCapabilities())

        repeat(3) { index ->
            viewModel.onFrame(signals, overlay)
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            if (index < 2) SystemClock.sleep(310)
        }

        assertEquals(CueId.MOVE_CLOSER, viewModel.ui.value.guidance.currentCue?.id)
        val state = viewModel.ui.value
        render(state)
        compose.onNodeWithText("1/2").assertIsDisplayed()
        compose.onNodeWithText(requireNotNull(state.guidance.currentCue).text).assertIsDisplayed()
    }

    @Test
    fun missingFaceAndPoseReachVisibleRecoveryInsteadOfReady() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = AppViewModel(application)
        val overlay = OverlayGeometry(emptyList(), emptyList(), showSilhouette = false)
        val signals = Signals(faceCount = 0, poseAvailable = false)
        viewModel.onCameraReady(CameraCapabilities())

        repeat(3) { index ->
            viewModel.onFrame(signals, overlay)
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            if (index < 2) SystemClock.sleep(310)
        }

        assertEquals(CueId.FIND_PERSON, viewModel.ui.value.guidance.currentCue?.id)
        render(viewModel.ui.value)
        compose.onNodeWithText("请露出脸，或靠近一点").assertIsDisplayed()
        compose.onNodeWithText("可以拍了").assertDoesNotExist()
    }

    @Test
    fun twoPMinusOneIntentsAreVisibleAndNoP0EntriesLeak() {
        val selected = AtomicReference<ShotIntent>()
        render(ui(GuidanceStage.Ready(optionalAvailable = false)), onIntent = selected::set)

        compose.onNodeWithTag("intent_close_up").assertIsDisplayed()
        compose.onNodeWithTag("intent_scenery").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(ShotIntent.PERSON_WITH_SCENERY, selected.get()) }
        compose.onAllNodesWithText("切换前后摄").assertCountEquals(0)
        compose.onAllNodesWithText("代拍").assertCountEquals(0)
        compose.onAllNodesWithText("再讲细").assertCountEquals(0)
        compose.onAllNodesWithText("评分").assertCountEquals(0)
    }

    @Test
    fun optionalAppearsOnlyWhenReadyAndOnlyVerifiedFocalButtonsAreShown() {
        render(
            ui(GuidanceStage.Ready(optionalAvailable = true)).copy(
                focalPresets = listOf(defaultFocal, unverifiedTelephoto),
                selectedFocalId = defaultFocal.cameraId,
            ),
        )
        compose.onNodeWithText("发现新建议，可再优化").assertIsDisplayed()
        compose.onNodeWithTag("optional").assertIsDisplayed()
        compose.onNodeWithTag("focal_0").assertIsDisplayed()
        compose.onAllNodesWithTag("focal_1").assertCountEquals(0)
    }

    @Test
    fun saveFailureKeepsRetryAndDiscardVisible() {
        render(
            ui(
                GuidanceStage.SaveFailed("保存失败，请重试", retryAvailable = true),
                shutterEnabled = false,
            ),
        )
        compose.onNodeWithText("保存失败，请重试").assertIsDisplayed()
        compose.onNodeWithTag("save_retry").assertIsDisplayed()
        compose.onNodeWithTag("save_discard").assertIsDisplayed()
    }

    @Test
    fun recentPhotoClickIsForwardedAndPortraitPanelStaysCompact() {
        val opened = AtomicReference<String>()
        val uri = "android.resource://android/drawable/ic_menu_camera"
        render(
            ui(GuidanceStage.Ready(optionalAvailable = false)).copy(recentPhoto = uri),
            onOpenRecentPhoto = opened::set,
        )

        compose.onNodeWithTag("recent_photo").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(uri, opened.get()) }
        val rootBounds = compose.onRoot().getBoundsInRoot()
        val cameraBounds = compose.onNodeWithTag("camera_surface").getBoundsInRoot()
        val panelBounds = compose.onNodeWithTag("operation_panel").getBoundsInRoot()
        val rootHeight = rootBounds.bottom - rootBounds.top
        val panelHeight = panelBounds.bottom - panelBounds.top
        assertTrue(
            "panel height $panelHeight exceeded one fifth of root height $rootHeight",
            panelHeight <= rootHeight * 0.20f,
        )
        assertTrue("panel height was $panelHeight", panelHeight <= 144.dp)
        assertEquals(rootBounds.top, cameraBounds.top)
        assertEquals(rootBounds.bottom, cameraBounds.bottom)
        assertTrue("camera did not continue behind compact dock", cameraBounds.bottom >= panelBounds.bottom)
    }

    @Test
    fun portraitEvSliderStaysAboveOperationPanel() {
        render(
            ui(GuidanceStage.Ready(optionalAvailable = false)).copy(
                showEv = true,
                exposureCapability = ExposureCapability(-1.7f, 2.3f, 0.1f),
            ),
        )

        val evBounds = compose.onNodeWithTag("ev_slider").assertIsDisplayed().getBoundsInRoot()
        val panelBounds = compose.onNodeWithTag("operation_panel").getBoundsInRoot()
        assertTrue(
            "EV slider $evBounds overlapped operation panel $panelBounds",
            evBounds.bottom <= panelBounds.top,
        )
    }

    @Test
    fun pinchGestureIsForwardedToCameraLayer() {
        val zoom = AtomicReference(1f)
        render(
            ui(GuidanceStage.Ready(optionalAvailable = false)),
            onZoomBy = zoom::set,
        )

        compose.onNodeWithTag("camera_preview").performTouchInput {
            pinch(
                start0 = Offset(center.x - 24f, center.y),
                end0 = Offset(center.x - 96f, center.y),
                start1 = Offset(center.x + 24f, center.y),
                end1 = Offset(center.x + 96f, center.y),
            )
        }
        compose.runOnIdle { assert(zoom.get() > 1f) }
    }

    @Test
    fun promptSettingsExposeIndependentVoiceAndSubjectCaptionControls() {
        val voice = AtomicReference<Boolean>()
        val captions = AtomicReference<Boolean>()
        render(
            ui(GuidanceStage.Ready(optionalAvailable = false)),
            onVoiceEnabledChange = voice::set,
            onSubjectCaptionsEnabledChange = captions::set,
        )

        compose.onNodeWithTag("prompt_settings").assertIsDisplayed().performClick()
        compose.onNodeWithTag("voice_toggle").assertIsDisplayed().performClick()
        compose.onNodeWithTag("subject_captions_toggle").assertIsDisplayed().performClick()
        compose.runOnIdle {
            assertEquals(false, voice.get())
            assertEquals(false, captions.get())
        }
    }

    @Test
    fun subjectCaptionCanHideWithoutHidingPromptState() {
        render(
            ui(
                GuidanceStage.Action(
                    step = RequiredStep.SUBJECT,
                    cue = subjectCue,
                    shownAtMs = 0,
                ),
            ).copy(subjectCaptionsEnabled = false),
        )
        compose.onAllNodesWithText(subjectCue.text).assertCountEquals(0)
        compose.onNodeWithText("中文口令播放中").assertIsDisplayed()
    }

    @Test
    fun retainedSubjectCaptionStaysVisibleInReadyState() {
        render(
            ui(
                GuidanceStage.Ready(
                    optionalAvailable = false,
                    retainedSubjectCue = subjectCue,
                    qualityConfirmed = true,
                ),
            ),
        )
        compose.onNodeWithText(subjectCue.text).assertIsDisplayed()
        compose.onNodeWithText("可以拍了").assertIsDisplayed()
    }

    @Test
    fun conservativeLensWarningOwnsTheSingleActionSlotWithoutLockingShutter() {
        render(
            ui(
                GuidanceStage.Action(
                    step = RequiredStep.SHOOTER,
                    cue = shooterCue,
                    shownAtMs = 0,
                ),
            ).copy(lensWarning = "镜头可能被挡住或弄脏，请检查"),
        )

        compose.onNodeWithText("镜头可能被挡住或弄脏，请检查").assertIsDisplayed()
        compose.onAllNodesWithTag("skip").assertCountEquals(0)
        compose.onNodeWithTag("shutter").assertIsDisplayed().assertIsEnabled()
    }

    @Test
    fun creativeMenuExposesTwelveStylesExplicitBurstAndParameterAdvice() {
        val style = AtomicReference<CreativeStyle>()
        val burst = AtomicReference<Boolean>()
        render(
            ui(GuidanceStage.Ready(optionalAvailable = false)).copy(
                parameterSuggestions = listOf(
                    ParameterSuggestion(
                        target = ParameterTarget.STABILITY_TIMER,
                        title = "稳定拍摄",
                        reason = "光线偏弱",
                        actionText = "架稳手机并使用 3 秒倒计时",
                        action = ParameterAction.SetTimer(CaptureTimer.THREE_SECONDS),
                        priority = 1,
                    ),
                ),
            ),
            onCreativeStyleChange = style::set,
            onThreeShotBurstChange = burst::set,
        )

        compose.onNodeWithTag("creative_capture_menu").assertIsDisplayed().performClick()
        CreativeStyle.entries.forEach { creativeStyle ->
            compose.onNodeWithTag("creative_style_${creativeStyle.name.lowercase()}").performScrollTo().assertIsDisplayed()
        }
        compose.onNodeWithTag("creative_style_sunset_gold").performScrollTo().performClick()
        compose.onNodeWithTag("three_shot_burst").performScrollTo().performClick()
        compose.onNodeWithTag("parameter_suggestion_0").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("光线偏弱；架稳手机并使用 3 秒倒计时").assertIsDisplayed()
        compose.runOnIdle {
            assertEquals(CreativeStyle.SUNSET_GOLD, style.get())
            assertEquals(true, burst.get())
        }
    }

    @Test
    fun burstResultKeepsThreeChoicesAndForwardsSelectionAndSaveCopy() {
        val selected = AtomicReference<String>()
        val saveCount = AtomicReference(0)
        val uri = "android.resource://android/drawable/ic_menu_camera"
        val photos = (1..3).map { index ->
            CreativePhotoUi(
                id = "photo-$index",
                originalUri = uri,
                displayUri = uri,
                score = PhotoQualityScore(index.toDouble(), index.toDouble(), index.toDouble()),
                sequence = index,
                effectWasDownsampled = false,
            )
        }
        render(
            ui(GuidanceStage.Saved(uri, 0L)).copy(
                creativeResult = CreativeResultUi(
                    photos = photos,
                    recommendedId = "photo-3",
                    selectedId = "photo-3",
                    recommendationReason = "清晰度更好",
                    isBurst = true,
                ),
                creativeResultVisible = true,
            ),
            onSelectCreativePhoto = selected::set,
            onSaveCreativeCopy = { saveCount.set(saveCount.get() + 1) },
        )

        compose.onNodeWithText("三张都已保留").assertIsDisplayed()
        compose.onNodeWithTag("burst_photo_1").performClick()
        compose.onNodeWithTag("creative_save_copy").performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals("photo-1", selected.get())
            assertEquals(1, saveCount.get())
        }
    }

    @Test
    fun unreadableResultPhotoShowsAVisibleSafePlaceholder() {
        val missing = "file:///definitely-missing-${System.nanoTime()}.jpg"
        val photo = CreativePhotoUi(
            id = "missing",
            originalUri = missing,
            displayUri = missing,
            score = PhotoQualityScore(0.0, 0.0, 0.0),
            sequence = 1,
            effectWasDownsampled = false,
        )
        render(
            ui(GuidanceStage.Saved(missing, 0L)).copy(
                creativeResult = CreativeResultUi(
                    photos = listOf(photo),
                    recommendedId = photo.id,
                    selectedId = photo.id,
                    recommendationReason = "原片",
                    isBurst = false,
                ),
                creativeResultVisible = true,
            ),
        )

        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithTag("creative_preview_error").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("creative_preview_error").assertIsDisplayed()
    }

    @Test
    fun savedPhotoSkipsConfirmationAndOffersOptionalEditorFromCreativeMenu() {
        val openCount = AtomicReference(0)
        val uri = "android.resource://android/drawable/ic_menu_camera"
        val photo = CreativePhotoUi(
            id = "saved-photo",
            originalUri = uri,
            displayUri = uri,
            score = PhotoQualityScore(1.0, 1.0, 1.0),
            sequence = 1,
            effectWasDownsampled = false,
        )
        render(
            ui(GuidanceStage.Saved(uri, 0L)).copy(
                creativeResult = CreativeResultUi(
                    photos = listOf(photo),
                    recommendedId = photo.id,
                    selectedId = photo.id,
                    recommendationReason = "原片",
                    isBurst = false,
                ),
                creativeResultVisible = false,
            ),
            onOpenCreativeResult = { openCount.set(openCount.get() + 1) },
        )

        compose.onNodeWithTag("creative_result").assertDoesNotExist()
        compose.onNodeWithTag("creative_capture_menu").performClick()
        compose.onNodeWithTag("open_creative_result").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, openCount.get()) }
    }

    @Test
    fun beautyMenuOffersExplicitPresetsWithoutBlockingShutter() {
        val selected = AtomicReference(BeautyPreset.OFF)
        render(ui(GuidanceStage.Ready(optionalAvailable = false)).copy(modePreference = CameraModePreference.PHOTO),
            onBeautyPresetChange = selected::set)
        compose.onNodeWithText("美颜·风格").assertIsDisplayed()
        compose.onNodeWithTag("shutter").assertIsEnabled()
        compose.onNodeWithTag("creative_capture_menu").performClick()
        compose.onNodeWithTag("beauty_natural").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(BeautyPreset.NATURAL, selected.get()) }
        compose.onNodeWithTag("shutter").assertIsEnabled()
    }

    @Test
    fun beautyFallbackIsVisibleAndKeepsShutterAvailable() {
        render(ui(GuidanceStage.Ready(optionalAvailable = false)).copy(beautyPreviewWarning = "美颜不可用，普通预览继续"))
        compose.onNodeWithTag("preview_effect_error").assertIsDisplayed()
        compose.onNodeWithText("美颜不可用，普通预览继续").assertIsDisplayed()
        compose.onNodeWithTag("shutter").assertIsDisplayed().assertIsEnabled()
    }

    @Test
    fun beautyMenuExplainsLiveConflictAndRequiresExplicitModeChanges() {
        val selected = AtomicReference(BeautyPreset.OFF)
        val live = AtomicReference(true)
        val mode = AtomicReference(CameraModePreference.AUTO)
        render(ui(GuidanceStage.Ready(optionalAvailable = false)).copy(livePhotoEnabled = true),
            onBeautyPresetChange = selected::set, onLivePhotoChange = live::set, onModePreferenceChange = mode::set)
        compose.onNodeWithTag("creative_capture_menu").performClick()
        compose.onNodeWithTag("beauty_unavailable_reason").assertIsDisplayed()
        compose.onNodeWithTag("beauty_natural").performScrollTo().assertIsNotEnabled()
        compose.runOnIdle {
            assertEquals(BeautyPreset.OFF, selected.get())
            assertEquals(true, live.get())
            assertEquals(CameraModePreference.AUTO, mode.get())
        }
        compose.onNodeWithTag("beauty_disable_live").performScrollTo().performClick()
        compose.onNodeWithTag("beauty_select_photo").performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals(false, live.get())
            assertEquals(CameraModePreference.PHOTO, mode.get())
            assertEquals(BeautyPreset.OFF, selected.get())
        }
    }

    @Test
    fun neutralReadyShowsTheCurrentActionWithoutRequiringAnotherStep() {
        render(ui(GuidanceStage.Ready(optionalAvailable = false, qualityConfirmed = false,
            readinessIssue = com.photocoach.coach.ReadinessIssue.SUBJECT_TOO_SMALL)))
        compose.onNodeWithText("人物偏小，请调整拍摄距离").assertIsDisplayed()
        compose.onNodeWithTag("shutter").assertIsEnabled()
        compose.onNodeWithTag("optional").assertDoesNotExist()
    }

    private fun render(
        state: ViewfinderUi,
        onIntent: (ShotIntent) -> Unit = {},
        onZoomBy: (Float) -> Unit = {},
        onOpenRecentPhoto: (String) -> Unit = {},
        onVoiceEnabledChange: (Boolean) -> Unit = {},
        onSubjectCaptionsEnabledChange: (Boolean) -> Unit = {},
        onCreativeStyleChange: (CreativeStyle) -> Unit = {},
        onThreeShotBurstChange: (Boolean) -> Unit = {},
        onOpenCreativeResult: () -> Unit = {},
        onSelectCreativePhoto: (String) -> Unit = {},
        onSaveCreativeCopy: () -> Unit = {},
        onBeautyPresetChange: (BeautyPreset) -> Unit = {},
        onLivePhotoChange: (Boolean) -> Unit = {},
        onModePreferenceChange: (CameraModePreference) -> Unit = {},
    ) {
        compose.setContent {
            PhotoCoachTheme {
                ViewfinderScreen(
                    ui = state,
                    tiltDegrees = 0f,
                    onPreviewReady = {},
                    onTapFocus = { _, _, _ -> },
                    onEv = {},
                    onSetFocal = {},
                    onZoomBy = onZoomBy,
                    onVoiceEnabledChange = onVoiceEnabledChange,
                    onSubjectCaptionsEnabledChange = onSubjectCaptionsEnabledChange,
                    onGridEnabledChange = {},
                    onLevelEnabledChange = {},
                    onTimerChange = {},
                    onAspectRatioChange = {},
                    onCapturePriorityChange = {},
                    onModePreferenceChange = onModePreferenceChange,
                    onResetSettings = {},
                    onUnlockFocus = {},
                    onToggleFlash = {},
                    onSelectIntent = onIntent,
                    onSkip = {},
                    onOptional = {},
                    onCapture = {},
                    onOpenRecentPhoto = onOpenRecentPhoto,
                    onRetrySave = {},
                    onDiscardSave = {},
                    onRetryCamera = {},
                    onOpenSettings = {},
                    onExit = {},
                    onHideFocusControls = {},
                    onDismissControlMessage = {},
                    onCreativeStyleChange = onCreativeStyleChange,
                    onThreeShotBurstChange = onThreeShotBurstChange,
                    onOpenCreativeResult = onOpenCreativeResult,
                    onSelectCreativePhoto = onSelectCreativePhoto,
                    onSaveCreativeCopy = onSaveCreativeCopy,
                    onBeautyPresetChange = onBeautyPresetChange,
                    onLivePhotoChange = onLivePhotoChange,
                )
            }
        }
    }

    private fun ui(stage: GuidanceStage, shutterEnabled: Boolean = true): ViewfinderUi = ViewfinderUi(
        guidance = GuidanceSnapshot(
            intent = ShotIntent.CLOSE_UP,
            intentLocked = false,
            stage = stage,
            shutterEnabled = shutterEnabled,
            optionalUsed = false,
            roundId = 1,
        ),
    )

    private companion object {
        val shooterCue = Cue(
            id = CueId.MOVE_CLOSER,
            text = "走近一步",
            audience = Audience.SHOOTER,
            channel = Channel.COMPOSITION,
            priority = 90,
        )
        val subjectCue = Cue(
            id = CueId.CHIN_DOWN,
            text = "下巴微收，头略往前",
            audience = Audience.SUBJECT,
            channel = Channel.POSE,
            priority = 80,
        )
        val defaultFocal = QuickFocalPreset(
            cameraId = "rear-default",
            label = "1×",
            relativeZoom = 1f,
            focalLengthMm = 6.7f,
            isDefault = true,
        )
        val unverifiedTelephoto = QuickFocalPreset(
            cameraId = "runtime-candidate",
            label = "3.2×",
            relativeZoom = 3.2f,
            focalLengthMm = 21.4f,
            isDefault = false,
            verification = QuickFocalVerification.CALIBRATION_CANDIDATE,
        )
    }
}
