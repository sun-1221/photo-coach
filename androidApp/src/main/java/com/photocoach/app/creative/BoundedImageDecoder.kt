package com.photocoach.app.creative

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import androidx.core.graphics.createBitmap
import androidx.exifinterface.media.ExifInterface
import java.io.IOException

data class DecodedLocalImage(
    val bitmap: Bitmap,
    val originalWidth: Int,
    val originalHeight: Int,
    val wasDownsampled: Boolean,
)

class BoundedImageDecoder(
    private val resolver: ContentResolver,
) {
    fun decode(source: Uri, maximumPixels: Long): DecodedLocalImage {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        open(source).use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw IOException("无法读取照片尺寸")
        }
        val sample = ImageDecodePolicy.inSampleSize(bounds.outWidth, bounds.outHeight, maximumPixels)
        val decoded = try {
            open(source).use {
                BitmapFactory.decodeStream(
                    it,
                    null,
                    BitmapFactory.Options().apply {
                        inSampleSize = sample
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                    },
                )
            }
        } catch (error: OutOfMemoryError) {
            throw IOException("照片预览内存不足", error)
        } ?: throw IOException("无法解码照片")

        val orientation = runCatching {
            open(source).use { exifRotation(ExifInterface(it)) }
        }.getOrDefault(0)
        val oriented = try {
            rotate(decoded, orientation)
        } catch (error: Throwable) {
            decoded.recycle()
            if (error is OutOfMemoryError) throw IOException("照片预览内存不足", error)
            throw error
        }
        if (oriented !== decoded) decoded.recycle()
        return DecodedLocalImage(
            bitmap = oriented,
            originalWidth = bounds.outWidth,
            originalHeight = bounds.outHeight,
            wasDownsampled = sample > 1,
        )
    }

    private fun open(source: Uri) = resolver.openInputStream(source)
        ?: throw IOException("无法读取照片")

    private fun rotate(source: Bitmap, degrees: Int): Bitmap {
        if (degrees == 0) return source
        val rotated = degrees == 90 || degrees == 270
        val target = createBitmap(
            if (rotated) source.height else source.width,
            if (rotated) source.width else source.height,
        )
        return try {
            Canvas(target).drawBitmap(
                source,
                rotationMatrix(degrees, source.width, source.height),
                Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG),
            )
            target
        } catch (error: Throwable) {
            target.recycle()
            throw error
        }
    }

    private fun rotationMatrix(degrees: Int, width: Int, height: Int): Matrix = Matrix().apply {
        setRotate(degrees.toFloat())
        val bounds = RectF(0f, 0f, width.toFloat(), height.toFloat())
        mapRect(bounds)
        postTranslate(-bounds.left, -bounds.top)
    }

    private fun exifRotation(exif: ExifInterface): Int = when (
        exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    ) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90
        ExifInterface.ORIENTATION_ROTATE_180 -> 180
        ExifInterface.ORIENTATION_ROTATE_270 -> 270
        else -> 0
    }
}
