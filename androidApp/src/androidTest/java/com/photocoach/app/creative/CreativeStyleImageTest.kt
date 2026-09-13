package com.photocoach.app.creative

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Assert.*
import org.junit.Test

class CreativeStyleImageTest {
    @Test fun allTwelveStylesProcessRealSyntheticJpegAndPreserveSourceBytes() {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val directory=File(context.cacheDir,"style-image-${java.util.UUID.randomUUID()}").apply {mkdirs()}
        try {
            val source=File(directory,"source.jpg")
            val bitmap=Bitmap.createBitmap(96,64,Bitmap.Config.ARGB_8888)
            for(y in 0 until 64)for(x in 0 until 96)bitmap.setPixel(x,y,Color.rgb(x*255/95,y*255/63,(x+y)*255/158))
            try {source.outputStream().use {assertTrue(bitmap.compress(Bitmap.CompressFormat.JPEG,95,it))}} finally {bitmap.recycle()}
            val original=source.readBytes();val processor=CreativeImageProcessor(File(directory,"outputs"))
            val report=mutableListOf<String>()
            assertEquals(12,CreativeStyle.entries.size)
            CreativeStyle.entries.forEach {style ->
                val result=processor.process(source,style)
                val decoded=BitmapFactory.decodeFile(result.file.absolutePath)
                assertNotNull(style.name,decoded)
                try {assertEquals(96,decoded!!.width);assertEquals(64,decoded.height)
                    report+="${style.name}:96x64;decode=true;sourceUnchanged=true"
                } finally {decoded?.recycle()}
                assertArrayEquals(original,source.readBytes())
            }
            val evidence=File(context.getExternalFilesDir(null),"acceptance-evidence").apply {mkdirs()}
            File(evidence,"synthetic-twelve-styles.txt").writeText(report.joinToString("\n"))
        } finally {directory.deleteRecursively()}
    }
}
