package com.photocoach.app.camera

import android.opengl.GLES30 as G
import com.photocoach.app.beauty.BeautyGlRenderer
import com.photocoach.app.beauty.BeautyPreset
import java.nio.ByteBuffer
import org.junit.Assert.*
import org.junit.Test

class MotionEglOwnershipTest {
    @Test fun closingEitherRendererDoesNotTerminateTheOtherOwnersDisplay() {
        for(closeMotionFirst in listOf(true,false)) {
            val beauty=BeautyGlRenderer();val motion=MotionGlRenderer()
            val beautySurface=beauty.pbuffer(2,2);val motionSurface=motion.pbuffer(2,2)
            var beautyClosed=false;var motionClosed=false
            fun drawBeauty() {
                beauty.makeCurrent(beautySurface)
                val texture=beauty.texture()
                try {
                    val pixels=ByteBuffer.allocateDirect(16).apply {repeat(4){put(255.toByte());put(0);put(0);put(255.toByte())};rewind()}
                    G.glBindTexture(G.GL_TEXTURE_2D,texture)
                    G.glTexImage2D(G.GL_TEXTURE_2D,0,G.GL_RGBA,2,2,0,G.GL_RGBA,G.GL_UNSIGNED_BYTE,pixels)
                    beauty.render(texture,false,BeautyGlRenderer.identity(),beautySurface,2,2,null,null,BeautyPreset.OFF,0f)
                    pixels.clear();G.glReadPixels(0,0,2,2,G.GL_RGBA,G.GL_UNSIGNED_BYTE,pixels)
                    assertEquals(255,pixels.get(0).toInt() and 255);assertEquals(G.GL_NO_ERROR,G.glGetError())
                } finally {beauty.deleteTexture(texture)}
            }
            fun drawMotion() {
                motion.current(motionSurface)
                val texture=motion.texture(false)
                try {
                    val pixels=ByteBuffer.allocateDirect(16).apply {repeat(4){put(0);put(255.toByte());put(0);put(255.toByte())};rewind()}
                    motion.upload(texture,2,2,pixels)
                    motion.draw(texture,false,MotionGlRenderer.identity(),motionSurface,2,2)
                    motion.read(2,2,pixels)
                    assertEquals(255,pixels.get(1).toInt() and 255)
                } finally {motion.deleteTexture(texture)}
            }
            try {
                drawBeauty();drawMotion()
                if(closeMotionFirst) {
                    motion.destroy(motionSurface);motion.close();motionClosed=true;drawBeauty()
                } else {
                    beauty.destroy(beautySurface);beauty.close();beautyClosed=true;drawMotion()
                }
            } finally {
                if(!beautyClosed){beauty.destroy(beautySurface);beauty.close()}
                if(!motionClosed){motion.destroy(motionSurface);motion.close()}
            }
        }
    }
}
