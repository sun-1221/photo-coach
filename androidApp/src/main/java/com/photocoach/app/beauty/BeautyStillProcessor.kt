package com.photocoach.app.beauty

import android.graphics.Bitmap
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlin.math.roundToInt

/** Save-worker only. No second full-size bitmap or full-size pixel array. */
internal object BeautyStillProcessor {
    fun apply(bitmap:Bitmap,rotation:Int,preset:BeautyPreset):String? {
        if(preset==BeautyPreset.OFF) return null
        check(bitmap.isMutable)
        val detector=FaceDetection.getClient(FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
            .setMinFaceSize(.05f).build())
        // Await completion before releasing the bitmap/detector; a timed-out task may still own it.
        val frame=try {
            beautyFaceFrame(Tasks.await(detector.process(InputImage.fromBitmap(bitmap,rotation))),0L,BeautyTransform())
        } finally { detector.close() }
        val mask=frame.mask ?: return "成片未检测到可处理的单人正脸，未应用美颜"
        val rawToAnalysis=BeautyTransform.rotation(bitmap.width,bitmap.height,rotation)
        applyMask(bitmap, mask.copy(analysisToLocal = mask.analysisToLocal * rawToAnalysis), preset)
        return null
    }

    internal fun applyMask(bitmap: Bitmap, mask: BeautyMask, preset: BeautyPreset) {
        if (preset == BeautyPreset.OFF) return
        val w=(bitmap.width+3)/4; val h=(bitmap.height+3)/4
        val row=IntArray(bitmap.width)
        // Process one color channel at a time; keep only six quarter-size scalar buffers.
        for(channel in 0..2) {
            val shift=16-channel*8
            val data=FloatArray(w*h)
            for(y in 0 until h) {
                bitmap.getPixels(row,0,bitmap.width,0,(y*4+2).coerceAtMost(bitmap.height-1),bitmap.width,1)
                for(x in 0 until w) data[y*w+x]=((row[(x*4+2).coerceAtMost(bitmap.width-1)] shr shift) and 255)/255f
            }
            val coefficients=GuidedCoefficients.create(data,w,h)
            for(y in 0 until bitmap.height) {
                bitmap.getPixels(row,0,bitmap.width,0,y,bitmap.width,1)
                for(x in row.indices) {
                    val amount=mask.weight(x+.5f,y+.5f)*preset.blend*(1f-preset.detail)
                    if(amount<=0f) continue
                    val original=((row[x] shr shift) and 255)/255f
                    val smooth=coefficients.reconstruct((x+.5f)/bitmap.width,(y+.5f)/bitmap.height,original)
                    val value=((original+(smooth-original)*amount)*255).roundToInt().coerceIn(0,255)
                    row[x]=(row[x] and (255 shl shift).inv()) or (value shl shift)
                }
                bitmap.setPixels(row,0,bitmap.width,0,y,bitmap.width,1)
            }
        }
    }
}

/** Self-guided RGB channels, epsilon in normalized [0,1] units. Shared mathematical contract with GLSL. */
internal class GuidedCoefficients(private val a:FloatArray,private val b:FloatArray,val width:Int,val height:Int) {
    fun reconstruct(u:Float,v:Float,original:Float):Float = sample(a,u,v)*original+sample(b,u,v)
    private fun sample(values:FloatArray,u:Float,v:Float):Float {
        val x=(u*width-.5f).coerceIn(0f,(width-1).toFloat()); val y=(v*height-.5f).coerceIn(0f,(height-1).toFloat())
        val ix=x.toInt(); val iy=y.toInt(); val nx=(ix+1).coerceAtMost(width-1); val ny=(iy+1).coerceAtMost(height-1)
        val top=values[iy*width+ix]*(1-(x-ix))+values[iy*width+nx]*(x-ix)
        val bottom=values[ny*width+ix]*(1-(x-ix))+values[ny*width+nx]*(x-ix)
        return top*(1-(y-iy))+bottom*(y-iy)
    }
    companion object {
        fun create(values:FloatArray,w:Int,h:Int):GuidedCoefficients {
            require(w>0 && h>0 && values.size==w*h)
            val mean=box(values,w,h)
            val second=box(FloatArray(values.size) { values[it]*values[it] },w,h)
            for(i in values.indices) {
                val variance=(second[i]-mean[i]*mean[i]).coerceAtLeast(0f)
                val coefficient=variance/(variance+.0025f)
                second[i]=coefficient; mean[i]*=1f-coefficient
            }
            return GuidedCoefficients(box(second,w,h),box(mean,w,h),w,h)
        }
        private fun box(values:FloatArray,w:Int,h:Int):FloatArray {
            val temp=FloatArray(values.size); val out=FloatArray(values.size)
            for(y in 0 until h) for(x in 0 until w) {
                var sum=0f; for(k in -2..2) sum+=values[y*w+(x+k).coerceIn(0,w-1)]
                temp[y*w+x]=sum/5f
            }
            for(y in 0 until h) for(x in 0 until w) {
                var sum=0f; for(k in -2..2) sum+=temp[(y+k).coerceIn(0,h-1)*w+x]
                out[y*w+x]=sum/5f
            }
            return out
        }
    }
}
