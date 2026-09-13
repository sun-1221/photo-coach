package com.photocoach.app.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import androidx.camera.core.ImageInfo
import androidx.camera.core.ImageProxy
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.nio.ByteBuffer
import org.junit.Assert.*
import org.junit.Test

class CameraJpegCaptureTest {
    @Test fun timestampIsDeliveredBeforeDiskPreparationAndCallbackFailureClosesProxy()=fixture {directory,jpeg ->
        val image=Proxy(jpeg,42L);var timestampDelivered=false
        prepareCapturedJpeg(image,File(directory,"early.jpg"),95,onTimestamp={
            assertEquals(42L,it);assertFalse(File(directory,"early.jpg.capture-raw").exists());timestampDelivered=true
        }) {assertTrue(timestampDelivered)}
        assertEquals(1,image.closed)
        val failed=Proxy(jpeg,43L)
        assertTrue(runCatching {prepareCapturedJpeg(failed,File(directory,"rejected.jpg"),95,
            onTimestamp={throw java.util.concurrent.RejectedExecutionException("injected callback rejection")}) {error("must not prepare")}}.isFailure)
        assertEquals(1,failed.closed)
        assertFalse(File(directory,"rejected.jpg.capture-raw").exists())
    }
    @Test fun sameProxyBytesRemainPublishableWhenSensorTimestampIsInvalid()=fixture {directory,jpeg ->
        for(time in listOf(0L,-1L,42_000_000_000L)) {
            val image=Proxy(jpeg,time);val output=File(directory,"$time.jpg")
            val stages=mutableListOf<JpegPreparation?>()
            val actual=prepareCapturedJpeg(image,output,95,persistPreparation=stages::add)
            assertEquals(time.takeIf {it>0},actual);assertEquals(1,image.closed)
            assertEquals(null,stages.last());assertEquals(true,stages[1]!!.motionCompatible)
            assertArrayEquals(jpeg,File(stages.first()!!.rawPath).readBytes())
            val decoded=BitmapFactory.decodeFile(output.path)!!
            try {assertEquals(16,decoded.width);assertEquals(9,decoded.height)} finally {decoded.recycle()}
        }
    }
    @Test fun rawXmpEligibilitySurvivesCroppingAndJournalClearFailureKeepsRawRetryable()=fixture {directory,jpeg ->
        val payload=("http://ns.adobe.com/xap/1.0/\u0000<x:xmpmeta xmlns:x=\"adobe:ns:meta/\">ordinary metadata</x:xmpmeta>").toByteArray()
        val length=payload.size+2
        val source=jpeg.take(2).toByteArray()+byteArrayOf(0xff.toByte(),0xe1.toByte(),(length shr 8).toByte(),length.toByte())+payload+jpeg.drop(2).toByteArray()
        val image=Proxy(source,42_000_000_000L);val output=File(directory,"crop.jpg")
        var durable:JpegPreparation?=null
        assertTrue(runCatching {prepareCapturedJpeg(image,output,95){stage ->
            if(stage==null)error("injected journal clear interruption")
            durable=stage
        }}.isFailure)
        assertEquals(1,image.closed);assertEquals(false,durable!!.motionCompatible)
        val raw=File(durable!!.rawPath)
        assertArrayEquals(source,raw.readBytes());assertTrue(output.length()>0)
        durable!!.prepare(output)
        assertEquals(false,durable!!.motionCompatible);assertArrayEquals(source,raw.readBytes())
    }
    @Test fun emptyPlaneFailureStillClosesProxyAndKeepsPreparationIdentity()=fixture {directory,_ ->
        val image=Proxy(byteArrayOf(),1);var durable:JpegPreparation?=null
        assertTrue(runCatching {prepareCapturedJpeg(image,File(directory,"empty.jpg"),95){durable=it}}.isFailure)
        assertEquals(1,image.closed);assertNotNull(durable)
    }
    private class Proxy(val bytes:ByteArray,val timestamp:Long):ImageProxy {
        var closed=0;private var crop=Rect(2,2,18,11)
        override fun close(){closed++}
        override fun getCropRect()=crop
        override fun setCropRect(value:Rect?){crop=value ?: Rect(0,0,20,16)}
        override fun getFormat()=android.graphics.ImageFormat.JPEG
        override fun getHeight()=16
        override fun getWidth()=20
        override fun getPlanes()=arrayOf(object:ImageProxy.PlaneProxy {
            override fun getRowStride()=bytes.size
            override fun getPixelStride()=1
            override fun getBuffer()=ByteBuffer.wrap(bytes)
        })
        override fun getImageInfo():ImageInfo=object:ImageInfo {
            override fun getTagBundle()=androidx.camera.core.impl.TagBundle.emptyBundle()
            override fun getTimestamp():Long=this@Proxy.timestamp
            override fun getRotationDegrees()=90
            override fun populateExifData(builder:androidx.camera.core.impl.utils.ExifData.Builder) { }
        }
        override fun getImage():android.media.Image?=null
    }
    private fun fixture(test:(File,ByteArray)->Unit) {
        val directory=File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir,"jpeg-proxy-${java.util.UUID.randomUUID()}").apply {mkdirs()}
        try {
            val image=Bitmap.createBitmap(20,16,Bitmap.Config.ARGB_8888)
            val bytes=try {java.io.ByteArrayOutputStream().use {image.compress(Bitmap.CompressFormat.JPEG,100,it);it.toByteArray()}} finally {image.recycle()}
            test(directory,bytes)
        } finally {directory.deleteRecursively()}
    }
}
