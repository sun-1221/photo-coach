package com.photocoach.app.beauty

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.opengl.GLES30 as G
import android.os.Handler
import android.os.Looper
import android.view.Surface
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.math.abs

/** Synthetic GPU/CPU contract checks, not photographic quality or target-device acceptance. */
@RunWith(AndroidJUnit4::class)
class BeautyRendererTest {
    private val size = 96
    private val mask = BeautyMask(BeautyTransform(a=1f/size,e=1f/size),
        List(4) { BeautyEllipse(.5f,.5f,.42f,.42f) },
        List(4) { BeautyEllipse(.5f,.5f,.08f,.08f) })

    private fun pixels(width: Int = size, height: Int = size): ByteArray = ByteArray(width*height*4) { i ->
        if (i%4==3) 255.toByte() else (100+((i/4%width+i/4/width)%2)*50).toByte()
    }

    private fun upload(renderer: BeautyGlRenderer, data: ByteArray, width: Int, height: Int): Int {
        val texture = renderer.texture()
        G.glTexImage2D(G.GL_TEXTURE_2D,0,G.GL_RGBA8,width,height,0,G.GL_RGBA,G.GL_UNSIGNED_BYTE,
            ByteBuffer.allocateDirect(data.size).put(data).apply { rewind() })
        BeautyGlRenderer.checkGl()
        return texture
    }

    private fun read(width: Int, height: Int): ByteArray {
        val buffer = ByteBuffer.allocateDirect(width*height*4)
        G.glReadPixels(0,0,width,height,G.GL_RGBA,G.GL_UNSIGNED_BYTE,buffer)
        BeautyGlRenderer.checkGl()
        return ByteArray(buffer.capacity()).also { buffer.rewind(); buffer.get(it) }
    }

    @Test fun offMissingMaskAndExpiredSnapshotArePixelIdentical() {
        BeautyGlRenderer().use { renderer ->
            val input = pixels(); val texture = upload(renderer,input,size,size); val output=renderer.pbuffer(size,size)
            try {
                listOf(Triple(BeautyPreset.OFF,mask,1f),Triple(BeautyPreset.SOFT,null,1f),
                    Triple(BeautyPreset.SOFT,mask,0f)).forEach { (preset,region,freshness) ->
                    renderer.render(texture,false,BeautyGlRenderer.identity(),output,size,size,
                        region,BeautyTransform(),preset,freshness)
                    assertArrayEquals(input,read(size,size))
                }
            } finally { renderer.destroy(output); renderer.deleteTexture(texture) }
        }
    }

    @Test fun gpuChangesOnlyUnprotectedMaskAndKeepsChangesBounded() {
        BeautyGlRenderer().use { renderer ->
            val input=pixels(); val texture=upload(renderer,input,size,size); val output=renderer.pbuffer(size,size)
            try {
                renderer.render(texture,false,BeautyGlRenderer.identity(),output,size,size,mask,
                    BeautyTransform(),BeautyPreset.SOFT,1f)
                val result=read(size,size)
                var changed=0
                for(y in 0 until size) for(x in 0 until size) for(c in 0..2) {
                    val i=(y*size+x)*4+c
                    val delta=abs((result[i].toInt() and 255)-(input[i].toInt() and 255))
                    if(mask.weight(x+.5f,size-y-.5f)==0f) assertEquals("protected pixel $x,$y",0,delta)
                    if(delta>0) changed++
                    assertTrue("bounded blend",delta<=7)
                }
                assertTrue("the shader must actually smooth",changed>100)
            } finally { renderer.destroy(output); renderer.deleteTexture(texture) }
        }
    }

    @Test fun gpuPreservesConstantColorsAcrossResizeAndCloseIsIdempotent() {
        val renderer=BeautyGlRenderer()
        try {
            listOf(64 to 48,96 to 64,48 to 64).forEach { (w,h) ->
                val input=ByteArray(w*h*4) { if(it%4==3) 255.toByte() else 127 }
                val texture=upload(renderer,input,w,h); val output=renderer.pbuffer(w,h)
                try {
                    renderer.render(texture,false,BeautyGlRenderer.identity(),output,w,h,mask,
                        BeautyTransform(),BeautyPreset.SOFT,1f)
                    val result=read(w,h)
                    assertTrue(input.indices.all { abs((input[it].toInt() and 255)-(result[it].toInt() and 255))<=1 })
                } finally { renderer.destroy(output); renderer.deleteTexture(texture) }
            }
        } finally { renderer.close(); renderer.close() }
    }

    @Test fun oesCameraInputPathSupportsPassthroughAndBeauty() {
        BeautyGlRenderer().use { renderer ->
            val texture=renderer.texture(external=true)
            val input=SurfaceTexture(texture).apply { setDefaultBufferSize(size,size) }
            val surface=Surface(input); val output=renderer.pbuffer(size,size)
            val received=CountDownLatch(1)
            input.setOnFrameAvailableListener({ received.countDown() },Handler(Looper.getMainLooper()))
            try {
                val canvas=surface.lockCanvas(null)
                canvas.drawColor(Color.rgb(80,130,180))
                surface.unlockCanvasAndPost(canvas)
                assertTrue("OES frame delivered",received.await(5,TimeUnit.SECONDS))
                input.updateTexImage()
                val transform=FloatArray(16); input.getTransformMatrix(transform)
                for(preset in listOf(BeautyPreset.OFF,BeautyPreset.SOFT)) {
                    renderer.render(texture,true,transform,output,size,size,mask,BeautyTransform(),preset,1f)
                    val result=read(size,size)
                    for(c in 0..2) assertEquals(listOf(80,130,180)[c].toDouble(),
                        (result[(size*size/2)*4+c].toInt() and 255).toDouble(),1.0)
                }
            } finally {
                input.setOnFrameAvailableListener(null); surface.release(); input.release()
                renderer.destroy(output); renderer.deleteTexture(texture)
            }
        }
    }

    @Test fun cpuBlendPreservesProtectedPixelsAndOffIsIdentity() {
        val bitmap=Bitmap.createBitmap(size,size,Bitmap.Config.ARGB_8888)
        val input=IntArray(size*size) { val v=100+(it%size+it/size)%2*50; Color.rgb(v,v,v) }
        try {
            bitmap.setPixels(input,0,size,0,0,size,size)
            BeautyStillProcessor.applyMask(bitmap,mask,BeautyPreset.OFF)
            val off=IntArray(input.size); bitmap.getPixels(off,0,size,0,0,size,size)
            assertArrayEquals(input,off)
            BeautyStillProcessor.applyMask(bitmap,mask,BeautyPreset.SOFT)
            val result=IntArray(input.size); bitmap.getPixels(result,0,size,0,0,size,size)
            var changed=0
            for(y in 0 until size) for(x in 0 until size) {
                val i=y*size+x
                if(mask.weight(x+.5f,y+.5f)==0f) assertEquals(input[i],result[i])
                if(input[i]!=result[i]) changed++
            }
            assertTrue(changed>100)
        } finally { bitmap.recycle() }
    }

    @Test fun noFaceStillReturnsVisibleWarningWithoutChangingBitmap() {
        val bitmap=Bitmap.createBitmap(192,192,Bitmap.Config.ARGB_8888)
        try {
            bitmap.eraseColor(Color.GRAY)
            val warning=BeautyStillProcessor.apply(bitmap,0,BeautyPreset.NATURAL)
            assertTrue(warning!!.contains("未应用美颜"))
            assertEquals(Color.GRAY,bitmap.getPixel(96,96))
        } finally { bitmap.recycle() }
    }
}
