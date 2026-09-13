package com.photocoach.app.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Rect
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlinx.serialization.Serializable

/** Durable initial camera-save crop. Motion packaging never runs this operation again. */
@Serializable
data class JpegPreparation(val rawPath:String,val width:Int,val height:Int,
    val left:Int,val top:Int,val right:Int,val bottom:Int,val rotation:Int,val quality:Int,
    val motionCompatible:Boolean?=null) {
    init {
        require(width>0 && height>0 && left>=0 && top>=0 && right in (left+1)..width && bottom in (top+1)..height)
        require(rotation in listOf(0,90,180,270) && quality in 1..100)
    }
    internal fun prepare(output:File,maxPixels:Long=Runtime.getRuntime().maxMemory()/12L,beforeExif:()->Unit={}) {
        val cropped=left!=0 || top!=0 || right!=width || bottom!=height
        if(cropped)require((right-left).toLong()*(bottom-top)<=maxPixels){"JPEG crop exceeds current memory budget; raw source retained"}
        val raw=File(rawPath)
        val bounds=BitmapFactory.Options().apply {inJustDecodeBounds=true}
        BitmapFactory.decodeFile(raw.path,bounds)
        require(bounds.outWidth==width && bounds.outHeight==height){"JPEG dimensions do not match capture crop"}
        val temporary=temporaryFor(output)
        try {
            if(left==0 && top==0 && right==width && bottom==height)raw.copyTo(temporary,overwrite=true)
            else {
                @Suppress("DEPRECATION") val decoder=BitmapRegionDecoder.newInstance(raw.path,false)
                try {
                    val bitmap=requireNotNull(decoder.decodeRegion(Rect(left,top,right,bottom),BitmapFactory.Options()))
                    try {FileOutputStream(temporary).use {check(bitmap.compress(Bitmap.CompressFormat.JPEG,quality,it));it.fd.sync()}}
                    finally {bitmap.recycle()}
                } finally {decoder.recycle()}
            }
            beforeExif()
            val sourceExif=ExifInterface(raw)
            ExifInterface(temporary).apply {
                for(tag in listOf(ExifInterface.TAG_MAKE,ExifInterface.TAG_MODEL,ExifInterface.TAG_DATETIME,
                    ExifInterface.TAG_DATETIME_ORIGINAL,ExifInterface.TAG_EXPOSURE_TIME,ExifInterface.TAG_F_NUMBER,
                    ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY,ExifInterface.TAG_FOCAL_LENGTH,ExifInterface.TAG_FLASH,
                    ExifInterface.TAG_WHITE_BALANCE,ExifInterface.TAG_EXPOSURE_BIAS_VALUE))
                    sourceExif.getAttribute(tag)?.let {setAttribute(tag,it)}
                setAttribute(ExifInterface.TAG_ORIENTATION,when(rotation) {
                    90->ExifInterface.ORIENTATION_ROTATE_90;180->ExifInterface.ORIENTATION_ROTATE_180
                    270->ExifInterface.ORIENTATION_ROTATE_270;else->ExifInterface.ORIENTATION_NORMAL
                }.toString())
                saveAttributes()
            }
            FileOutputStream(temporary,true).use {it.fd.sync()}
            Files.move(temporary.toPath(),output.toPath(),StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING)
        } finally {temporary.delete()}
        // Caller durably clears the preparation record before deleting the raw source.
    }
    companion object {internal fun temporaryFor(output:File)=File(output.parentFile,"${output.name}.jpeg-preparing")}
}
