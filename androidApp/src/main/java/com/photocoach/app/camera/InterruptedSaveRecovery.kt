package com.photocoach.app.camera

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
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
    private val pendingSources: PendingSourceStore? = null,
    private val researchRecoveryStore: com.photocoach.app.research.ResearchRecoveryStore? = null,
    private val wallClockMillis: () -> Long = System::currentTimeMillis,
) {
    fun recover(onlyKey: String? = null): List<SaveJournal> {
        MotionTemporaryPolicy.expired(motionDirectory.listFiles()?.toList().orEmpty(), wallClockMillis())
            .forEach(File::delete)
        journalStore.readAll().forEach { originalRecord ->
            if (onlyKey != null && originalRecord.key != onlyKey) return@forEach
            if (originalRecord.exportSourceUri != null) {
                runCatching { recoverExport(originalRecord) }
                return@forEach
            }
            val lease = SaveTransactionRegistry.tryAcquire(
                journalStore.transactionKey(originalRecord.captureId, originalRecord.sequence),
            ) ?: return@forEach
            lease.use {
                recoverRecord(originalRecord)
            }
        }
        return journalStore.readAll().filter { SaveStage.COMPLETE.name !in it.completedStages || it.error != null }
    }

    private fun exportRunner() = ExportTransactionRunner(journalStore,
        generate = { record ->
            fun generate(): GeneratedExport {
                val processed = creativeProcessor.process(resolver, Uri.parse(requireNotNull(record.exportSourceUri)),
                    CreativeStyle.valueOf(record.style),
                    EditAdjustment(record.exposureStops, record.contrast, record.saturation, record.temperature,
                        record.tint, record.fade, record.styleStrength), DerivativeQuality.valueOf(record.derivativeQuality),
                    beautyPreset = BeautyPreset.requireSupported(record.beautyPreset, record.beautyEngineVersion))
                return GeneratedExport(processed.file, processed.wasDownsampled, processed.beautyWarning)
            }
            pendingSources?.generate { generate() } ?: generate()
        },
        publish = { record, file, pending ->
            CaptureSaver.publish(resolver, file, record.displayName, record.takenAtMillis,
                existingUri = record.derivativePendingUri?.let(Uri::parse),
                onPendingCreated = { pending(it.toString()) }).toString()
        },
        verify = { record, uri -> PublishedAssetVerifier.verifyPublished(resolver, Uri.parse(uri),
            record.displayName, CaptureSaver.RELATIVE_DIR, false); Unit },
    )

    internal fun recoverExport(original: SaveJournal): ExportedCopy = exportRunner().retry(original).let {
        ExportedCopy(Uri.parse(it.uri), it.downsampled, it.warning)
    }

    internal fun createExport(original: SaveJournal): ExportedCopy = exportRunner().create(original).let {
        ExportedCopy(Uri.parse(it.uri), it.downsampled, it.warning)
    }

    private fun recoverRecord(originalRecord: SaveJournal) {
        // The previous owner may have completed/deleted this entry since readAll().
        var record = journalStore.read(originalRecord) ?: return
        var recoveryStage: SaveStage? = null
        runCatching {
            if(record.discarded) {
                discardCaptureSources(resolver,journalStore,record)
                return
            }
            var source = File(record.sourcePath)
            record.jpegPreparation?.let {preparation ->
                if(preparation.motionCompatible!=true && runCatching {MotionPhotoAssembler.verifySourceCompatibility(File(preparation.rawPath))}.isFailure) {
                    record=record.copy(motionPhotoFallback=true)
                    journalStore.write(record)
                }
                val budget=pendingSources ?: PendingSourceStore(source.parentFile!!)
                budget.reserve(creative=false,live=true).use {preparation.prepare(source)}
                record=record.copy(jpegPreparation=null,jpegRawPath=record.jpegRawPath ?: preparation.rawPath)
                journalStore.write(record)
            }
            pendingSources?.let { store ->
                if (source.isFile && source.length() > 0) {
                    val durable = store.preserveLegacy(source)
                    if (durable != source) {
                        record = record.copy(sourcePath = durable.absolutePath)
                        journalStore.write(record) // Keep old source until new path is durably journaled.
                        source.delete()
                        source = durable
                    }
                }
            }
            val packaged = record.packagedPath?.let(::File)?.takeIf(File::isFile)
            var motionPhoto = record.motionPhotoRequested && !record.motionPhotoFallback
            var primary = if (motionPhoto) packaged else source.takeIf { it.isFile && it.length() > 0 }
            if (record.originalUri != null) {
                recoveryStage = SaveStage.ORIGINAL_PUBLISH
                val publishedUri = Uri.parse(record.originalUri)
                runCatching {
                    CaptureSaver.commitVerifiedPending(
                        resolver,
                        publishedUri,
                        record.displayName,
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
                }.getOrThrow()
            }
            if (record.originalUri == null) {
                recoveryStage = SaveStage.ORIGINAL_PUBLISH
                if (primary == null) {
                    val known = record.pendingUri?.let(Uri::parse)
                    val recovered=known?.let {runCatching {CaptureSaver.commitVerifiedPending(resolver,it,record.displayName,motionPhoto)}}
                    if (recovered?.isSuccess == true) {
                        record = record.copy(originalUri = known.toString(), pendingUri = null,
                            completedStages = record.completedStages + SaveStage.ORIGINAL_PUBLISH.name,
                            verifiedAssetStages = record.verifiedAssetStages + assetStageKey(PublishedAssetKind.ORIGINAL, AssetPublishStage.VERIFY_PUBLISHED))
                        journalStore.write(record)
                        // Re-enter from the persisted published URI, without requiring or republishing the source.
                        recoverRecord(record)
                        return@runCatching
                    }
                    if(motionPhoto && source.isFile && source.length()>0) {
                        // Only an uncommitted, safely retired row may be replaced by JPEG.
                        deleteFailedAsset(known)
                        record=record.copy(pendingUri=null,packagedPath=null,motionPhotoFallback=true,
                            displayName=CaptureIdentity.displayName(CaptureId(record.captureId),CaptureAssetKind.ORIGINAL,record.sequence,record.takenAtMillis),
                            verifiedAssetStages=record.verifiedAssetStages.withoutAssetStages(PublishedAssetKind.ORIGINAL),
                            completedStages=record.completedStages-SaveStage.MOTION_PACKAGE.name-SaveStage.ORIGINAL_PUBLISH.name)
                        journalStore.write(record)
                        motionPhoto=false;primary=source
                    } else throw recovered?.exceptionOrNull() ?: IllegalStateException("捕获源文件已丢失，无法恢复；不会重新拍摄。已有相册照片不会删除")
                }
                if (record.pendingUri == null) {
                    record = record.copy(displayName = if (motionPhoto) {
                        CaptureIdentity.motionPhotoDisplayName(CaptureId(record.captureId), record.sequence, record.takenAtMillis)
                    } else {
                        CaptureIdentity.displayName(CaptureId(record.captureId), CaptureAssetKind.ORIGINAL, record.sequence, record.takenAtMillis)
                    })
                    journalStore.write(record)
                }
                fun publishPrimary(): Uri {
                    val stalePending = record.pendingUri?.let(Uri::parse)
                    val resumed = stalePending?.let { pendingUri ->
                        runCatching {
                            CaptureSaver.resumePending(
                                resolver,
                                pendingUri,
                                requireNotNull(primary),
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
                            deleteFailedAsset(pendingUri)
                            record = record.copy(pendingUri = null,
                                verifiedAssetStages = record.verifiedAssetStages.withoutAssetStages(PublishedAssetKind.ORIGINAL))
                            journalStore.write(record)
                            if (motionPhoto) throw it
                        }.getOrNull()
                    }
                    return resumed ?: CaptureSaver.publish(
                        resolver,
                        requireNotNull(primary),
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
                }
                val uri = try {
                    publishPrimary()
                } catch (error: Throwable) {
                    if (!motionPhoto || !source.isFile) throw error
                    // Retire the failed row before publishing a different primary asset.
                    deleteFailedAsset(record.pendingUri?.let(Uri::parse))
                    record = record.copy(
                        pendingUri = null,
                        packagedPath = null,
                        motionPhotoFallback = true,
                        displayName = CaptureIdentity.displayName(CaptureId(record.captureId), CaptureAssetKind.ORIGINAL,
                            record.sequence, record.takenAtMillis),
                        verifiedAssetStages = record.verifiedAssetStages.withoutAssetStages(PublishedAssetKind.ORIGINAL),
                        completedStages = record.completedStages - SaveStage.MOTION_PACKAGE.name - SaveStage.ORIGINAL_PUBLISH.name,
                        failedStage = null,
                        error = "Live 恢复失败，改存普通照片：${error.message}",
                    )
                    // Persist the choice before retrying so another interruption cannot select the bad package.
                    journalStore.write(record)
                    packaged?.delete()
                    motionPhoto = false
                    primary = source
                    publishPrimary()
                }
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
            record.researchContext?.let { context ->
                requireNotNull(researchRecoveryStore) { "research recovery receipt store unavailable" }.record(
                    com.photocoach.app.research.ResearchRecoveryReceipt(record.captureId,context))
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
                        motionPhoto,
                        BeautyPreset.requireSupported(record.beautyPreset, record.beautyEngineVersion),
                        record.beautyEngineVersion,
                    ),
                )
                record = record.copy(completedStages = record.completedStages + SaveStage.RECIPE_WRITE.name)
                journalStore.write(record)
            }
            if (
                record.derivativeRequested &&
                record.derivativeUri != null
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
                    CaptureSaver.commitVerifiedPending(
                        resolver,
                        publishedUri,
                        displayName,
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
                }.getOrThrow()
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
                    ?: run {
                        fun generate() = creativeProcessor.process(source, style, edit, quality,
                            beautyPreset = BeautyPreset.requireSupported(record.beautyPreset, record.beautyEngineVersion)).file
                        pendingSources?.generate { generate() } ?: generate()
                    }
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
                journalStore.write(
                    record.copy(
                        failedStage = recoveryStage?.name,
                        error = error.message ?: "恢复保存失败",
                    ),
                )
            }
        }
    }

    // Do not forget a row or publish a replacement when MediaStore refuses its deletion.
    private fun deleteFailedAsset(uri: Uri?) {
        CaptureSaver.deletePendingOnly(resolver, uri)
    }

    private fun cleanupRecoveredRecord(record: SaveJournal) {
        listOfNotNull(record.sourcePath, record.motionPath, record.packagedPath, record.derivativePath,
            record.jpegRawPath,record.jpegPreparation?.rawPath,JpegPreparation.temporaryFor(File(record.sourcePath)).path)
            .map(::File)
            .forEach {file ->check(!file.exists() || file.delete()){ "已发布照片的暂存清理待重试" }}
    }
}
