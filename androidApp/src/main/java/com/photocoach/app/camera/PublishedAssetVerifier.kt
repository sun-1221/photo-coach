package com.photocoach.app.camera

import android.content.ContentResolver
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

data class VerifiedAsset(
    val length: Long,
    val width: Int,
    val height: Int,
    val exifOrientation: Int,
    val mimeType: String,
    val displayName: String,
    val relativePath: String?,
)

object AssetIntegrityValidator {
    private val allowedOrientations = setOf(
        ExifInterface.ORIENTATION_UNDEFINED,
        ExifInterface.ORIENTATION_NORMAL,
        ExifInterface.ORIENTATION_ROTATE_90,
        ExifInterface.ORIENTATION_ROTATE_180,
        ExifInterface.ORIENTATION_ROTATE_270,
    )

    fun validate(length: Long, width: Int, height: Int, orientation: Int) {
        require(length > 0L) { "published asset is empty" }
        require(width > 0 && height > 0) { "JPEG dimensions are unreadable" }
        require(orientation in allowedOrientations) { "unsupported EXIF orientation: $orientation" }
    }

    fun validateMotionHeader(header: ByteArray, totalLength: Long) {
        val text = header.toString(StandardCharsets.UTF_8)
        require(
            text.contains("GCamera:MotionPhoto=\"1\"") ||
                text.contains("Camera:MotionPhoto=\"1\""),
        ) { "Motion Photo flag is missing" }
        require(Regex("Item:Semantic=\\\"Primary\\\"").findAll(text).count() == 1) { "Motion Photo must contain one Primary item" }
        require(Regex("Item:Semantic=\\\"MotionPhoto\\\"").findAll(text).count() == 1) { "Motion Photo must contain one MotionPhoto item" }
        val videoLength = Regex("Item:Length=\\\"(\\d+)\\\"").find(text)?.groupValues?.get(1)?.toLongOrNull()
            ?: throw IllegalArgumentException("Motion Photo video length is missing")
        require(videoLength in 12 until totalLength) { "Motion Photo video length is invalid" }
    }
}

object PublishedAssetVerifier {
    private const val MOTION_HEADER_LIMIT = 512 * 1024

    fun verifyPending(resolver: ContentResolver, uri: Uri, motionPhoto: Boolean): VerifiedAsset =
        inspectContent(resolver, uri, expectedDisplayName = null, expectedPath = null, motionPhoto = motionPhoto)

    fun verifyPublished(
        resolver: ContentResolver,
        uri: Uri,
        expectedDisplayName: String,
        expectedPath: String,
        motionPhoto: Boolean,
    ): VerifiedAsset = inspectContent(resolver, uri, expectedDisplayName, expectedPath, motionPhoto)

    private fun inspectContent(
        resolver: ContentResolver,
        uri: Uri,
        expectedDisplayName: String?,
        expectedPath: String?,
        motionPhoto: Boolean,
    ): VerifiedAsset {
        val metadata = queryMetadata(resolver, uri)
        if (expectedDisplayName != null && metadata.displayName != expectedDisplayName) {
            throw IOException("published display name mismatch")
        }
        if (metadata.mimeType != "image/jpeg") throw IOException("published MIME is not image/jpeg")
        if (expectedPath != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            normalizePath(metadata.relativePath) != normalizePath(expectedPath)) {
            throw IOException("published relative path mismatch")
        }
        val descriptorLength = resolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L
        val length = metadata.length.takeIf { it > 0L } ?: descriptorLength
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        val descriptor = resolver.openFileDescriptor(uri, "r")
            ?: throw IOException("cannot open JPEG for dimensions")
        descriptor.use { BitmapFactory.decodeFileDescriptor(it.fileDescriptor, null, bounds) }
        val orientation = resolver.openInputStream(uri)?.use {
            ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        } ?: throw IOException("cannot open JPEG EXIF")
        AssetIntegrityValidator.validate(length, bounds.outWidth, bounds.outHeight, orientation)
        verifyJpegEnds(resolver, uri, length, motionPhoto)
        if (motionPhoto) verifyMotionTail(resolver, uri, length)
        return VerifiedAsset(length, bounds.outWidth, bounds.outHeight, orientation,
            metadata.mimeType, metadata.displayName, metadata.relativePath)
    }

    private fun queryMetadata(resolver: ContentResolver, uri: Uri): Metadata {
        val columns = buildList {
            add(MediaStore.Images.Media.DISPLAY_NAME)
            add(MediaStore.Images.Media.MIME_TYPE)
            add(MediaStore.Images.Media.SIZE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) add(MediaStore.Images.Media.RELATIVE_PATH)
        }.toTypedArray()
        return resolver.query(uri, columns, null, null, null)?.use { cursor ->
            if (!cursor.moveToFirst()) throw IOException("published MediaStore row is missing")
            fun string(column: String): String? = cursor.getColumnIndex(column).takeIf { it >= 0 }?.let(cursor::getString)
            fun long(column: String): Long = cursor.getColumnIndex(column).takeIf { it >= 0 }?.let(cursor::getLong) ?: -1L
            Metadata(
                displayName = string(MediaStore.Images.Media.DISPLAY_NAME).orEmpty(),
                mimeType = string(MediaStore.Images.Media.MIME_TYPE).orEmpty(),
                length = long(MediaStore.Images.Media.SIZE),
                relativePath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) string(MediaStore.Images.Media.RELATIVE_PATH) else null,
            )
        } ?: throw IOException("cannot query published MediaStore row")
    }

    private fun verifyJpegEnds(resolver: ContentResolver, uri: Uri, length: Long, motionPhoto: Boolean) {
        require(length >= 4L) { "JPEG is too short" }
        resolver.openFileDescriptor(uri, "r")?.use { descriptor ->
            FileInputStream(descriptor.fileDescriptor).channel.use { channel ->
                val prefix = ByteBuffer.allocate(2)
                channel.read(prefix, 0L)
                require(prefix.array().contentEquals(byteArrayOf(0xff.toByte(), 0xd8.toByte()))) { "invalid JPEG SOI" }
                if (!motionPhoto) {
                    val suffix = ByteBuffer.allocate(2)
                    channel.read(suffix, length - 2L)
                    require(suffix.array().contentEquals(byteArrayOf(0xff.toByte(), 0xd9.toByte()))) { "invalid JPEG EOI" }
                }
            }
        } ?: throw IOException("cannot verify JPEG structure")
    }

    private fun verifyMotionTail(resolver: ContentResolver, uri: Uri, length: Long) {
        resolver.openFileDescriptor(uri, "r")?.use { descriptor ->
            FileInputStream(descriptor.fileDescriptor).channel.use { channel ->
                val header = ByteBuffer.allocate(minOf(length, MOTION_HEADER_LIMIT.toLong()).toInt())
                channel.read(header, 0L)
                AssetIntegrityValidator.validateMotionHeader(header.array(), length)
                val text = header.array().toString(StandardCharsets.UTF_8)
                val videoLength = Regex("Item:Length=\\\"(\\d+)\\\"").find(text)!!.groupValues[1].toLong()
                val eoi = ByteBuffer.allocate(2)
                channel.read(eoi, length - videoLength - 2L)
                require(eoi.array().contentEquals(byteArrayOf(0xff.toByte(), 0xd9.toByte()))) { "Motion Photo primary JPEG has no EOI" }
                val ftyp = ByteBuffer.allocate(4)
                channel.read(ftyp, length - videoLength + 4L)
                require(ftyp.array().contentEquals(byteArrayOf('f'.code.toByte(), 't'.code.toByte(), 'y'.code.toByte(), 'p'.code.toByte()))) {
                    "Motion Photo video is not at file tail"
                }
            }
        } ?: throw IOException("cannot verify Motion Photo structure")
    }

    private fun normalizePath(value: String?): String = value.orEmpty().trim().trimEnd('/')
    private data class Metadata(val displayName: String, val mimeType: String, val length: Long, val relativePath: String?)
}
