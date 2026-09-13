# 非真机实际用例报告索引
由当前 JVM XML 与明确列出的 AndroidJUnitRunner 完成状态提取。只表示这些实际方法的结果，不表示关联 FR/UX 整项通过；合成输入、注入故障和真实 CameraX 的差异见主矩阵。历史失败仍在原始轮次报告。

## ActivityLifecycleAcceptanceTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| AVD instrumented | controlledCueUsesActualActivitySpeechAndPanelCancellationOrUnavailableFallback | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | actualThreeAndTenSecondTimersConsumeVolumeKeysAndCancelWithoutCapture | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | actualParameterPanelKeepsAnalysisAndDoesNotStartSpeechOnClose | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | actualPostPhotoActionsGrantUrisFavoriteAndConfirmOrCancelTrash | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | realActivityBackgroundAndRecreationResumeAnalysisAndCapture | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | actualLiveGraphKeepsAnalysisAndPublishesOnePrimaryOrExplicitJpegFallback | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | actualLiveGraphKeepsAnalysisAndPublishesOnePrimaryOrExplicitJpegFallback | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/target-57.txt) |
| AVD instrumented | discardMarkerWriteFailureKeepsActualActivityRetryStateUntilDurable | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/target-61.txt) |
| AVD instrumented | revokedPermissionShowsRecoveryAndActualSettingsGrantReturnsToWorkingCamera | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/permission-runtime-67.txt) |
| AVD instrumented | actualCameraPhotoSurvivesTwoUiEditingAndExportRounds | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/第78j轮-编辑两轮-通过.txt) |
| AVD instrumented | actualLiveWithMeasuredPrebufferPublishesVerifiedMotionPhoto | Fail（正确JPEG回退） | [报告](../../androidApp/build/reports/non-device-2026-09-12/第78j轮-实际Live-SPEED-失败.txt) |

## AnalysisFrameMetadataStoreTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | nextFrameCannotOverwriteTheMetadataOfADelayedResult() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.analysis.AnalysisFrameMetadataStoreTest.xml) |
| JVM | onlyBoundedMetadataSurvivesAndCloseClearsIt() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.analysis.AnalysisFrameMetadataStoreTest.xml) |

## AssetRetirementTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | successfulDeleteStillRequiresConfirmedAbsence() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.AssetRetirementTest.xml) |
| JVM | absentRowOrConfirmedDeletionAllowsReplacement() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.AssetRetirementTest.xml) |
| JVM | unknownRowStateBlocksReplacement() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.AssetRetirementTest.xml) |
| JVM | motionPublishFailureCanFallBackButPublishedOriginalCannot() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.AssetRetirementTest.xml) |
| JVM | deletionRefusalBlocksReplacement() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.AssetRetirementTest.xml) |

## AssetStageKeyTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | original and derivative verification stages do not collide() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.AssetStageKeyTest.xml) |
| JVM | legacy unscoped stages are accepted only for original assets() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.AssetStageKeyTest.xml) |
| JVM | clearing derivative stages preserves original evidence() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.AssetStageKeyTest.xml) |

## BeautyCameraBindingTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| AVD instrumented | threeUseCasesCaptureAndReleaseAcrossEffectRebinds | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |

## BeautyModelTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | defaultsAndPresetCapsAreConservative() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.beauty.BeautyModelTest.xml) |
| JVM | smoothingResetsOnLargeMotionAndSensorChanges() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.beauty.BeautyModelTest.xml) |
| JVM | freshnessIsZeroForFutureAndExpiredFrames() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.beauty.BeautyModelTest.xml) |
| JVM | geometryProtectsFeaturesAndBackgroundWithoutSkinColorRules() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.beauty.BeautyModelTest.xml) |
| JVM | onlyStandardPhotoAllowsBeautyAndOffNeverBlocksOtherModes() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.beauty.BeautyModelTest.xml) |
| JVM | latestOnlyStoreRejectsOldFramesAndSuppressesMultipleFacesImmediately() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.beauty.BeautyModelTest.xml) |
| JVM | originalIsDefaultEvenWithBeautyAndAutoSaveAddsOneDerivative() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.beauty.BeautyModelTest.xml) |
| JVM | maskFollowsRotatedAndTranslatedLandmarks() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.beauty.BeautyModelTest.xml) |
| JVM | abruptRotationAndZoomNeverInterpolateThroughASingularMask() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.beauty.BeautyModelTest.xml) |
| JVM | missingSmallSidewaysAndInvalidLandmarksDisableMask() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.beauty.BeautyModelTest.xml) |

## BeautyPersistenceTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | recipeOnlyPersistsTheVersionedPresetNotFacialData() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.beauty.BeautyPersistenceTest.xml) |
| JVM | oldRecipeAndJournalRemainOff() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.beauty.BeautyPersistenceTest.xml) |
| JVM | retryKeepsOriginalIdentityAndCapturedBeautyAndReplacesOneJournal() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.beauty.BeautyPersistenceTest.xml) |

## BeautyPreviewStateTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | selectedPresetDoesNotImplyThatBeautyIsActive() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.beauty.BeautyPreviewStateTest.xml) |
| JVM | staleFutureAndInvalidCoordinateFramesNeverReportActive() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.beauty.BeautyPreviewStateTest.xml) |
| JVM | offAndThermalPauseOverrideAValidFace() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.beauty.BeautyPreviewStateTest.xml) |

## BeautyRendererTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| AVD instrumented | noFaceStillReturnsVisibleWarningWithoutChangingBitmap | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | cpuBlendPreservesProtectedPixelsAndOffIsIdentity | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | oesCameraInputPathSupportsPassthroughAndBeauty | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | gpuPreservesConstantColorsAcrossResizeAndCloseIsIdempotent | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | gpuChangesOnlyUnprotectedMaskAndKeepsChangesBounded | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | offMissingMaskAndExpiredSnapshotArePixelIdentical | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |

## BeautyStateTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| AVD instrumented | incompatibleModesRejectWithoutSilentlyChangingSelection | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | settingsPersistAndResetWithoutClearingOtherConsent | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | captureFreezesRecipeEvenWhenPreviewFailsOrSettingsReset | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |

## BeautyTransformTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | croppedMirroredOutputsMapBackToAnalysisForAllRotationsAndRatios() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.beauty.BeautyTransformTest.xml) |
| JVM | invalidAndSingularMatricesAreRejected() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.beauty.BeautyTransformTest.xml) |
| JVM | rotationCornersHaveExpectedBounds() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.beauty.BeautyTransformTest.xml) |

## BoundedImageDecoderInstrumentedTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| AVD instrumented | temporaryFileCreationFailureLeavesOriginalBytesUntouched | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | decoderDownsamplesWithinTheExplicitPixelLimit | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | unreadableUriFailsWithoutAllocatingAnUnboundedPlaceholder | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |

## BuildVariantAcceptanceTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| AVD instrumented | installedBuildMatchesRequestedResearchVariantAndIsolatesCreativeSettings | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | installedBuildMatchesRequestedResearchVariantAndIsolatesCreativeSettings | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/variant-final-DYNAMIC-BACKLIGHT-test.txt) |
| AVD instrumented | installedBuildMatchesRequestedResearchVariantAndIsolatesCreativeSettings | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/variant-final-DYNAMIC-SCENERY-test.txt) |
| AVD instrumented | installedBuildMatchesRequestedResearchVariantAndIsolatesCreativeSettings | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/variant-final-DYNAMIC-WINDOW-test.txt) |
| AVD instrumented | installedBuildMatchesRequestedResearchVariantAndIsolatesCreativeSettings | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/variant-final-STATIC-BACKLIGHT-test.txt) |
| AVD instrumented | installedBuildMatchesRequestedResearchVariantAndIsolatesCreativeSettings | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/variant-final-STATIC-SCENERY-test.txt) |
| AVD instrumented | installedBuildMatchesRequestedResearchVariantAndIsolatesCreativeSettings | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/variant-final-STATIC-WINDOW-test.txt) |
| AVD instrumented | installedBuildMatchesRequestedResearchVariantAndIsolatesCreativeSettings | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-NONE-variant-test.txt) |

## BurstSessionTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | requires explicit enable rejects reentry and completes at exactly three() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.creative.BurstSessionTest.xml) |
| JVM | failure retains completed photos and stable tie picks earlier sequence() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.creative.BurstSessionTest.xml) |

## CameraAccessScreenTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| AVD instrumented | permanentDenialUsesSettingsAsTheOnlyRecoveryAction | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | retryableDenialUsesReauthorizationAsPrimary | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | consentKeepsAuthorizationAsThePrimaryAction | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |

## CameraBinderPreparationRetryTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| AVD instrumented | preparationFailureUsesSaveErrorAndRealBinderRetryPublishesSameCaptureOnce | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |

## CameraJpegCaptureTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| AVD instrumented | rawXmpEligibilitySurvivesCroppingAndJournalClearFailureKeepsRawRetryable | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | emptyPlaneFailureStillClosesProxyAndKeepsPreparationIdentity | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | sameProxyBytesRemainPublishableWhenSensorTimestampIsInvalid | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |

## CameraLockTimeoutInstrumentedTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| AVD instrumented | realMainHandlerDeliversTimeoutFailureAndOldResultCannotConfirm | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |

## CameraUserSettingsTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | defaultsAreSafeAndRecoverable() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.CameraUserSettingsTest.xml) |
| JVM | validValuesRestoreAndUnknownValuesFallBackIndependently() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.CameraUserSettingsTest.xml) |

## CaptureEventLedgerTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | delayedImmutableProgressKeepsOldIdentityAndPublishesEachCaptureOnce() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.research.CaptureEventLedgerTest.xml) |

## CaptureIdentityTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | captureId names link original effect recipe and burst without collisions() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.creative.CaptureIdentityTest.xml) |
| JVM | motion photo filename follows official MP suffix pattern() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.creative.CaptureIdentityTest.xml) |

## CaptureRecomputationTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | sameMillisecondExitBeforePublicationKeepsExitOrder() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.research.CaptureRecomputationTest.xml) |
| JVM | timeoutThenExitRemainsFailedAndPureEarlyExitRemainsSeparate() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.research.CaptureRecomputationTest.xml) |
| JVM | publicationThenErrorAtSameMillisecondDoesNotRewriteFirstSave() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.research.CaptureRecomputationTest.xml) |
| JVM | sameCaptureRetryAndLatePublicationRetainExitAndAcceptedLedger() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.research.CaptureRecomputationTest.xml) |
| JVM | aFailedCaptureAndNewBPublicationIsNotARecovery() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.research.CaptureRecomputationTest.xml) |
| JVM | rejectedRequestIsFailedRoundWithoutInventingAcceptedCapture() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.research.CaptureRecomputationTest.xml) |

## CaptureRequestGateTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | save failure and unavailable camera block a new batch() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.CaptureRequestGateTest.xml) |
| JVM | only one request can be pending and cancellation invalidates its delayed token() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.CaptureRequestGateTest.xml) |
| JVM | schedule and execution both reject capture or result states() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.CaptureRequestGateTest.xml) |

## CaptureSaveCallbacksTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | rejectedRetryAndLateFailureAfterDestroyUseOwnedIdentityAndSafeRefresh() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.CaptureSaveCallbacksTest.xml) |
| JVM | actualRetryWiringKeepsOriginalIdentityAfterAnotherCaptureStarts() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.CaptureSaveCallbacksTest.xml) |

## CaptureViewPortTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | landscapeRotationUsesLandscapeDimensionsForEveryCaptureRatio() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.CaptureViewPortTest.xml) |
| JVM | portraitRotationUsesPortraitDimensionsForEveryCaptureRatio() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.CaptureViewPortTest.xml) |

## ClosingAnalyzerExecutorTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | unexpectedWorkerFailureIsNotHidden() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.analysis.ClosingAnalyzerExecutorTest.xml) |
| JVM | shutdownRacingSubmissionCompletesCleanupExactlyOnce() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.analysis.ClosingAnalyzerExecutorTest.xml) |
| JVM | openExecutorUsesWorkerAndLateCompletionStillRunsAfterClose() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.analysis.ClosingAnalyzerExecutorTest.xml) |

## CoachAnalyzerCoordinateTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | portraitRotationSwapsAnalysisDimensions() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.analysis.CoachAnalyzerCoordinateTest.xml) |
| JVM | fillCenterMatchesPreviewCropForFaceTapHitTesting() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.analysis.CoachAnalyzerCoordinateTest.xml) |

## CoachRulesTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | userIntentHardFiltersSceneryAndNeverUsesTelephotoForIt() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.CoachRulesTest.xml) |
| JVM | multipleFacesKeepOnlySafeFramingAndExposureCues() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.CoachRulesTest.xml) |
| JVM | noVerifiedTelephotoNeverOffersTelephoto() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.CoachRulesTest.xml) |
| JVM | poseOnlyFrameFallsBackToFindPersonAfterObservablePoseIssueClears() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.CoachRulesTest.xml) |
| JVM | unreliableExpressionAndBodyJudgementLanguageIsRejectedAtTheCatalogBoundary() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.CoachRulesTest.xml) |
| JVM | verifiedTelephotoCanBeOfferedForCloseUpOnly() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.CoachRulesTest.xml) |
| JVM | reliablePoseSurvivesFaceMissButMultipleFacesStillBlockSinglePersonPose() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.CoachRulesTest.xml) |
| JVM | conservativeLensWarningCanAppearWhenTheCoveredLensHidesTheSubject() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.CoachRulesTest.xml) |
| JVM | faceDetailsProduceOnlyObservedSubjectCuesWithoutRequiringPose() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.CoachRulesTest.xml) |
| JVM | atMostOneCandidatePerChannelAndNoForbiddenProductLanguage() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.CoachRulesTest.xml) |
| JVM | faceAndPoseMissProduceVisibleRecoveryInsteadOfReady() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.CoachRulesTest.xml) |
| JVM | darkFaceDoesNotRepeatMeteringAfterSuccessfulFaceTap() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.CoachRulesTest.xml) |
| JVM | closeUpMovesAVisiblyLowFaceTowardTheUpperThird() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.CoachRulesTest.xml) |
| JVM | poseCandidateTracksCurrentPoseInsteadOfUsingOneFrozenSceneCue() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.CoachRulesTest.xml) |

## CreativeCaptureSessionTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | editing resets to the captured strength and retains per photo history() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.CreativeCaptureSessionTest.xml) |
| JVM | edit snapshot owns undo redo and reset flags() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.CreativeCaptureSessionTest.xml) |
| JVM | save retry retains pause until explicit continue and never recaptures source() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.CreativeCaptureSessionTest.xml) |
| JVM | each shot has a unique capture id and a stable batch id() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.CreativeCaptureSessionTest.xml) |
| JVM | burst keeps all photos and recommends the highest scored photo after explicit retry() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.CreativeCaptureSessionTest.xml) |
| JVM | capture start freezes identity settings style and time for the batch() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.CreativeCaptureSessionTest.xml) |

## CreativeFallbackTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | successful effect is selected without warning() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.creative.CreativeFallbackTest.xml) |
| JVM | effect failure retains original and a visible warning() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.creative.CreativeFallbackTest.xml) |

## CreativeStyleImageTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| AVD instrumented | allTwelveStylesProcessRealSyntheticJpegAndPreserveSourceBytes | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |

## CreativeStyleTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | zero style strength returns to original while each explicit edit remains effective() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.creative.CreativeStyleTest.xml) |
| JVM | preview export and edit matrix is deterministic for every style() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.creative.CreativeStyleTest.xml) |
| JVM | twelve approved styles are defined with original first() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.creative.CreativeStyleTest.xml) |

## DeviceMotionStabilityTrackerTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | movement becomes unstable immediately and needs consecutive quiet samples to recover() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.analysis.DeviceMotionStabilityTrackerTest.xml) |

## DisplayRollTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | uprightInEveryDisplayRotationIsLevel() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.analysis.DisplayRollTest.xml) |
| JVM | flatAndInvalidGravityAreUnknown() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.analysis.DisplayRollTest.xml) |

## EditHistoryTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | seven parameter history supports undo redo and reset() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.creative.EditHistoryTest.xml) |
| JVM | all values are clamped and a new edit clears redo() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.creative.EditHistoryTest.xml) |
| JVM | continuous updates of one slider undo as one adjustment() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.creative.EditHistoryTest.xml) |

## ExportTransactionRunnerTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | onlineAndRecoveryShareExactLeaseAndCannotOverwriteSameTemporaryJournal() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.ExportTransactionRunnerTest.xml) |
| JVM | newExportAfterFailureUsesNewIdentityAndNewRecipeWhileRetryRetainsOldRecipe() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.ExportTransactionRunnerTest.xml) |
| JVM | recoveryCompletionAndStaleRetryDoNotResurrectOrRepublish() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.ExportTransactionRunnerTest.xml) |
| JVM | commitThenJournalOrCleanupFailureRemainsIdempotentAfterRestart() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.ExportTransactionRunnerTest.xml) |

## ExposureControllerTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | rebindDiscardsOldCameraCompletion() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.ExposureControllerTest.xml) |
| JVM | failureIsNeverReportedAsApplied() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.ExposureControllerTest.xml) |
| JVM | clampsToHardwareRangeAndRejectsUnavailableCapability() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.ExposureControllerTest.xml) |
| JVM | waitsForHardwareAndIgnoresReplacedRequest() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.ExposureControllerTest.xml) |

## ExposureGuidancePolicyTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | scenesNeverAutomaticallyUnderexposeFaces() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.ExposureGuidancePolicyTest.xml) |
| JVM | brightWallWithoutClippingDoesNotRequestDarkerExposure() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.ExposureGuidancePolicyTest.xml) |

## ExtensionPolicyTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | photoAndFaceRetouchStayStandard() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.ExtensionPolicyTest.xml) |
| JVM | requiresBothAvailabilityAndAnalysis() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.ExtensionPolicyTest.xml) |

## FaceDetailClassifierTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | frontalOpenEyesDoNotInventAnExpressionCorrection() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.analysis.FaceDetailClassifierTest.xml) |
| JVM | oneMissingEyeProbabilityDoesNotGuessBlinkOrExpression() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.analysis.FaceDetailClassifierTest.xml) |
| JVM | oneLikelyClosedEyeProducesBlinkCue() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.analysis.FaceDetailClassifierTest.xml) |
| JVM | turnedFaceWinsAndDoesNotGuessEyeOrExpressionDetails() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.analysis.FaceDetailClassifierTest.xml) |
| JVM | unavailableClassificationNeverInventsFacialDetails() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.analysis.FaceDetailClassifierTest.xml) |
| JVM | unknownEyeClassificationIsDistinctFromKnownOpenEyes() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.analysis.FaceDetailClassifierTest.xml) |

## FaceFocusSignalStateTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | onlySuccessfulFaceMeteringCompletesTheAction() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.analysis.FaceFocusSignalStateTest.xml) |
| JVM | unknownInitialStateDoesNotCreateAFakeFocusFaceCue() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.analysis.FaceFocusSignalStateTest.xml) |
| JVM | explicitTapUpdatesStateAndNewPhotoReturnsToUnknownSatisfiedState() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.analysis.FaceFocusSignalStateTest.xml) |

## FaceLuminanceClassifierTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | gridCoordinatesFollowCameraRotation() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.analysis.FaceLuminanceClassifierTest.xml) |
| JVM | partiallyOutOfFrameFaceIsClippedBeforeComparingItsCore() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.analysis.FaceLuminanceClassifierTest.xml) |
| JVM | invalidOrUndersampledFaceRegionStaysUnknown() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.analysis.FaceLuminanceClassifierTest.xml) |
| JVM | facePositionAloneCannotInventBacklight() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.analysis.FaceLuminanceClassifierTest.xml) |
| JVM | actualFaceSamplesMustBeDarkerThanSamplesOutsideTheFace() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.analysis.FaceLuminanceClassifierTest.xml) |

## FreshOfflineAcceptanceTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| AVD instrumented | consentPrecedesPermissionAndFreshOfflineCameraPublishesOriginal | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/fresh-offline-verified.txt) |
| AVD instrumented | decliningAppConsentDoesNotRequestCameraPermission | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/consent-decline-final.txt) |
| AVD instrumented | deniedSystemPermissionCanBeExplicitlyRetried | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/permission-retry-final.txt) |

## GuidanceSessionTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | ordinaryShooterCueUsesStableRealtimeReplacementInTheSameStep() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.GuidanceSessionTest.xml) |
| JVM | completedSpeechRetainsValidCaptionAndLatestPoseBecomesOptional() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.GuidanceSessionTest.xml) |
| JVM | unresolvedQualityCanEndAdviceBudgetWithoutClaimingConfirmedReady() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.GuidanceSessionTest.xml) |
| JVM | subjectCueUsesStableRealtimeReplacementInsteadOfFreezing() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.GuidanceSessionTest.xml) |
| JVM | optionalAvailabilityDoesNotFlickerOnTransientCandidateLoss() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.GuidanceSessionTest.xml) |
| JVM | intentLockSurvivesPhotoWhileActionBudgetResets() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.GuidanceSessionTest.xml) |
| JVM | readyQualityRefreshesAfterStableImprovementAndDeterioration() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.GuidanceSessionTest.xml) |
| JVM | subjectOnlyCandidateStartsFirstVisibleAndSpeakableStep() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.GuidanceSessionTest.xml) |
| JVM | exhaustedAdviceStillExplainsUnresolvedQualityWithoutAddingAStep() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.GuidanceSessionTest.xml) |
| JVM | threeStableFramesStartOneOfTwoAndSkippingNeverAddsRequiredSteps() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.GuidanceSessionTest.xml) |
| JVM | mutedSubjectFallsBackToReadyWithoutWaitingForPoseMatch() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.GuidanceSessionTest.xml) |
| JVM | userCanSkipPersonRecoveryWithoutLockingShutterOrClaimingQuality() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.GuidanceSessionTest.xml) |
| JVM | oneGoodFrameDoesNotEraseTheReadinessReason() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.GuidanceSessionTest.xml) |
| JVM | currentShooterCueIsSpeakableAndRateLimited() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.GuidanceSessionTest.xml) |
| JVM | stablePoseThatAppearsAfterObservationTimeoutBecomesOptionalWithoutAddingRequiredStep() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.GuidanceSessionTest.xml) |
| JVM | disablingBothSubjectChannelsSkipsEmptyWaitImmediately() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.GuidanceSessionTest.xml) |
| JVM | faceTapCompletesDarkFaceMeteringWithoutClaimingBrightnessImproved() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.GuidanceSessionTest.xml) |
| JVM | missingPersonRecoveryDoesNotTimeoutToFalseReady() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.GuidanceSessionTest.xml) |
| JVM | missingPersonRecoveryCanReappearAfterItWasResolvedAndReady() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.GuidanceSessionTest.xml) |
| JVM | observationAndShooterTimeoutsNeverBecomeInfiniteThinking() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.GuidanceSessionTest.xml) |
| JVM | speechAndOppositeDirectionAreRateLimited() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.GuidanceSessionTest.xml) |
| JVM | resolvedSubjectCueClearsAfterStableSignalChange() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.GuidanceSessionTest.xml) |
| JVM | latePoseAfterShooterStepStaysOptional() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.GuidanceSessionTest.xml) |
| JVM | knownDarkFaceOrSubjectMotionDoesNotClaimConfirmedReady() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.GuidanceSessionTest.xml) |
| JVM | observationTimeoutWithNoReliablePersonShowsRecoveryInsteadOfFalseReady() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.GuidanceSessionTest.xml) |
| JVM | differentLightingTechniquesReplaceTheOldInstructionAfterStability() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.GuidanceSessionTest.xml) |
| JVM | shutterWorksDuringObservationAndBothGuidanceSteps() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.GuidanceSessionTest.xml) |
| JVM | upperThirdCueCompletesWhenTheFaceMovesOutOfTheLowRegion() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.GuidanceSessionTest.xml) |
| JVM | machineReadableImprovementNeedsFiveHundredMillisecondsAndMinimumDisplay() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.GuidanceSessionTest.xml) |
| JVM | criticalDetectionRecoveryCanReplaceReadyButOrdinaryPoseCannot() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.GuidanceSessionTest.xml) |

## GuidanceTransactionRegressionTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | closingChannelsUsesReliablePoseAndMultiPersonSafetyRules() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.GuidanceTransactionRegressionTest.xml) |
| JVM | panelDwellCannotCompletePrePauseImprovementAndPlaybackCallbacksAreIgnored() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.GuidanceTransactionRegressionTest.xml) |
| JVM | optionalCandidateChangesCannotAdvanceWhilePausedAndExpiryResumesRecovery() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.GuidanceTransactionRegressionTest.xml) |
| JVM | readyExpiresWithoutAnotherAnalysisCallback() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.GuidanceTransactionRegressionTest.xml) |
| JVM | optionalPoseRequiresExplicitActionAndConsumesSingleBudget() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.GuidanceTransactionRegressionTest.xml) |
| JVM | panelPausesSpeechAndBudgetAndResumesLatestStableCueWithoutReplay() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.GuidanceTransactionRegressionTest.xml) |
| JVM | unknownEvidenceAndPoseOnlyNeverConfirmQuality() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.GuidanceTransactionRegressionTest.xml) |
| JVM | intentChangesCannotEraseCaptureOrRetryTransaction() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.GuidanceTransactionRegressionTest.xml) |
| JVM | closingPanelRestoresLatestStableReplacementWithoutRestartingBudget() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.GuidanceTransactionRegressionTest.xml) |

## GuidanceTtsInstrumentedTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| AVD instrumented | chineseShooterCueCompletesOnTheEmulatorTtsEngine | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | installedTtsEngineIsVisibleToTheAppPackage | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |

## GuidedCoefficientsTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | edgeIsPreservedAndReconstructionIsBounded() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.beauty.GuidedCoefficientsTest.xml) |
| JVM | constantColorIsPreservedAcrossTheWholeImage() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.beauty.GuidedCoefficientsTest.xml) |

## ImageDecodePolicyTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | power of two sample never exceeds requested pixel ceiling() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.creative.ImageDecodePolicyTest.xml) |
| JVM | export preview and thumbnail limits share a descending bounded policy() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.creative.ImageDecodePolicyTest.xml) |

## InterruptedSaveRecoveryTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| AVD instrumented | completedExplicitExportOnlyVerifiesAndCleansOnRecoveryAndStaleRetry | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | missingSourceWithoutPublishedIdentityRemainsExplicitUnrecoverableRecord | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | fullQuotaDefersJpegPreparationWithoutTouchingRawOrPublishing | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | completePendingJpegAndMotionAreCommittedBeforeSourceCleanup | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | committedMotionWithLostPackageKeepsOriginalUriAndContainer | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | legacySchemasRecoverOriginalKeyAndNeverRepublishAfterRecipeFailure | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | corruptMotionPendingRowIsRetiredBeforeJpegFallback | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | discardCleanupFailureRetainsTombstoneAndRecoveryNeverPublishesIt | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | interruptedJpegPreparationReplaysRawOnceBeforePublishingSameCapture | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | reloadedResearchJournalPublishesReceiptWithOriginalIdentityAndUnknownLatency | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | fullQuotaRejectsNewAndRecoveryGenerationButKeepsExistingSourcePublishable | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | corruptMotionFallsBackOnceAndSurvivesAnotherInterruptedStage | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | newRecoverySkipsActiveOldSaveAndDoesNotReplayItsCompletedJournal | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | missingSourceWithPublishedPendingIdentityIsVerifiedWithoutDeletingOrRepublishing | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | commitThenJournalFailureRetainsPublishedRowAndRecoveryReusesIt | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |

## JpegPreparationTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| AVD instrumented | crashTemporaryHasDeterministicTransactionOwnershipAndIsReplacedOnReplay | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | twoCroppedAspectRatiosAndFourRotationsKeepRawBytesAndExpectedPixels | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | exifFailureLeavesRawAndPreviousOutputUntouchedAndSamePreparationCanRetry | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | memoryAdmissionRejectsBeforeDecodeWithoutDownsamplingOrLosingRaw | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | uncroppedJpegDoesNotConsumeBitmapDecodeBudget | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |

## LensObstructionDetectorTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | ordinaryDarkOrLowDetailFrameDoesNotTrigger() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.analysis.LensObstructionDetectorTest.xml) |
| JVM | requiresSeveralVeryDarkFlatFramesAndClearsConservatively() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.analysis.LensObstructionDetectorTest.xml) |

## LiveRecorderOwnershipTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | twoRetiredControllersBoundProcessQuotaAndCannotReleaseEachOthersOwner() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.LiveRecorderOwnershipTest.xml) |
| JVM | ownVideoMayFinishWhileLateRecorderStillBlocksRestartUntilItsFinalize() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.LiveRecorderOwnershipTest.xml) |

## LiveRecordingSessionTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | retiredFinalizeCannotClearNewRecording() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.LiveRecordingSessionTest.xml) |
| JVM | lateStartAfterReleaseCannotReviveRecording() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.LiveRecordingSessionTest.xml) |
| JVM | warmupStartsAtEncoderStartNotPrepareRequest() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.LiveRecordingSessionTest.xml) |

## LiveSessionFallbackTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | unsupportedHdFallsBackToSdWithoutTryingToBindHd() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.LiveSessionFallbackTest.xml) |
| JVM | staticCaptureUsesOrdinarySdrJpegSemantics() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.LiveSessionFallbackTest.xml) |
| JVM | qualityOrderIsHdThenSd() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.LiveSessionFallbackTest.xml) |
| JVM | hdBindingFailureFallsBackToSdAndStopsAfterSuccess() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.LiveSessionFallbackTest.xml) |
| JVM | noSupportedCandidateReturnsNullWithoutBinding() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.LiveSessionFallbackTest.xml) |

## LumaGridBackgroundDetailTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | flat background stays quiet() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.analysis.LumaGridBackgroundDetailTest.xml) |
| JVM | busy detail around subject has high edge density() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.analysis.LumaGridBackgroundDetailTest.xml) |

## MotionDualOutputTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| AVD instrumented | realSurfaceTextureFansOutFrameIdentityAndMirrorToTwoPlayableEncoders | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | realSurfaceTextureFansOutFrameIdentityAndMirrorToTwoPlayableEncoders | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/target-57.txt) |

## MotionEglOwnershipTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| AVD instrumented | closingEitherRendererDoesNotTerminateTheOtherOwnersDisplay | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |

## MotionEncoderInstrumentedTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| AVD instrumented | eglSurfaceEncoderRetainsMeasuredVfrPtsAndDecodesAfterFirstInputDrop | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |

## MotionFrameTimelineTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | unknownOrAmbiguousAssociationAndOldGenerationFailClosed() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.MotionFrameTimelineTest.xml) |
| JVM | byteFrameDurationAndMuxMismatchQuotasFailClosed() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.MotionFrameTimelineTest.xml) |
| JVM | postbufferWaitsForRetainedSensorCoverageRatherThanButtonElapsedTime() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.MotionFrameTimelineTest.xml) |
| JVM | duplicateCoalescedInputIsIgnoredButBackwardsAndUnmatchedOutputsInvalidate() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.MotionFrameTimelineTest.xml) |
| JVM | sensorCorrelationCanArriveAfterEncodingButEveryRetainedSampleMustMatch() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.MotionFrameTimelineTest.xml) |
| JVM | droppedFirstInputAndNonKeyOutputsDoNotBecomeTheRetainedOrigin() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.MotionFrameTimelineTest.xml) |
| JVM | muxRoundingRequiresMeasuredTrackTimebaseAndTimeScalingIsRejected() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.MotionFrameTimelineTest.xml) |

## MotionGlWorkerTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| AVD instrumented | concurrentAdmissionAndCloseNeverRunControlAfterCleanup | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | closeWaitsForInflightFrameBeforeCleanupAndReleasesAllLeases | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | deadlineReportsWhileBlockedOwnerKeepsItsPermitUntilCleanupFinishes | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |

## MotionMuxTimingModelTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | unmodelledDecodeClampingAndMalformedInputRemainRejected() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.MotionMuxTimingModelTest.xml) |
| JVM | integerCoalescingReproducesIndependentlyCalculatedVfrPoints() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.MotionMuxTimingModelTest.xml) |
| JVM | completeActualAvdContainerFixtureMatchesWrittenInputsAndRejectsTampering() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.MotionMuxTimingModelTest.xml) |

## MotionPhotoFileInstrumentedTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| AVD instrumented | fileAssemblerPreservesJpegPayloadWritesV1XmpAndCleansFailureOutput | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |

## MotionPhotoTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | existing Motion Photo xmp is replaced instead of duplicated() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.MotionPhotoTest.xml) |
| JVM | motion xmp mixed with unrelated metadata is preserved by rejecting rewrite() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.MotionPhotoTest.xml) |
| JVM | invalid jpeg or mp4 is rejected for reliable ordinary photo fallback() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.MotionPhotoTest.xml) |
| JVM | clip window keeps about one point five seconds around shutter and clamps boundaries() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.MotionPhotoTest.xml) |
| JVM | file validation failure removes empty output but never overwrites a nonempty target() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.MotionPhotoTest.xml) |
| JVM | unrelated xmp and gain map xmp fall back instead of creating ambiguous jpeg() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.MotionPhotoTest.xml) |
| JVM | v1 xmp matches Xiaomi native directory shape() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.MotionPhotoTest.xml) |
| JVM | truncated jpeg segment is rejected() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.MotionPhotoTest.xml) |
| JVM | temporary policy removes oversized stale and missing entries but keeps bounded fresh video() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.MotionPhotoTest.xml) |
| JVM | non xmp jpeg metadata is preserved byte for byte() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.MotionPhotoTest.xml) |
| JVM | container preserves jpeg bytes and appends mp4 as final bytes() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.MotionPhotoTest.xml) |
| JVM | file assembly rejects source overwrite and leaves jpeg unchanged() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.MotionPhotoTest.xml) |
| JVM | file assembly reports real terminal video offset() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.MotionPhotoTest.xml) |

## MotionPixelPoolTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | independentSinkLeasesPreventReuseAndHaveIndependentPositions() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.MotionPixelPoolTest.xml) |
| JVM | dimensionsStayWithinHdPixelAndFixedSlotQuota() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.MotionPixelPoolTest.xml) |
| JVM | closeRejectsNewWorkButDoesNotInvalidateSlowConsumerBytes() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.MotionPixelPoolTest.xml) |

## MotionTimestampBoundaryTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | missingNegativeOverflowAndOutOfWindowAnchorsNeverBecomeValid() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.MotionTimestampBoundaryTest.xml) |

## NewRequirementsCameraTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | storageRefusesQuotaWithoutDeletingOldSourceAndPreservesLegacyBytes() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.NewRequirementsCameraTest.xml) |
| JVM | actualCaptureTimestampRequiresMediaAnchorAndMustBeInsideClip() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.NewRequirementsCameraTest.xml) |
| JVM | unsupportedAndTimeoutNeverBecomeDoubleLock() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.NewRequirementsCameraTest.xml) |
| JVM | afSuccessDoesNotConfirmAeAndOldResultsCannotConfirmNewRequest() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.NewRequirementsCameraTest.xml) |
| JVM | emergencyStopsNewCaptureButKeepsCapturedSave() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.NewRequirementsCameraTest.xml) |

## NewRequirementsGuidanceTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | frameGateRejectsRepeatedOldAndExpiredFramesAndResetsAcrossGaps() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.NewRequirementsGuidanceTest.xml) |
| JVM | sceneryNeverSuggestsTelephotoAndPositionIsExplicitInspiration() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.NewRequirementsGuidanceTest.xml) |
| JVM | missingPosePointsAreUnknownNotImprovement() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.NewRequirementsGuidanceTest.xml) |
| JVM | actualGuidanceDoesNotAccumulateRepeatedOrRetiredSessionFrames() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.NewRequirementsGuidanceTest.xml) |
| JVM | staticResearchCardsIgnoreQualityAndHaveSameSkipAndCapturePath() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.NewRequirementsGuidanceTest.xml) |
| JVM | actualGuidanceRetractsMissingShoulderEvidenceBeforeMinimumDisplayTime() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.NewRequirementsGuidanceTest.xml) |

## OfflineModelInstrumentedTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| AVD instrumented | bundledFaceAndPoseInitializeAndProcessSyntheticInputWithoutInternetPermission | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | bundledFaceAndPoseInitializeAndProcessSyntheticInputWithoutInternetPermission | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/fresh-offline-verified.txt) |

## OriginalPublicationFeedbackTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| AVD instrumented | researchFastNextShutterPreparesBeforeCaptureAndSlowConfirmationRestartsCueClock | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | originalPublicationImmediatelyUpdatesThumbnailAndDoesNotBecomeTotalFailure | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | retryCallbackForAAfterBStartsCannotOverwriteBAndRefreshesARecovery | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | originalPipelineReleasesNewCaptureWhileOldEffectIsSuspendedAndLateFailureCannotLockIt | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |

## ParameterCoachTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | priority is stable limited to three and EV uses exact reported step() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.creative.ParameterCoachTest.xml) |
| JVM | validated focal and supported extension produce one tap actions() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.creative.ParameterCoachTest.xml) |
| JVM | unverified telephoto becomes physical distance advice with reason and no fake action() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.creative.ParameterCoachTest.xml) |

## PendingCaptureTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | journal snapshot preserves immutable identity completed stages and retry evidence() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.PendingCaptureTest.xml) |

## PhotoQualityScorerTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | middle exposure outranks clipped exposure and scoring is deterministic() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.creative.PhotoQualityScorerTest.xml) |
| JVM | sharp patterned frame outranks flat frame with same exposure() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.creative.PhotoQualityScorerTest.xml) |

## PhotoTechniqueEngineTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | unknown evidence stays quiet() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.PhotoTechniqueEngineTest.xml) |
| JVM | unverified focal length is never invented() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.PhotoTechniqueEngineTest.xml) |
| JVM | multiple people only receive safe observable technique() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.PhotoTechniqueEngineTest.xml) |

## PlayableMotionPhotoTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| AVD instrumented | encodedVideoClipsAtRealSyncSampleAndProducesDecodableSilentMotionContainer | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |

## PMinusOneCameraPolicyTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | flashOffersOffAndAutoOnly() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.PMinusOneCameraPolicyTest.xml) |
| JVM | mediaStoreNameAndDirectoryAreStable() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.PMinusOneCameraPolicyTest.xml) |
| JVM | manifestRequestsCameraOnly() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.PMinusOneCameraPolicyTest.xml) |

## PMinusOneRegressionTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | window-enters-and-uses-scene-cues | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.PMinusOneRegressionTest.xml) |
| JVM | window-does-not-match-outdoors | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.PMinusOneRegressionTest.xml) |
| JVM | scenery-enters-from-explicit-intent | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.PMinusOneRegressionTest.xml) |
| JVM | scenery-small-face-keeps-landmark | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.PMinusOneRegressionTest.xml) |
| JVM | close-up-low-face-uses-upper-third-guidance | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.PMinusOneRegressionTest.xml) |
| JVM | close-up-lock-rejects-scenery-pack | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.PMinusOneRegressionTest.xml) |
| JVM | backlight-enters-and-focuses-face | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.PMinusOneRegressionTest.xml) |
| JVM | backlight-respects-scenery-one-x | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.PMinusOneRegressionTest.xml) |
| JVM | multiple-faces-do-not-enter-single-person-scene | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.PMinusOneRegressionTest.xml) |

## PortraitToneGuardTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | even safe sample respects uncalibrated style range() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.creative.PortraitToneGuardTest.xml) |
| JVM | risky face change only lowers whole image strength() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.creative.PortraitToneGuardTest.xml) |
| JVM | unknown and multiple faces never claim protection() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.creative.PortraitToneGuardTest.xml) |

## PoseGuidanceTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | low confidence and multiple people suppress single person pose() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.PoseGuidanceTest.xml) |
| JVM | timed inspiration remains active until its timeout() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.PoseGuidanceTest.xml) |
| JVM | inspiration cues never claim visual auto completion() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.PoseGuidanceTest.xml) |
| JVM | catalog covers six types and has bounded neutral cues() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.PoseGuidanceTest.xml) |
| JVM | changing eligible evidence restarts acquisition stability window() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.PoseGuidanceTest.xml) |
| JVM | observable cue requires stable entry and stable improvement() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.PoseGuidanceTest.xml) |

## PoseSignalClassifierTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | raisedShoulderAndHandsBlockingTorsoRequireNormalizedEvidence() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.analysis.PoseSignalClassifierTest.xml) |
| JVM | followingBodyTurnRemovesAngleCueFromTheEngineInput() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.analysis.PoseSignalClassifierTest.xml) |
| JVM | halfBodyCanStillDetectRaisedShoulders() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.analysis.PoseSignalClassifierTest.xml) |
| JVM | fullBodySignalsUseOnlyReliableVisibleLandmarks() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.analysis.PoseSignalClassifierTest.xml) |
| JVM | halfBodyKeepsUpperBodySignalsButDoesNotGuessLowerBodySignals() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.analysis.PoseSignalClassifierTest.xml) |
| JVM | uncalibratedShoulderDepthDoesNotInventBodyTurnWithoutTorso() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.analysis.PoseSignalClassifierTest.xml) |
| JVM | frontFacingTorsoTriggersSquareShouldersButTurningBodyClearsIt() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.analysis.PoseSignalClassifierTest.xml) |

## PreviewEffectFallbackTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | render effect failure clears the effect and reports fallback() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.ui.viewfinder.PreviewEffectFallbackTest.xml) |

## ProcessingResourceScopeTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | successful processing transfers only output ownership() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.creative.ProcessingResourceScopeTest.xml) |
| JVM | render or save failure deletes temporary file and releases both bitmaps() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.creative.ProcessingResourceScopeTest.xml) |
| JVM | temporary file creation failure still releases source and target() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.creative.ProcessingResourceScopeTest.xml) |

## ProcessRecoveryAcceptanceTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| AVD instrumented | prepareCommittedButUnjournaledOriginal | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/process-final-phase1.txt) |
| AVD instrumented | recoverInNewProcessWithoutRepublishing | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/process-final-phase2.txt) |

## PublishedAssetVerifierInstrumentedTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| AVD instrumented | publishAcceptsBoundsOnlyDecodeAndLeavesNoPendingRow | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |

## PublishedAssetVerifierTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | motion directory requires one primary one motion and real length() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.PublishedAssetVerifierTest.xml) |
| JVM | asset facts reject empty unreadable and unsupported orientation() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.PublishedAssetVerifierTest.xml) |

## QualitySignalPipelineTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | measuredCompleteProductionSignalsReachReadyAndOptional() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.analysis.QualitySignalPipelineTest.xml) |
| JVM | staleSensorAndFocusStayUnknownEvenWithFreshFrames() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.analysis.QualitySignalPipelineTest.xml) |
| JVM | eachMissingMeasurementPreventsConfirmation() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.analysis.QualitySignalPipelineTest.xml) |

## QuickFocalPolicyTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | discoversOnlyRearCameraXEntriesWithStandardFocalMetadata() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.QuickFocalPolicyTest.xml) |
| JVM | targetCalibrationPromotesOnlyTheVerifiedCandidateToAQuickControl() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.QuickFocalPolicyTest.xml) |
| JVM | unavailableOrStaleSelectionFallsBackToDefaultWithoutInventingAPreset() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.QuickFocalPolicyTest.xml) |

## RecoveryInboxTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | deferOldRecoveryThenRetryNewCaptureHasIndependentIdentity() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.RecoveryInboxTest.xml) |

## ResearchDataContractTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | loggerEscapesControlsAndRecordsFailedWrites() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.research.ResearchDataContractTest.xml) |
| JVM | syntheticSideRecordsKeepMissingTiesDisagreementsAndLostFollowUpsSeparate() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.research.ResearchDataContractTest.xml) |
| JVM | realJsonlRetainsTimeoutExitCaptureFailureAndSaveRecovery() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.research.ResearchDataContractTest.xml) |
| JVM | corruptionLostEventsMetadataAndCaptureReuseFailClosed() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.research.ResearchDataContractTest.xml) |

## ResearchEventLoggerTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | storesOnlyDeclaredNonImageFields() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.research.ResearchEventLoggerTest.xml) |
| JVM | diskFailureAndSizeLimitAreIsolatedAndMarked() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.research.ResearchEventLoggerTest.xml) |

## ResearchLogPipelineTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| AVD instrumented | productionRejectionBeforeCameraAcceptanceDoesNotInventSaveAttempt | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | productionNoneModeNeverCreatesOrAppendsResearchFile | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | productionVmFailureRetryPublicationExportsReplayableJsonl | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |

## ResearchMetricCalculationTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | syntheticAllMetricFamiliesExposeDenominatorsAndPolicyChoicesWithoutGo() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.research.ResearchMetricCalculationTest.xml) |

## ResearchPreparationTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | disappearingEyeClassificationOrBodyEvidenceImmediatelyWithdrawsRequiredCue() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.ResearchPreparationTest.xml) |
| JVM | slowPreparationStartsStaticAndDynamicTimersOnlyAtAcknowledgement() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.ResearchPreparationTest.xml) |
| JVM | fastSavedShutterCannotCaptureBeforeNextRoundPreparation() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.ResearchPreparationTest.xml) |

## ResearchRecomputationTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | duplicatesFailuresMissingAndLateRowsRemainVisible() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.research.ResearchRecomputationTest.xml) |

## ResearchRecoveryAssociationTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | recoveryKeepsOldOwnerAndDoesNotInventTimeOrCountNewCapture() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.research.ResearchRecoveryAssociationTest.xml) |
| JVM | exportedBundleReadsActualLoggerAndReceiptFilesWithoutInventingPublicationTime() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.research.ResearchRecoveryAssociationTest.xml) |

## SaveJournalStoreTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | legacySchemaFixturesPreserveOriginalKeysAndPublishedIdentityOnRetry() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.SaveJournalStoreTest.xml) |
| JVM | journalCarriesVersionedVerificationAndRetryState() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.SaveJournalStoreTest.xml) |
| JVM | recovery record atomically preserves capture stage pending uri and retry identity() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.SaveJournalStoreTest.xml) |

## SaveTransactionRegistryTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | recreated owner cannot recover a save still running after executor shutdown() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.SaveTransactionRegistryTest.xml) |
| JVM | different captures can own transactions independently() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.SaveTransactionRegistryTest.xml) |

## SaveWorkflowTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | safe EXIF policy excludes location maker note and unknown tags() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.SaveWorkflowTest.xml) |
| JVM | space preflight includes processing derivative and reserve() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.SaveWorkflowTest.xml) |
| JVM | motion packaging failure changes plan to ordinary jpeg without duplicate publish() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.SaveWorkflowTest.xml) |
| JVM | partial failure preserves original and retries only failed recipe stage() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.SaveWorkflowTest.xml) |

## SecondRepairPipelineTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | concurrentReservationsAndRetainedExportsShareQuotaWithoutDeletingSources() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.SecondRepairPipelineTest.xml) |
| JVM | suspendedEffectsDoNotBlockNextOriginalOrMixTransactionResults() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.SecondRepairPipelineTest.xml) |
| JVM | timeoutScheduledCallbackNotifiesFailureThenRejectsLateCallbacks() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.SecondRepairPipelineTest.xml) |

## SpeechCompletionIntegrationTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | lateDoneErrorAndStopCannotAdvanceReplacementGuidance() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.tts.SpeechCompletionIntegrationTest.xml) |

## StableCueCoverageTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | everyCatalogIdHasAReachableCaseOrExplicitDormantBoundary() | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | pose-close-face-entry-exit-invalid-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | pose-close-eyes-entry-exit-invalid-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | pose-half-shoulders-entry-exit-invalid-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | pose-half-hand-entry-exit-invalid-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | pose-half-angle-entry-exit-invalid-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | pose-full-feet-entry-exit-invalid-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | pose-full-joints-entry-exit-invalid-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | pose-full-step-entry-exit-invalid-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | pose-seated-upright-entry-exit-invalid-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | pose-seated-chair-entry-exit-invalid-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | pose-walking-step-entry-exit-invalid-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | pose-solo-interaction-entry-exit-invalid-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | technique-P1_FRAME-trigger-exit-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | technique-P1_JOINTS-trigger-exit-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | technique-P1_FACE_LIGHT-trigger-exit-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | technique-P1_HIGHLIGHTS-trigger-exit-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | technique-P1_DISTANCE-trigger-exit-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | technique-P1_BACKGROUND-trigger-exit-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | technique-P1_NIGHT-trigger-exit-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | technique-P1_MOTION-trigger-exit-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | technique-P1_MOTION_BURST-trigger-exit-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | technique-P1_MULTI_FRAME-trigger-exit-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | technique-P1_MULTI_LEVEL-trigger-exit-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | technique-P1_MULTI_HIGHLIGHTS-trigger-exit-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | camera-position-manual-only | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | catalog-FIND_PERSON-trigger-exit-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | catalog-CLEAN_LENS-trigger-exit-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | catalog-KEEP_SUBJECT_IN_FRAME-trigger-exit-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | catalog-FOCUS_FACE-trigger-exit-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | catalog-MOVE_CLOSER-trigger-exit-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | catalog-MOVE_CLOSER_KEEP_SCENERY-trigger-exit-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | catalog-LEVEL_PHONE-trigger-exit-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | catalog-PLACE_ON_THIRDS-trigger-exit-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | catalog-PLACE_FACE_ON_UPPER_THIRD-trigger-exit-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | catalog-LOWER_EXPOSURE-trigger-exit-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | catalog-TURN_FACE_TO_CAMERA-trigger-exit-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | catalog-OPEN_EYES-trigger-exit-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | catalog-CHIN_DOWN-trigger-exit-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | catalog-ANGLE_BODY-trigger-exit-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | catalog-RELAX_SHOULDERS-trigger-exit-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | catalog-REST_HANDS-trigger-exit-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | catalog-MOVE_TO_WINDOW-trigger-exit-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | catalog-TURN_TO_WINDOW-trigger-exit-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | catalog-STEP_SIDEWAYS-trigger-exit-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |
| JVM | catalog-ANGLE_BODY_BACKLIT-trigger-exit-unknown | Pass | [报告](../../coach/build/test-results/test/TEST-com.photocoach.coach.StableCueCoverageTest.xml) |

## StyleRecommendationEngineTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | recommendation does not select or mutate a style() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.creative.StyleRecommendationEngineTest.xml) |
| JVM | recent keeps five unique entries and favorites are explicit() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.creative.StyleRecommendationEngineTest.xml) |
| JVM | original is always first and recommendation is bounded() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.creative.StyleRecommendationEngineTest.xml) |

## TemporalPoseTrackerTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | single frame and missing evidence never invent motion() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.analysis.TemporalPoseTrackerTest.xml) |
| JVM | alternating ankles plus displacement produces walking evidence() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.analysis.TemporalPoseTrackerTest.xml) |

## ThermalPolicyTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | load sheds progressively without removing core camera guarantees() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.ThermalPolicyTest.xml) |
| JVM | unknown status is conservative rather than reported normal() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.ThermalPolicyTest.xml) |

## ThermalRebindGateTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | thermal rebind is deferred until camera operation settles() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.ThermalRebindGateTest.xml) |
| JVM | thermal rebind proceeds immediately while camera is idle() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.camera.ThermalRebindGateTest.xml) |

## TtsReadinessGateTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| JVM | mutingBeforeInitializationCompletesCancelsQueuedCue() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.tts.TtsReadinessGateTest.xml) |
| JVM | cueOfferedDuringInitializationPlaysWhenEngineBecomesReady() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.tts.TtsReadinessGateTest.xml) |
| JVM | initializationFailureReturnsQueuedCueAndRejectsLaterCues() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.tts.TtsReadinessGateTest.xml) |
| JVM | latestRealtimeCueReplacesStaleCueWhileEngineInitializes() | Pass | [报告](../../androidApp/build/test-results/testDebugUnitTest/TEST-com.photocoach.app.tts.TtsReadinessGateTest.xml) |

## ViewfinderActivityTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| AVD instrumented | manualZoomRespectsTheBoundCameraCapability | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |

## ViewfinderScreenTest

| 类型 | 实际方法 | 结果 | 原始报告 |
|---|---|---|---|
| AVD instrumented | conservativeLensWarningOwnsTheSingleActionSlotWithoutLockingShutter | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | optionalAppearsOnlyWhenReadyAndOnlyVerifiedFocalButtonsAreShown | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | recentPhotoClickIsForwardedAndPortraitPanelStaysCompact | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | largeTextKeepsGuidanceSkipAndShutterInsideScreen | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | retainedSubjectCaptionStaysVisibleInReadyState | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | staticResearchCardIsNotReplacedByLiveLensWarning | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | subjectCaptionCanHideWithoutHidingPromptState | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | portraitEvSliderStaysAboveOperationPanel | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | creativeMenuExposesTwelveStylesExplicitBurstAndParameterAdvice | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | twoPMinusOneIntentsAreVisibleAndNoP0EntriesLeak | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | burstResultKeepsThreeChoicesAndForwardsSelectionAndSaveCopy | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | requiredStepKeepsSkipAndShutterReachable | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | brightnessCanBeOpenedWithoutTappingFaceAndRemainsAvailable | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | promptSettingsExposeIndependentVoiceAndSubjectCaptionControls | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | cameraErrorKeepsBothRecoveryActionsVisible | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | saveFailureKeepsRetryAndDiscardVisible | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | unreadableResultPhotoShowsAVisibleSafePlaceholder | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | beautyMenuOffersExplicitPresetsWithoutBlockingShutter | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | pinchGestureIsForwardedToCameraLayer | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | previewWarningAndExposureStayAboveTheDock | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | beautyFallbackIsVisibleAndKeepsShutterAvailable | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | neutralReadyShowsTheCurrentActionWithoutRequiringAnotherStep | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | missingFaceAndPoseReachVisibleRecoveryInsteadOfReady | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | shutterIsCenteredAndControlsHaveAccessibleLabels | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | landscapeCurrentActionUsesBothMeasuredLinesWithoutEllipsis | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | savedPhotoSkipsConfirmationAndOffersOptionalEditorFromCreativeMenu | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | selectedBeautyShowsActualGateOutsideMenu | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | stableAnalyzerSignalsReachTheVisibleGuidanceCard | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | beautyMenuExplainsLiveConflictAndRequiresExplicitModeChanges | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | compactLandscapeUsesAdaptiveRailAndKeepsGuidanceSkipAndShutterReachable | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/final-instrumented-55.txt) |
| AVD instrumented | conservativeLensWarningOwnsTheSingleActionSlotWithoutLockingShutter | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/target-57.txt) |
| AVD instrumented | optionalAppearsOnlyWhenReadyAndOnlyVerifiedFocalButtonsAreShown | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/target-57.txt) |
| AVD instrumented | recentPhotoClickIsForwardedAndPortraitPanelStaysCompact | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/target-57.txt) |
| AVD instrumented | largeTextKeepsGuidanceSkipAndShutterInsideScreen | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/target-57.txt) |
| AVD instrumented | retainedSubjectCaptionStaysVisibleInReadyState | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/target-57.txt) |
| AVD instrumented | staticResearchCardIsNotReplacedByLiveLensWarning | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/target-57.txt) |
| AVD instrumented | subjectCaptionCanHideWithoutHidingPromptState | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/target-57.txt) |
| AVD instrumented | portraitEvSliderStaysAboveOperationPanel | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/target-57.txt) |
| AVD instrumented | creativeMenuExposesTwelveStylesExplicitBurstAndParameterAdvice | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/target-57.txt) |
| AVD instrumented | twoPMinusOneIntentsAreVisibleAndNoP0EntriesLeak | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/target-57.txt) |
| AVD instrumented | burstResultKeepsThreeChoicesAndForwardsSelectionAndSaveCopy | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/target-57.txt) |
| AVD instrumented | requiredStepKeepsSkipAndShutterReachable | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/target-57.txt) |
| AVD instrumented | brightnessCanBeOpenedWithoutTappingFaceAndRemainsAvailable | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/target-57.txt) |
| AVD instrumented | promptSettingsExposeIndependentVoiceAndSubjectCaptionControls | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/target-57.txt) |
| AVD instrumented | cameraErrorKeepsBothRecoveryActionsVisible | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/target-57.txt) |
| AVD instrumented | saveFailureKeepsRetryAndDiscardVisible | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/target-57.txt) |
| AVD instrumented | unreadableResultPhotoShowsAVisibleSafePlaceholder | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/target-57.txt) |
| AVD instrumented | beautyMenuOffersExplicitPresetsWithoutBlockingShutter | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/target-57.txt) |
| AVD instrumented | pinchGestureIsForwardedToCameraLayer | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/target-57.txt) |
| AVD instrumented | previewWarningAndExposureStayAboveTheDock | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/target-57.txt) |
| AVD instrumented | beautyFallbackIsVisibleAndKeepsShutterAvailable | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/target-57.txt) |
| AVD instrumented | neutralReadyShowsTheCurrentActionWithoutRequiringAnotherStep | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/target-57.txt) |
| AVD instrumented | missingFaceAndPoseReachVisibleRecoveryInsteadOfReady | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/target-57.txt) |
| AVD instrumented | shutterIsCenteredAndControlsHaveAccessibleLabels | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/target-57.txt) |
| AVD instrumented | landscapeCurrentActionUsesBothMeasuredLinesWithoutEllipsis | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/target-57.txt) |
| AVD instrumented | savedPhotoSkipsConfirmationAndOffersOptionalEditorFromCreativeMenu | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/target-57.txt) |
| AVD instrumented | selectedBeautyShowsActualGateOutsideMenu | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/target-57.txt) |
| AVD instrumented | allSevenEditorSlidersPreserveOtherAdjustmentsAndUseCorrectFields | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/target-57.txt) |
| AVD instrumented | stableAnalyzerSignalsReachTheVisibleGuidanceCard | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/target-57.txt) |
| AVD instrumented | beautyMenuExplainsLiveConflictAndRequiresExplicitModeChanges | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/target-57.txt) |
| AVD instrumented | compactLandscapeUsesAdaptiveRailAndKeepsGuidanceSkipAndShutterReachable | Pass | [报告](../../androidApp/build/reports/non-device-2026-09-12/target-57.txt) |
