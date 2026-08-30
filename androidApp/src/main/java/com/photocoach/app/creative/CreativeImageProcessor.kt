package com.photocoach.app.creative

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.exifinterface.media.ExifInterface
import com.photocoach.app.camera.DerivativeQuality
import java.io.File
import java.io.IOException
import kotlin.math.max
import kotlin.math.roundToInt

data class ProcessedImage(
    val file: File,
    val width: Int,
    val height: Int,
    val wasDownsampled: Boolean,
)

class CreativeImageProcessor(
    private val cacheDirectory: File,
    private val maximumPixels: Long = MAXIMUM_PIXELS,
) {
    fun score(source: File): PhotoQualityScore {
        val bitmap = decodeFile(source, QUALITY_SAMPLE_PIXELS).bitmap
        return try {
            val scaled = scaleForScoring(bitmap)
            try {
                PhotoQualityScorer.score(scaled.toLuminanceFrame())
            } finally {
                if (scaled !== bitmap) scaled.recycle()
            }
        } finally {
            bitmap.recycle()
        }
    }

    fun process(
        source: File,
        style: CreativeStyle,
        edit: EditAdjustment = EditAdjustment(),
        quality: DerivativeQuality = DerivativeQuality.FULL,
    ): ProcessedImage {
        val exif = runCatching { ExifInterface(source.absolutePath) }.getOrNull()
        return processDecoded(
            decoded = decodeFile(source, pixelLimit(quality)),
            orientation = exif?.let(::exifRotation) ?: 0,
            safeExif = exif?.let(::safeExif) ?: emptyMap(),
            style = style,
            edit = edit,
            quality = quality,
        )
    }

    fun process(
        resolver: ContentResolver,
        source: Uri,
        style: CreativeStyle,
        edit: EditAdjustment,
        quality: DerivativeQuality = DerivativeQuality.FULL,
    ): ProcessedImage {
        val exif = runCatching {
            resolver.openInputStream(source)?.use { ExifInterface(it) }
        }.getOrNull()
        return processDecoded(
            decodeUri(resolver, source, pixelLimit(quality)),
            exif?.let(::exifRotation) ?: 0,
            exif?.let(::safeExif) ?: emptyMap(),
            style,
            edit,
            quality,
        )
    }

    private fun processDecoded(
        decoded: DecodedBitmap,
        orientation: Int,
        safeExif: Map<String, String>,
        style: CreativeStyle,
        edit: EditAdjustment,
        quality: DerivativeQuality,
    ): ProcessedImage {
        return try {
            ProcessingResourceScope<Bitmap, File>(Bitmap::recycle, { it.delete() }).use { resources ->
                val source = resources.ownSource(decoded.bitmap)
                val rotated = orientation == 90 || orientation == 270
                val outputWidth = if (rotated) source.height else source.width
                val outputHeight = if (rotated) source.width else source.height
                val target = resources.ownTarget(createBitmap(outputWidth, outputHeight))
                val output = resources.ownTemporaryFile(newTemporaryFile())

                val canvas = Canvas(target)
                val transform = rotationMatrix(orientation, source.width, source.height)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                    colorFilter = ColorMatrixColorFilter(ColorMatrix(CreativeColorMatrix.forSelection(style, edit)))
                }
                canvas.drawBitmap(source, transform, paint)
                output.outputStream().buffered().use { stream ->
                    if (!target.compress(Bitmap.CompressFormat.JPEG, jpegQuality(quality), stream)) {
                        throw IOException("无法编码创意副本")
                    }
                }
                writeSafeExif(output, safeExif, outputWidth, outputHeight)
                ProcessedImage(
                    resources.releaseTemporaryFile(output),
                    outputWidth,
                    outputHeight,
                    decoded.wasDownsampled,
                )
            }
        } catch (error: OutOfMemoryError) {
            throw IOException("创意效果内存不足，已保留原片", error)
        }
    }

    private fun decodeFile(source: File, pixelLimit: Long): DecodedBitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(source.absolutePath, bounds)
        validateBounds(bounds)
        val sample = ImageDecodePolicy.inSampleSize(bounds.outWidth, bounds.outHeight, pixelLimit)
        val bitmap = try {
            BitmapFactory.decodeFile(
                source.absolutePath,
                BitmapFactory.Options().apply {
                    inSampleSize = sample
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                },
            )
        } catch (error: OutOfMemoryError) {
            throw IOException("照片过大，已保留原片", error)
        } ?: throw IOException("无法解码照片，已保留原片")
        return DecodedBitmap(bitmap, sample > 1)
    }

    private fun decodeUri(resolver: ContentResolver, source: Uri, pixelLimit: Long): DecodedBitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(source)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            ?: throw IOException("无法读取原片")
        validateBounds(bounds)
        val sample = ImageDecodePolicy.inSampleSize(bounds.outWidth, bounds.outHeight, pixelLimit)
        val bitmap = try {
            resolver.openInputStream(source)?.use {
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
            throw IOException("照片过大，已保留原片", error)
        } ?: throw IOException("无法解码原片")
        return DecodedBitmap(bitmap, sample > 1)
    }

    private fun scaleForScoring(source: Bitmap): Bitmap {
        val maxSide = max(source.width, source.height)
        if (maxSide <= QUALITY_SAMPLE_SIDE) return source
        val scale = QUALITY_SAMPLE_SIDE.toFloat() / maxSide
        return source.scale(
            (source.width * scale).roundToInt().coerceAtLeast(3),
            (source.height * scale).roundToInt().coerceAtLeast(3),
        )
    }

    private fun Bitmap.toLuminanceFrame(): LuminanceFrame {
        val pixels = IntArray(width * height)
        getPixels(pixels, 0, width, 0, 0, width, height)
        val luma = IntArray(pixels.size) { index ->
            val color = pixels[index]
            val red = color shr 16 and 0xff
            val green = color shr 8 and 0xff
            val blue = color and 0xff
            ((red * 54 + green * 183 + blue * 19) shr 8).coerceIn(0, 255)
        }
        return LuminanceFrame(width, height, luma)
    }

    private fun rotationMatrix(degrees: Int, width: Int, height: Int): Matrix = Matrix().apply {
        if (degrees == 0) return@apply
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

    private fun safeExif(exif: ExifInterface): Map<String, String> = SAFE_EXIF_TAGS.mapNotNull { tag ->
        exif.getAttribute(tag)?.let { tag to it }
    }.toMap()

    private fun writeSafeExif(output: File, attributes: Map<String, String>, width: Int, height: Int) {
        runCatching {
            ExifInterface(output.absolutePath).apply {
                attributes.forEach { (tag, value) -> setAttribute(tag, value) }
                setAttribute(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL.toString())
                setAttribute(ExifInterface.TAG_IMAGE_WIDTH, width.toString())
                setAttribute(ExifInterface.TAG_IMAGE_LENGTH, height.toString())
                saveAttributes()
            }
        }.getOrElse { throw IOException("无法写入安全 EXIF", it) }
    }

    private fun pixelLimit(quality: DerivativeQuality): Long = when (quality) {
        DerivativeQuality.FULL -> maximumPixels
        DerivativeQuality.SPACE_SAVER -> minOf(maximumPixels, SPACE_SAVER_PIXELS)
    }

    private fun jpegQuality(quality: DerivativeQuality): Int = when (quality) {
        DerivativeQuality.FULL -> JPEG_QUALITY
        DerivativeQuality.SPACE_SAVER -> SPACE_SAVER_JPEG_QUALITY
    }

    private fun newTemporaryFile(): File {
        val directory = File(cacheDirectory, "creative-exports")
        check(directory.exists() || directory.mkdirs()) { "cannot create creative export directory" }
        return File.createTempFile("creative-", ".jpg", directory)
    }

    private fun validateBounds(options: BitmapFactory.Options) {
        if (options.outWidth <= 0 || options.outHeight <= 0) throw IOException("无法读取照片尺寸")
    }

    private data class DecodedBitmap(val bitmap: Bitmap, val wasDownsampled: Boolean)

    companion object {
        const val MAXIMUM_PIXELS = ImageDecodePolicy.EXPORT_MAX_PIXELS
        private const val QUALITY_SAMPLE_PIXELS = 262_144L
        private const val QUALITY_SAMPLE_SIDE = 256
        private const val JPEG_QUALITY = 94
        private const val SPACE_SAVER_JPEG_QUALITY = 86
        private const val SPACE_SAVER_PIXELS = 3_000_000L
        private val SAFE_EXIF_TAGS = listOf(
            ExifInterface.TAG_DATETIME,
            ExifInterface.TAG_DATETIME_ORIGINAL,
            ExifInterface.TAG_DATETIME_DIGITIZED,
            ExifInterface.TAG_MAKE,
            ExifInterface.TAG_MODEL,
        )
    }
}

internal class ProcessingResourceScope<B : Any, F : Any>(
    private val recycleBitmap: (B) -> Unit,
    private val deleteTemporaryFile: (F) -> Unit,
) : AutoCloseable {
    private var source: B? = null
    private var target: B? = null
    private var temporaryFile: F? = null

    fun ownSource(value: B): B = value.also {
        check(source == null) { "source already owned" }
        source = it
    }

    fun ownTarget(value: B): B = value.also {
        check(target == null) { "target already owned" }
        target = it
    }

    fun ownTemporaryFile(value: F): F = value.also {
        check(temporaryFile == null) { "temporary file already owned" }
        temporaryFile = it
    }

    fun releaseTemporaryFile(value: F): F {
        check(temporaryFile == value) { "temporary file is not owned" }
        temporaryFile = null
        return value
    }

    override fun close() {
        temporaryFile?.let { runCatching { deleteTemporaryFile(it) } }
        temporaryFile = null
        target?.let { runCatching { recycleBitmap(it) } }
        target = null
        source?.let { runCatching { recycleBitmap(it) } }
        source = null
    }
}
