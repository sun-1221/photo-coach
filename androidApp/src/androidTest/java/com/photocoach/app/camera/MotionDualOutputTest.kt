package com.photocoach.app.camera

import android.media.MediaMetadataRetriever
import android.os.SystemClock
import android.util.Size
import android.view.Surface
import androidx.camera.core.CameraEffect
import androidx.camera.core.SurfaceOutput
import androidx.core.util.Consumer
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import org.junit.Assert.*
import org.junit.Test

/** Synthetic camera producer, real SurfaceTexture/three-owner fanout/two surface encoders and MP4s. */
class MotionDualOutputTest {
    @Test fun realSurfaceTextureFansOutFrameIdentityAndMirrorToTwoPlayableEncoders() {
        for(rotation in listOf(0,90,180,270))runPipeline(false,rotation)
        runPipeline(true,0)
    }
    private fun runPipeline(mirror:Boolean,rotation:Int) {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val directory=File(context.cacheDir,"dual-motion-${java.util.UUID.randomUUID()}").apply {mkdirs()}
        val source=MotionSensorSource("synthetic-camera",1)
        val ownLedger=MotionFrameTimeline(1,source)
        val sinkLedger=MotionFrameTimeline(2,source).apply {submit(1L)}
        val ownResult=AtomicReference<Result<FinalizedMotionVideo>>()
        val sinkResult=AtomicReference<Result<FinalizedMotionVideo>>()
        val ownDone=CountDownLatch(1);val sinkDone=CountDownLatch(1)
        val failures=java.util.concurrent.ConcurrentLinkedQueue<Throwable>()
        val processor=MotionSurfaceProcessor {failures.add(it)}
        val recorder=MotionSilentEncoder(64,64,File(directory,"recorder.mp4"),sinkLedger) {sinkResult.set(it);sinkDone.countDown()}
        val inputReady=CountDownLatch(1);val outputClosed=CountDownLatch(1)
        val inputSurface=AtomicReference<Surface>()
        val releaseInput=AtomicReference<()->Unit>()
        val input=object:MotionSurfaceInput {
            override val resolution=Size(64,64)
            override fun decline(){failures.add(IllegalStateException("input declined"));inputReady.countDown()}
            override fun provide(surface:Surface,executor:Executor,returned:()->Unit) {
                inputSurface.set(surface);releaseInput.set {executor.execute(returned)};inputReady.countDown()
            }
            override fun invalidate(){releaseInput.get()?.invoke()}
        }
        val output=object:SurfaceOutput {
            override fun getTargets()=CameraEffect.VIDEO_CAPTURE
            override fun getSize()=Size(64,64)
            override fun getSurface(executor:Executor,listener:Consumer<SurfaceOutput.Event>)=recorder.surface
            override fun close(){outputClosed.countDown()}
            override fun updateTransformMatrix(destination:FloatArray,original:FloatArray) {
                val transform=MotionGlRenderer.identity()
                android.opengl.Matrix.translateM(transform,0,.5f,.5f,0f)
                android.opengl.Matrix.rotateM(transform,0,rotation.toFloat(),0f,0f,1f)
                android.opengl.Matrix.scaleM(transform,0,if(mirror)-1f else 1f,1f,1f)
                android.opengl.Matrix.translateM(transform,0,-.5f,-.5f,0f)
                android.opengl.Matrix.multiplyMM(destination,0,original,0,transform,0)
            }
        }
        var producer:MotionGlRenderer?=null;var window:android.opengl.EGLSurface?=null;var texture=0
        try {
            processor.executor.execute {processor.onOutputSurface(output);processor.acceptInput(input)}
            assertTrue(inputReady.await(3,TimeUnit.SECONDS));assertNotNull(inputSurface.get())
            processor.start(File(directory,"own.mp4"),ownLedger){ownResult.set(it);ownDone.countDown()}
            producer=MotionGlRenderer();window=producer.window(inputSurface.get());texture=producer.texture(false)
            for(index in 0 until 100) {
                val sensorNs=1_000_001L+index*33_333_000L
                processor.observe(source,sensorNs,index.toLong())
                sinkLedger.sensorResult(source,sensorNs,index.toLong());sinkLedger.submit(sensorNs)
                val pixels=ByteBuffer.allocateDirect(64*64*4)
                repeat(64){repeat(64){x ->
                    val value=if(index and (1 shl (x/8))!=0)255 else 0
                    pixels.put(value.toByte()).put(value.toByte()).put(value.toByte()).put(255.toByte())
                }}
                pixels.rewind();producer.current();producer.upload(texture,64,64,pixels)
                producer.draw(texture,false,MotionGlRenderer.identity(),window,64,64)
                producer.present(window,sensorNs);SystemClock.sleep(30)
            }
            producer.destroy(window);window=null;producer.deleteTexture(texture);texture=0;producer.close();producer=null
            processor.finish()
            assertTrue("own encoder did not complete: ${processor.diagnostic()}",ownDone.await(10,TimeUnit.SECONDS))
            val own=ownResult.get().getOrThrow()
            assertTrue(own.timeline.frames.size>30)
            verifyFrames(own,mirror,rotation)
            processor.close()
            assertTrue(outputClosed.await(3,TimeUnit.SECONDS));recorder.finishInput()
            assertTrue(sinkDone.await(10,TimeUnit.SECONDS))
            val recorded=sinkResult.get().getOrThrow()
            assertTrue(recorded.timeline.frames.size>30);verifyFrames(recorded,mirror,rotation)
            assertTrue(failures.toString(),failures.isEmpty())
            File(context.getExternalFilesDir(null),"acceptance-evidence").apply {mkdirs()}.let {evidence ->
                File(evidence,"dual-output-frames.txt").appendText("syntheticSurfaceProducer=true; rotation=$rotation; mirror=$mirror; ownFrames=${own.timeline.frames.size}; recorderFrames=${recorded.timeline.frames.size}; firstMiddleLastIdentity=true; ${processor.diagnostic()}\n")
            }
        } finally {
            producer?.let {gl ->window?.let {gl.destroy(it)};if(texture!=0)gl.deleteTexture(texture);gl.close()}
            processor.close()
            if(outputClosed.await(3,TimeUnit.SECONDS))recorder.finishInput()
            assertTrue(processor.ownerTerminations.all {it.await(3,TimeUnit.SECONDS)})
            assertTrue(sinkDone.await(10,TimeUnit.SECONDS))
            directory.deleteRecursively()
        }
    }
    private fun verifyFrames(video:FinalizedMotionVideo,mirror:Boolean,rotation:Int) {
        val retriever=MediaMetadataRetriever()
        try {
            retriever.setDataSource(video.file.path)
            for(frame in listOf(video.timeline.frames.first(),video.timeline.frames[video.timeline.frames.size/2],video.timeline.frames.last())) {
                val expected=((frame.sensorNs-1_000_001L)/33_333_000L).toInt()
                val bitmap=requireNotNull(retriever.getFrameAtTime(frame.muxPtsUs,MediaMetadataRetriever.OPTION_CLOSEST))
                try {
                    assertEquals(64,bitmap.width);assertEquals(64,bitmap.height)
                    var decoded=0
                    for(bit in 0..7) {
                        val position=bit*8+4
                        val x=when(rotation){0->if(mirror)63-position else position;180->63-position;else->32}
                        val y=when(rotation){90->position;270->63-position;else->32}
                        if(android.graphics.Color.red(bitmap.getPixel(x,y))>128)decoded=decoded or (1 shl bit)
                    }
                    assertEquals("mux=${frame.muxPtsUs}, sensor=${frame.sensorNs}, mirror=$mirror, rotation=$rotation",expected,decoded)
                } finally {bitmap.recycle()}
            }
        } finally {retriever.release()}
    }
}
