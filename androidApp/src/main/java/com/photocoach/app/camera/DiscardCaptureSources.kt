package com.photocoach.app.camera

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import java.io.File
import java.io.IOException

internal class DiscardIntentNotPersisted(cause:Throwable):IOException("未能记录放弃，请重试或继续保存",cause)

/** Persist user intent first. A failed cleanup can never turn into a publication on restart. */
internal fun discardCaptureSources(resolver:ContentResolver,journals:SaveJournalStore,original:SaveJournal,
    delete:(File)->Boolean=File::delete) {
    val record=original.copy(discarded=true)
    try {journals.write(record)} catch(error:Throwable) {throw DiscardIntentNotPersisted(error)}
    try {
        listOfNotNull(record.pendingUri,record.derivativePendingUri).distinct().forEach {value ->
            val uri=Uri.parse(value)
            val pending=resolver.query(uri,arrayOf(MediaStore.MediaColumns.IS_PENDING),null,null,null)?.use {
                if(it.moveToFirst())it.getInt(0)==1 else false
            } ?: error("无法确认待发布照片的清理状态")
            if(pending)CaptureSaver.deletePendingOnly(resolver,uri)
        }
        val files=listOfNotNull(record.sourcePath,record.jpegRawPath,record.jpegPreparation?.rawPath,
            record.motionPath,record.packagedPath,record.derivativePath,
            record.sourcePath.takeIf {it.isNotBlank()}?.let {JpegPreparation.temporaryFor(File(it)).path})
            .filter {it.isNotBlank()}.distinct().map(::File)
        val cleaned=files.map {file ->!file.exists() || file.isFile && delete(file)}.all {it}
        check(cleaned){"照片已放弃，暂存清理待重试"}
        journals.delete(record)
    } catch(error:Throwable) {
        // The tombstone is already durable; failure to add diagnostics cannot revoke it.
        runCatching {journals.write(record.copy(error=error.message ?: "照片已放弃，暂存清理待重试"))}
        throw error
    }
}
