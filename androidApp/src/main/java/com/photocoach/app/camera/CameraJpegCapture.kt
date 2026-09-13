package com.photocoach.app.camera

import android.graphics.ImageFormat
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executor

internal interface JpegSaveCallback {
    fun onImageSaved()
    fun onError(exception:ImageCaptureException)
}

/** The JPEG bytes and SENSOR_TIMESTAMP come from the very same ImageProxy. */
internal fun captureTimestampedJpeg(capture:ImageCapture,file:File,io:Executor,main:Executor,
    timestamp:(Long?)->Unit,persistPreparation:(JpegPreparation?)->Unit,callback:JpegSaveCallback) {
    capture.takePicture(io,object:ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(image:ImageProxy) {
            val result=runCatching {prepareCapturedJpeg(image,file,capture.jpegQuality,
                onTimestamp={sensor -> main.execute {timestamp(sensor)}},persistPreparation=persistPreparation)}
            main.execute {
                result.fold({callback.onImageSaved()},
                    {callback.onError(ImageCaptureException(ImageCapture.ERROR_FILE_IO,"JPEG 保存失败",it))})
            }
        }
        override fun onError(exception:ImageCaptureException) {main.execute {callback.onError(exception)}}
    })
}

internal fun prepareCapturedJpeg(image:ImageProxy,file:File,quality:Int,onTimestamp:(Long?)->Unit={},
    persistPreparation:(JpegPreparation?)->Unit):Long? {
                try {
                val sensorNs=image.imageInfo.timestamp.takeIf {it>0}
                onTimestamp(sensorNs)
                require(image.format==ImageFormat.JPEG){"Live still is not JPEG"}
                val raw=File(file.parentFile,"${file.name}.capture-raw")
                val crop=image.cropRect
                val preparation=JpegPreparation(raw.path,image.width,image.height,crop.left,crop.top,crop.right,crop.bottom,
                    image.imageInfo.rotationDegrees,quality)
                persistPreparation(preparation)
                FileOutputStream(raw).use {stream ->
                    val bytes=image.planes.single().buffer.duplicate()
                    require(bytes.remaining() in 1..64*1024*1024)
                    while(bytes.hasRemaining())stream.channel.write(bytes)
                    stream.fd.sync()
                }
                val inspected=preparation.copy(motionCompatible=runCatching {MotionPhotoAssembler.verifySourceCompatibility(raw)}.isSuccess)
                persistPreparation(inspected)
                inspected.prepare(file)
                persistPreparation(null)
                return sensorNs
                } finally {runCatching {image.close()}.onFailure {android.util.Log.w("MotionPipeline","JPEG proxy close failed",it)}}
}
