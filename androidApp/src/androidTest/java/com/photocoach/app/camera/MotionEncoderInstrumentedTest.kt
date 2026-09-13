package com.photocoach.app.camera

import android.media.MediaMetadataRetriever
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.Assert.*
import org.junit.Test

/** Actual production encoder and EGL surface. Sensor observations here are explicitly synthetic. */
class MotionEncoderInstrumentedTest {
    @Test(timeout=30000) fun eglSurfaceEncoderRetainsMeasuredVfrPtsAndDecodesAfterFirstInputDrop() {
        val app=InstrumentationRegistry.getInstrumentation().targetContext
        val file=File.createTempFile("own-encoder-test-",".mp4",app.cacheDir)
        val source=MotionSensorSource("synthetic",1)
        val timeline=MotionFrameTimeline(1,source)
        val done=CountDownLatch(1)
        var outcome:Result<FinalizedMotionVideo>?=null
        val encoder=MotionSilentEncoder(64,64,file,timeline){outcome=it;done.countDown()}
        val gl=MotionGlRenderer()
        var window:android.opengl.EGLSurface?=null
        var texture=0
        try {
            window=gl.window(encoder.surface);gl.current(window)
            texture=gl.texture(false)
            val pixels=ByteBuffer.allocateDirect(64*64*4).apply {repeat(64*64){put(20);put(190.toByte());put(90);put(255.toByte())};rewind()}
            gl.upload(texture,64,64,pixels)
            repeat(120) {index ->
                val sensor=80_000_000_000L+(index*33_333L+(index%3)*500L)*1000
                timeline.sensorResult(source,sensor,index.toLong())
                val pts=timeline.submit(sensor)!!
                if(index>0) { // Submitted ledger input zero deliberately never reaches the encoder.
                    gl.draw(texture,false,MotionGlRenderer.identity(),window,64,64)
                    gl.present(window,pts*1000)
                }
            }
        } finally {
            if(texture!=0)gl.deleteTexture(texture)
            window?.let(gl::destroy);gl.close();encoder.finishInput()
        }
        try {
            assertTrue("own encoder must finalize within its resource deadline",done.await(15,TimeUnit.SECONDS))
            val video=requireNotNull(outcome).getOrThrow()
            assertEquals(source,video.timeline.source)
            assertTrue(video.timeline.frames.size>30)
            assertTrue(video.timeline.frames.first().submittedPtsUs>0)
            assertEquals(0L,video.timeline.frames.first().muxPtsUs)
            assertNotNull(video.timeline.fullWindow(source,82_000_000_000L))
            val reader=MediaMetadataRetriever()
            try {
                reader.setDataSource(file.absolutePath)
                val bitmap=reader.getFrameAtTime(1_500_000,MediaMetadataRetriever.OPTION_CLOSEST)
                assertNotNull(bitmap);assertEquals(64,bitmap!!.width);bitmap.recycle()
            } finally {reader.release()}
        } finally {
            encoder.inspection?.let {actual ->
                val written=timeline.writtenSamples()
                val directory=File(app.getExternalFilesDir(null),"acceptance-evidence").apply {mkdirs()}
                File(directory,"own-encoder-timing.txt").writeText(buildString {
                    appendLine("synthetic sensor; production EGL/AVC/Muxer; timescale=${actual.trackTimescale}; written=${written.size}; actual=${actual.samples.size}; failure=${timeline.failure}")
                    appendLine("stts=${actual.tables.durations}")
                    appendLine("ctts=${actual.tables.compositionOffsets}")
                    appendLine("elst=${actual.tables.edits}")
                    appendLine("index,sensorNs,submittedPtsUs,writtenPtsUs,actualMuxPtsUs,keyFrame")
                    written.forEachIndexed {index,sample ->appendLine("$index,${sample.sensorNs},${sample.ptsUs},${sample.ptsUs-written.first().ptsUs},${actual.samples.getOrNull(index)?.ptsUs},${sample.keyFrame}")}
                })
            }
            file.delete()
        }
    }
}
