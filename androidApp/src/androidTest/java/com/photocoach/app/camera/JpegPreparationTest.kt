package com.photocoach.app.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.exifinterface.media.ExifInterface
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Assert.*
import org.junit.Test

class JpegPreparationTest {
    @Test fun uncroppedJpegDoesNotConsumeBitmapDecodeBudget() = fixture {raw,output ->
        JpegPreparation(raw.path,200,160,0,0,200,160,0,95).prepare(output,maxPixels=0)
        assertTrue(output.length()>0)
    }
    @Test fun memoryAdmissionRejectsBeforeDecodeWithoutDownsamplingOrLosingRaw() = fixture {raw,output ->
        val original=raw.readBytes()
        val preparation=JpegPreparation(raw.path,200,160,20,20,180,110,0,95)
        assertTrue(runCatching {preparation.prepare(output,maxPixels=100)}.isFailure)
        assertArrayEquals(original,raw.readBytes());assertFalse(output.exists())
        preparation.prepare(output,maxPixels=20_000)
        val bitmap=BitmapFactory.decodeFile(output.path)!!
        try {assertEquals(160,bitmap.width);assertEquals(90,bitmap.height)} finally {bitmap.recycle()}
    }
    @Test fun crashTemporaryHasDeterministicTransactionOwnershipAndIsReplacedOnReplay() = fixture {raw,output ->
        val temporary=JpegPreparation.temporaryFor(output)
        temporary.writeBytes(byteArrayOf(1,2,3))
        JpegPreparation(raw.path,200,160,20,20,180,110,0,95).prepare(output)
        assertFalse(temporary.exists());assertNotNull(BitmapFactory.decodeFile(output.path)?.also {it.recycle()})
    }
    @Test fun twoCroppedAspectRatiosAndFourRotationsKeepRawBytesAndExpectedPixels() = fixture {raw,output ->
        val original=raw.readBytes()
        for(height in listOf(120,90))for(rotation in listOf(0,90,180,270)) {
            JpegPreparation(raw.path,200,160,20,20,180,20+height,rotation,95).prepare(output)
            val result=BitmapFactory.decodeFile(output.path)!!
            try {assertEquals(160,result.width);assertEquals(height,result.height)
                val middle=result.getPixel(80,height/2)
                assertTrue(Color.green(middle)>200);assertTrue(Color.red(middle)<40)
            } finally {result.recycle()}
            assertEquals(when(rotation){90->6;180->3;270->8;else->1},
                ExifInterface(output).getAttributeInt(ExifInterface.TAG_ORIENTATION,0))
            assertArrayEquals(original,raw.readBytes())
        }
    }
    @Test fun exifFailureLeavesRawAndPreviousOutputUntouchedAndSamePreparationCanRetry() = fixture {raw,output ->
        val original=raw.readBytes();output.writeBytes(byteArrayOf(11,12,13))
        val preparation=JpegPreparation(raw.path,200,160,20,20,180,110,90,95)
        assertTrue(runCatching {preparation.prepare(output){error("injected Exif failure")}}.isFailure)
        assertArrayEquals(original,raw.readBytes());assertArrayEquals(byteArrayOf(11,12,13),output.readBytes())
        preparation.prepare(output)
        val first=output.readBytes();preparation.prepare(output)
        assertArrayEquals(first,output.readBytes());assertArrayEquals(original,raw.readBytes())
        assertEquals(2,raw.parentFile!!.listFiles()!!.size)
    }
    private fun fixture(test:(File,File)->Unit) {
        val directory=File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir,"jpeg-prepare-test-${java.util.UUID.randomUUID()}").apply {mkdirs()}
        val raw=File(directory,"raw.jpg");val output=File(directory,"prepared.jpg")
        try {
            val image=Bitmap.createBitmap(200,160,Bitmap.Config.ARGB_8888)
            try {image.eraseColor(Color.RED)
                android.graphics.Canvas(image).drawRect(20f,20f,180f,140f,android.graphics.Paint().apply {color=Color.GREEN})
                raw.outputStream().use {assertTrue(image.compress(Bitmap.CompressFormat.JPEG,100,it))}
            } finally {image.recycle()}
            test(raw,output)
        } finally {directory.deleteRecursively()}
    }
}
