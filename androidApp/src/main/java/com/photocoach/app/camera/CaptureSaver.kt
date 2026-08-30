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
        onPendingCreated: ((Uri) -> Unit)? = null,
    ): Uri {
        require(source.isFile && source.length() > 0L) { "captured photo is empty" }
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
        try {
            onPendingCreated?.invoke(uri)
            writeAndCommit(resolver, uri, source)
            return uri
        } catch (error: Throwable) {
            resolver.delete(uri, null, null)
            throw error
        }
    }

    fun resumePending(resolver: ContentResolver, uri: Uri, source: File): Uri {
        require(source.isFile && source.length() > 0L) { "captured photo is empty" }
        writeAndCommit(resolver, uri, source)
        return uri
    }

    fun deleteQuietly(resolver: ContentResolver, uri: Uri?) {
        if (uri != null) runCatching { resolver.delete(uri, null, null) }
    }

    private fun writeAndCommit(resolver: ContentResolver, uri: Uri, source: File) {
        resolver.openOutputStream(uri, "w")?.use { output ->
            source.inputStream().buffered().use { input -> input.copyTo(output) }
        } ?: throw IOException("cannot open MediaStore output")
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
