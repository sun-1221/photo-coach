package com.photocoach.app.camera

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CaptureSaver {
    const val RELATIVE_DIR = "DCIM/拍照教练"

    fun newDisplayName(now: Date = Date()): String {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(now)
        return "IMG_$stamp.jpg"
    }

    fun publish(
        resolver: ContentResolver,
        source: File,
        displayName: String = newDisplayName(),
        dateTakenMillis: Long = System.currentTimeMillis(),
        motionPhoto: Boolean = displayName.contains("MP.", ignoreCase = true),
        onAssetStage: ((AssetPublishStage) -> Unit)? = null,
        onPendingCreated: ((Uri) -> Unit)? = null,
        existingUri: Uri? = null,
    ): Uri {
        require(source.isFile && source.length() > 0L) { "captured photo is empty" }
        if (existingUri != null) {
            val exists = rowExists(resolver, existingUri)
            if (exists) {
                // Never overwrite an already published asset on a retry.
                val verified = runCatching {
                    PublishedAssetVerifier.verifyPublished(resolver, existingUri, displayName, RELATIVE_DIR, motionPhoto)
                }.isSuccess
                if (verified) {
                    onAssetStage?.invoke(AssetPublishStage.VERIFY_PUBLISHED)
                    return existingUri
                }
                return resumePending(resolver, existingUri, source, displayName, motionPhoto,
                    onAssetStage = onAssetStage)
            }
        }
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.DATE_TAKEN, dateTakenMillis)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, RELATIVE_DIR)
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        val uri = resolver.insert(collection, values) ?: throw IOException("cannot create MediaStore row")
        var publishedVerified = false
        try {
            onPendingCreated?.invoke(uri)
            onAssetStage?.invoke(AssetPublishStage.MEDIASTORE_INSERT_PENDING)
            writePending(resolver, uri, source)
            onAssetStage?.invoke(AssetPublishStage.ORIGINAL_COPY)
            PublishedAssetVerifier.verifyPending(resolver, uri, motionPhoto)
            onAssetStage?.invoke(AssetPublishStage.VERIFY_PENDING)
            commit(resolver, uri)
            onAssetStage?.invoke(AssetPublishStage.MEDIASTORE_COMMIT)
            PublishedAssetVerifier.verifyPublished(resolver, uri, displayName, RELATIVE_DIR, motionPhoto)
            publishedVerified = true
            onAssetStage?.invoke(AssetPublishStage.VERIFY_PUBLISHED)
            return uri
        } catch (error: Throwable) {
            // The caller's journal retains this identity even if cleanup is refused.
            if (!publishedVerified) runCatching { deletePendingOnly(resolver, uri) }.exceptionOrNull()?.let(error::addSuppressed)
            throw error
        }
    }

    fun resumePending(
        resolver: ContentResolver,
        uri: Uri,
        source: File,
        displayName: String,
        motionPhoto: Boolean = false,
        pendingAlreadyVerified: Boolean = false,
        onAssetStage: ((AssetPublishStage) -> Unit)? = null,
    ): Uri {
        require(source.isFile && source.length() > 0L) { "captured photo is empty" }
        if (runCatching {
            PublishedAssetVerifier.verifyPublished(resolver, uri, displayName, RELATIVE_DIR, motionPhoto)
        }.isSuccess) {
            onAssetStage?.invoke(AssetPublishStage.VERIFY_PUBLISHED)
            return uri
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            resolver.query(uri, arrayOf(MediaStore.Images.Media.IS_PENDING), null, null, null)
                ?.use { it.moveToFirst() && it.getInt(0) == 1 } != true) {
            throw IOException("原行不是可续写的 pending 资产；需确认清理后重试")
        }
        if (!pendingAlreadyVerified) {
            writePending(resolver, uri, source)
            onAssetStage?.invoke(AssetPublishStage.ORIGINAL_COPY)
        }
        PublishedAssetVerifier.verifyPending(resolver, uri, motionPhoto)
        onAssetStage?.invoke(AssetPublishStage.VERIFY_PENDING)
        commit(resolver, uri)
        onAssetStage?.invoke(AssetPublishStage.MEDIASTORE_COMMIT)
        PublishedAssetVerifier.verifyPublished(resolver, uri, displayName, RELATIVE_DIR, motionPhoto)
        onAssetStage?.invoke(AssetPublishStage.VERIFY_PUBLISHED)
        return uri
    }

    /** Recover fully written bytes even when the temporary package is gone. Never rewrite a
     * published row, and verify identity/content before changing the pending visibility flag.
     */
    internal fun commitVerifiedPending(resolver:ContentResolver,uri:Uri,displayName:String,motionPhoto:Boolean):Uri {
        if(runCatching {PublishedAssetVerifier.verifyPublished(resolver,uri,displayName,RELATIVE_DIR,motionPhoto)}.isSuccess)return uri
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.Q ||
            resolver.query(uri,arrayOf(MediaStore.Images.Media.IS_PENDING),null,null,null)?.use {it.moveToFirst() && it.getInt(0)==1}!=true)
            throw IOException("原行不是可提交的 pending 资产；保留原行")
        PublishedAssetVerifier.verifyPending(resolver,uri,motionPhoto,displayName,RELATIVE_DIR)
        commit(resolver,uri)
        PublishedAssetVerifier.verifyPublished(resolver,uri,displayName,RELATIVE_DIR,motionPhoto)
        return uri
    }

    fun deleteQuietly(resolver: ContentResolver, uri: Uri?) {
        if (uri != null) runCatching { resolver.delete(uri, null, null) }
    }

    fun deleteConfirmed(resolver: ContentResolver, uri: Uri?) {
        if (uri == null) return
        retireAsset({ rowExists(resolver, uri) }, { resolver.delete(uri, null, null) })
    }

    /** Automatic recovery never removes a published or unverifiable row. Explicit abandonment uses deleteConfirmed. */
    internal fun deletePendingOnly(resolver: ContentResolver, uri: Uri?) {
        if (uri == null || !rowExists(resolver, uri)) return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            resolver.query(uri, arrayOf(MediaStore.Images.Media.IS_PENDING), null, null, null)
                ?.use { it.moveToFirst() && it.getInt(0) == 1 } != true) {
            throw IOException("照片已发布或状态无法确认，保留原行供重试核验")
        }
        deleteConfirmed(resolver, uri)
    }

    private fun rowExists(resolver: ContentResolver, uri: Uri): Boolean =
        resolver.query(uri, arrayOf(MediaStore.Images.Media._ID), null, null, null)
            ?.use { it.moveToFirst() } ?: throw IOException("无法核验原照片状态")

    private fun writePending(resolver: ContentResolver, uri: Uri, source: File) {
        resolver.openOutputStream(uri, "w")?.use { output ->
            source.inputStream().buffered().use { input -> input.copyTo(output) }
        } ?: throw IOException("cannot open MediaStore output")
    }

    private fun commit(resolver: ContentResolver, uri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val updated = resolver.update(
                uri,
                ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) },
                null,
                null,
            )
            if (updated != 1) throw IOException("cannot publish MediaStore row")
        }
    }
}

enum class AssetPublishStage {
    MEDIASTORE_INSERT_PENDING,
    ORIGINAL_COPY,
    VERIFY_PENDING,
    MEDIASTORE_COMMIT,
    VERIFY_PUBLISHED,
}

enum class PublishedAssetKind {
    ORIGINAL,
    DERIVATIVE,
}

internal fun assetStageKey(kind: PublishedAssetKind, stage: AssetPublishStage): String =
    "${kind.name}:${stage.name}"

internal fun Set<String>.containsAssetStage(kind: PublishedAssetKind, stage: AssetPublishStage): Boolean =
    assetStageKey(kind, stage) in this ||
        (kind == PublishedAssetKind.ORIGINAL && stage.name in this)

internal fun Set<String>.withoutAssetStages(kind: PublishedAssetKind): Set<String> = filterNot { value ->
    value.startsWith("${kind.name}:") ||
        (kind == PublishedAssetKind.ORIGINAL && AssetPublishStage.entries.any { it.name == value })
}.toSet()
