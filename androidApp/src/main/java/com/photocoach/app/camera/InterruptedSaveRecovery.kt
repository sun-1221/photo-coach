package com.photocoach.app.camera

import android.content.ContentResolver
import android.net.Uri
import com.photocoach.app.beauty.BeautyPreset
import com.photocoach.app.creative.CaptureAssetKind
import com.photocoach.app.creative.CaptureId
import com.photocoach.app.creative.CaptureIdentity
import com.photocoach.app.creative.CreativeImageProcessor
import com.photocoach.app.creative.CreativeStyle
import com.photocoach.app.creative.EditAdjustment
import com.photocoach.app.creative.EditRecipe
import com.photocoach.app.creative.EditRecipeStore
import java.io.File

/** Replays staged saves after process interruption without republishing completed assets. */
internal class InterruptedSaveRecovery(
    private val resolver: ContentResolver,
    private val motionDirectory: File,
    private val journalStore: SaveJournalStore,
    private val recipeStore: EditRecipeStore,
    private val creativeProcessor: CreativeImageProcessor,
    private val wallClockMillis: () -> Long = System::currentTimeMillis,
) {
    fun recover() {
        MotionTemporaryPolicy.expired(motionDirectory.listFiles()?.toList().orEmpty(), wallClockMillis())
            .forEach(File::delete)
        journalStore.readAll().forEach { originalRecord ->
            var record = originalRecord
            var recoveryStage: SaveStage? = null
            runCatching {
                val source = File(record.sourcePath)
                val packaged = record.packagedPath?.let(::File)?.takeIf(File::isFile)
                val primary = packaged ?: source.takeIf(File::isFile)
                if (SaveStage.COMPLETE.name in record.completedStages) {
                    cleanupRecoveredRecord(record)
                    journalStore.delete(record)
                    return@runCatching
                }
                val motionPhoto = record.motionPhotoRequested && !record.motionPhotoFallback && packaged != null
                if (
                    record.originalUri != null &&
                    !record.verifiedAssetStages.containsAssetStage(
                        PublishedAssetKind.ORIGINAL,
                        AssetPublishStage.VERIFY_PUBLISHED,
                    )
                ) {
                    recoveryStage = SaveStage.ORIGINAL_PUBLISH
                    val publishedUri = Uri.parse(record.originalUri)
                    runCatching {
                        PublishedAssetVerifier.verifyPublished(
                            resolver,
                            publishedUri,
                            record.displayName,
                            CaptureSaver.RELATIVE_DIR,
                            motionPhoto,
                        )
                    }.onSuccess {
                        record = record.copy(
                            completedStages = record.completedStages + SaveStage.ORIGINAL_PUBLISH.name,
                            verifiedAssetStages = record.verifiedAssetStages + assetStageKey(
                                PublishedAssetKind.ORIGINAL,
                                AssetPublishStage.VERIFY_PUBLISHED,
                            ),
                        )
                        journalStore.write(record)
                    }.onFailure {
                        CaptureSaver.deleteQuietly(resolver, publishedUri)
                        record = record.copy(
                            originalUri = null,
                            completedStages = record.completedStages - SaveStage.ORIGINAL_PUBLISH.name,
                            verifiedAssetStages = record.verifiedAssetStages.withoutAssetStages(PublishedAssetKind.ORIGINAL),
                        )
                        journalStore.write(record)
                    }
                }
                if (record.originalUri == null) {
                    recoveryStage = SaveStage.ORIGINAL_PUBLISH
                    if (primary == null) {
                        CaptureSaver.deleteQuietly(resolver, record.pendingUri?.let(Uri::parse))
                        CaptureSaver.deleteQuietly(resolver, record.derivativePendingUri?.let(Uri::parse))
                        cleanupRecoveredRecord(record)
                        journalStore.delete(record)
                        return@runCatching
                    }
                    val stalePending = record.pendingUri?.let(Uri::parse)
                    val resumed = stalePending?.let { pendingUri ->
                        runCatching {
                            CaptureSaver.resumePending(
                                resolver,
                                pendingUri,
                                primary,
                                record.displayName,
                                motionPhoto = motionPhoto,
                                pendingAlreadyVerified = record.verifiedAssetStages.containsAssetStage(
                                    PublishedAssetKind.ORIGINAL,
                                    AssetPublishStage.VERIFY_PENDING,
                                ),
                                onAssetStage = { stage ->
                                    record = record.copy(
                                        verifiedAssetStages = record.verifiedAssetStages + assetStageKey(
                                            PublishedAssetKind.ORIGINAL,
                                            stage,
                                        ),
                                    )
                                    journalStore.write(record)
                                },
                            )
                        }.onFailure {
                            CaptureSaver.deleteQuietly(resolver, pendingUri)
                            record = record.copy(pendingUri = null)
                            journalStore.write(record)
                        }.getOrNull()
                    }
                    val uri = resumed ?: CaptureSaver.publish(
                        resolver,
                        primary,
                        record.displayName,
                        record.takenAtMillis,
                        motionPhoto = motionPhoto,
                        onAssetStage = { stage ->
                            record = record.copy(
                                verifiedAssetStages = record.verifiedAssetStages + assetStageKey(
                                    PublishedAssetKind.ORIGINAL,
                                    stage,
                                ),
                            )
                            journalStore.write(record)
                        },
                        onPendingCreated = { pendingUri ->
                            record = record.copy(pendingUri = pendingUri.toString())
                            journalStore.write(record)
                        },
                    )
                    record = record.copy(
                        pendingUri = null,
                        originalUri = uri.toString(),
                        completedStages = record.completedStages + SaveStage.ORIGINAL_PUBLISH.name,
                        verifiedAssetStages = record.verifiedAssetStages + assetStageKey(
                            PublishedAssetKind.ORIGINAL,
                            AssetPublishStage.VERIFY_PUBLISHED,
                        ),
                        failedStage = null,
                        error = null,
                    )
                    journalStore.write(record)
                }
                if (SaveStage.RECIPE_WRITE.name !in record.completedStages) {
                    recoveryStage = SaveStage.RECIPE_WRITE
                    val style = runCatching { CreativeStyle.valueOf(record.style) }.getOrDefault(CreativeStyle.ORIGINAL)
                    val edit = EditAdjustment(
                        exposureStops = record.exposureStops,
                        contrast = record.contrast,
                        saturation = record.saturation,
                        temperature = record.temperature,
                        tint = record.tint,
                        fade = record.fade,
                        styleStrength = record.styleStrength,
                    )
                    recipeStore.write(
                        EditRecipe.create(
                            CaptureId(record.captureId),
                            record.sequence,
                            record.takenAtMillis,
                            style,
                            edit,
                            record.motionPhotoRequested && !record.motionPhotoFallback && packaged != null,
                            BeautyPreset.requireSupported(record.beautyPreset, record.beautyEngineVersion),
                            record.beautyEngineVersion,
                        ),
                    )
                    record = record.copy(completedStages = record.completedStages + SaveStage.RECIPE_WRITE.name)
                    journalStore.write(record)
                }
                if (
                    record.derivativeRequested &&
                    record.derivativeUri != null &&
                    !record.verifiedAssetStages.containsAssetStage(
                        PublishedAssetKind.DERIVATIVE,
                        AssetPublishStage.VERIFY_PUBLISHED,
                    )
                ) {
                    recoveryStage = SaveStage.DERIVATIVE_PUBLISH
                    val publishedUri = Uri.parse(record.derivativeUri)
                    val displayName = CaptureIdentity.displayName(
                        CaptureId(record.captureId),
                        CaptureAssetKind.EFFECT,
                        record.sequence,
                        record.takenAtMillis,
                    )
                    runCatching {
                        PublishedAssetVerifier.verifyPublished(
                            resolver,
                            publishedUri,
                            displayName,
                            CaptureSaver.RELATIVE_DIR,
                            motionPhoto = false,
                        )
                    }.onSuccess {
                        record = record.copy(
                            completedStages = record.completedStages + SaveStage.DERIVATIVE_PUBLISH.name,
                            verifiedAssetStages = record.verifiedAssetStages + assetStageKey(
                                PublishedAssetKind.DERIVATIVE,
                                AssetPublishStage.VERIFY_PUBLISHED,
                            ),
                        )
                        journalStore.write(record)
                    }.onFailure {
                        CaptureSaver.deleteQuietly(resolver, publishedUri)
                        record = record.copy(
                            derivativeUri = null,
                            completedStages = record.completedStages - SaveStage.DERIVATIVE_PUBLISH.name,
                            verifiedAssetStages = record.verifiedAssetStages.withoutAssetStages(PublishedAssetKind.DERIVATIVE),
                        )
                        journalStore.write(record)
                    }
                }
                if (record.derivativeRequested && record.derivativeUri == null && !source.isFile) {
                    recoveryStage = SaveStage.DERIVATIVE_GENERATE
                    record = record.copy(
                        failedStage = SaveStage.DERIVATIVE_GENERATE.name,
                        error = "派生图源文件已丢失；原片和配方已保留，请从原片重新另存效果图",
                    )
                    journalStore.write(record)
                    return@runCatching
                }
                if (record.derivativeRequested && record.derivativeUri == null) {
                    recoveryStage = SaveStage.DERIVATIVE_GENERATE
                    val style = runCatching { CreativeStyle.valueOf(record.style) }.getOrDefault(CreativeStyle.ORIGINAL)
                    val quality = runCatching { DerivativeQuality.valueOf(record.derivativeQuality) }.getOrDefault(DerivativeQuality.FULL)
                    val edit = EditAdjustment(
                        record.exposureStops,
                        record.contrast,
                        record.saturation,
                        record.temperature,
                        record.tint,
                        record.fade,
                        record.styleStrength,
                    )
                    val derivative = record.derivativePath?.let(::File)?.takeIf(File::isFile)
                        ?: creativeProcessor.process(source, style, edit, quality,
                            beautyPreset = BeautyPreset.requireSupported(record.beautyPreset, record.beautyEngineVersion)).file
                    record = record.copy(
                        derivativePath = derivative.absolutePath,
                        completedStages = record.completedStages + SaveStage.DERIVATIVE_GENERATE.name,
                    )
                    journalStore.write(record)
                    recoveryStage = SaveStage.DERIVATIVE_PUBLISH
                    val derivativeUri = record.derivativePendingUri?.let(Uri::parse)?.let { pendingUri ->
                        CaptureSaver.resumePending(
                            resolver,
                            pendingUri,
                            derivative,
                            CaptureIdentity.displayName(
                                CaptureId(record.captureId),
                                CaptureAssetKind.EFFECT,
                                record.sequence,
                                record.takenAtMillis,
                            ),
                            pendingAlreadyVerified = record.verifiedAssetStages.containsAssetStage(
                                PublishedAssetKind.DERIVATIVE,
                                AssetPublishStage.VERIFY_PENDING,
                            ),
                            onAssetStage = { stage ->
                                record = record.copy(
                                    verifiedAssetStages = record.verifiedAssetStages + assetStageKey(
                                        PublishedAssetKind.DERIVATIVE,
                                        stage,
                                    ),
                                )
                                journalStore.write(record)
                            },
                        )
                    } ?: CaptureSaver.publish(
                        resolver,
                        derivative,
                        CaptureIdentity.displayName(
                            CaptureId(record.captureId),
                            CaptureAssetKind.EFFECT,
                            record.sequence,
                            record.takenAtMillis,
                        ),
                        record.takenAtMillis,
                        onAssetStage = { stage ->
                            record = record.copy(
                                verifiedAssetStages = record.verifiedAssetStages + assetStageKey(
                                    PublishedAssetKind.DERIVATIVE,
                                    stage,
                                ),
                            )
                            journalStore.write(record)
                        },
                        onPendingCreated = { pendingUri ->
                            record = record.copy(derivativePendingUri = pendingUri.toString())
                            journalStore.write(record)
                        },
                    )
                    derivative.delete()
                    record = record.copy(
                        derivativePendingUri = null,
                        derivativeUri = derivativeUri.toString(),
                        derivativePath = null,
                        completedStages = record.completedStages + SaveStage.DERIVATIVE_PUBLISH.name,
                        verifiedAssetStages = record.verifiedAssetStages + assetStageKey(
                            PublishedAssetKind.DERIVATIVE,
                            AssetPublishStage.VERIFY_PUBLISHED,
                        ),
                    )
                    journalStore.write(record)
                }
                recoveryStage = SaveStage.COMPLETE
                record = record.copy(completedStages = record.completedStages + SaveStage.COMPLETE.name)
                journalStore.write(record)
                cleanupRecoveredRecord(record)
                journalStore.delete(record)
            }.onFailure { error ->
                runCatching {
                    if (recoveryStage == SaveStage.ORIGINAL_PUBLISH && record.originalUri == null) {
                        CaptureSaver.deleteQuietly(resolver, record.pendingUri?.let(Uri::parse))
                        record = record.copy(pendingUri = null)
                    }
                    if (recoveryStage == SaveStage.DERIVATIVE_PUBLISH && record.derivativeUri == null) {
                        CaptureSaver.deleteQuietly(resolver, record.derivativePendingUri?.let(Uri::parse))
                        record = record.copy(derivativePendingUri = null)
                    }
                    journalStore.write(
                        record.copy(
                            failedStage = recoveryStage?.name,
                            error = error.message ?: "恢复保存失败",
                        ),
                    )
                }
            }
        }
    }

    private fun cleanupRecoveredRecord(record: SaveJournal) {
        listOfNotNull(record.sourcePath, record.motionPath, record.packagedPath, record.derivativePath)
            .map(::File)
            .forEach(File::delete)
    }
}
