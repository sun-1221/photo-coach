package com.photocoach.app.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Real synthetic AVC samples. This does not exercise Recorder/sensor timestamp origin or HyperOS playback. */
@RunWith(AndroidJUnit4::class)
class PlayableMotionPhotoTest {
    @Test fun encodedVideoClipsAtRealSyncSampleAndProducesDecodableSilentMotionContainer() {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val directory=File(context.cacheDir,"codec-test-${java.util.UUID.randomUUID()}").apply {mkdirs()}
        try {
            val source=File(directory,"source.mp4");encode(source)
            val inspected=MotionMp4Inspection.read(source)
            assertTrue(inspected.trackTimescale>0)
            assertEquals(90,inspected.samples.size)
            assertEquals(0L,inspected.samples.first().ptsUs)
            val window=MotionClipWindow(550_000,2_550_000,1_000_000)
            val extractor=MediaExtractor()
            val actualStart:Long
            try {extractor.setDataSource(source.path);assertEquals(1,extractor.trackCount);extractor.selectTrack(0)
                extractor.seekTo(window.startUs,MediaExtractor.SEEK_TO_PREVIOUS_SYNC);actualStart=extractor.sampleTime
                assertTrue(actualStart<window.startUs)
            } finally {extractor.release()}
            val result=Mp4Clipper.clip(source,File(directory,"clipped.mp4"),window)
            val invalid=File(directory,"invalid.mp4")
            assertTrue(runCatching {Mp4Clipper.clip(source,invalid,MotionClipWindow(0,10_000_000,9_000_000))}.isFailure)
            assertFalse("out-of-video cover must not leave a successful clip",invalid.exists())
            assertEquals(window.startUs+window.presentationTimestampUs-actualStart,result.presentationTimestampUs)
            assertTrue(result.durationUs>=window.durationUs)
            val clip=MediaExtractor()
            try {clip.setDataSource(result.file.path);assertEquals(1,clip.trackCount)
                assertTrue(clip.getTrackFormat(0).getString(MediaFormat.KEY_MIME)!!.startsWith("video/"))
                clip.selectTrack(0);assertEquals(0L,clip.sampleTime)
                assertTrue(clip.sampleFlags and MediaExtractor.SAMPLE_FLAG_SYNC != 0)
                var samples=0;do {samples++} while(clip.advance());assertTrue(samples>30)
            } finally {clip.release()}
            val retriever=MediaMetadataRetriever()
            try {retriever.setDataSource(result.file.path)
                val frame=retriever.getFrameAtTime(result.presentationTimestampUs,MediaMetadataRetriever.OPTION_CLOSEST)
                assertNotNull("real AVC clip must decode",frame);frame!!.recycle()
            } finally {retriever.release()}
            val jpeg=File(directory,"cover.jpg")
            val bitmap=Bitmap.createBitmap(64,64,Bitmap.Config.ARGB_8888)
            try {bitmap.eraseColor(android.graphics.Color.CYAN);jpeg.outputStream().use {assertTrue(bitmap.compress(Bitmap.CompressFormat.JPEG,95,it))}}
            finally {bitmap.recycle()}
            val original=jpeg.readBytes();val mp4=result.file.readBytes()
            val output=File(directory,"MVIMG_SYNTHETIC_MP.JPG")
            MotionPhotoAssembler.assemble(jpeg,result.file,output,result.presentationTimestampUs)
            val container=output.readBytes()
            assertArrayEquals(original,jpeg.readBytes())
            assertArrayEquals(mp4,container.takeLast(mp4.size).toByteArray())
            fun scan(bytes:ByteArray)=bytes.indices.first {it+1<bytes.size && bytes[it]==0xff.toByte() && bytes[it+1]==0xda.toByte()}
            assertArrayEquals(original.copyOfRange(scan(original),original.size),container.copyOfRange(scan(container),container.size-mp4.size))
            assertTrue(String(container,Charsets.ISO_8859_1).contains("MotionPhotoPresentationTimestampUs=\"${result.presentationTimestampUs}\""))
            val decoded=BitmapFactory.decodeFile(output.path);assertNotNull(decoded);decoded!!.recycle()
            val evidence=File(context.getExternalFilesDir(null),"acceptance-evidence").apply {mkdirs()}
            output.copyTo(File(evidence,output.name),overwrite=true)
            result.file.copyTo(File(evidence,"synthetic-clipped.mp4"),overwrite=true)
            File(evidence,"synthetic-media.txt").writeText("actual source sync start=$actualStart; requested start=${window.startUs}; cover=${result.presentationTimestampUs}; duration=${result.durationUs}; silent AVC decoded; Recorder origin and target playback NotRun")
        } finally {directory.deleteRecursively()}
    }

    internal fun encode(file:File) {
        val codec=MediaCodec.createEncoderByType("video/avc")
        val format=MediaFormat.createVideoFormat("video/avc",64,64).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT,MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
            setInteger(MediaFormat.KEY_BIT_RATE,128_000);setInteger(MediaFormat.KEY_FRAME_RATE,30)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,1)
        }
        val muxer=MediaMuxer(file.path,MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        var started=false
        try {
            codec.configure(format,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);codec.start()
            var sent=0;var done=false;var track=-1
            val info=MediaCodec.BufferInfo();val deadline=android.os.SystemClock.elapsedRealtime()+30_000
            while(!done && android.os.SystemClock.elapsedRealtime()<deadline) {
                if(sent<=90) {
                    val input=codec.dequeueInputBuffer(10_000)
                    if(input>=0) {
                        val buffer=codec.getInputBuffer(input)!!;buffer.clear()
                        if(sent==90) codec.queueInputBuffer(input,0,0,sent*1_000_000L/30,MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        else {buffer.put(ByteArray(64*64){(40+sent).toByte()});buffer.put(ByteArray(64*64/2){128.toByte()})
                            codec.queueInputBuffer(input,0,64*64*3/2,sent*1_000_000L/30,0)}
                        sent++
                    }
                }
                val output=codec.dequeueOutputBuffer(info,10_000)
                if(output==MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){track=muxer.addTrack(codec.outputFormat);muxer.start();started=true}
                else if(output>=0) {
                    if(info.size>0 && info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG==0){check(started);muxer.writeSampleData(track,codec.getOutputBuffer(output)!!,info)}
                    done=info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM!=0;codec.releaseOutputBuffer(output,false)
                }
            }
            assertTrue("encoder did not finish",done)
        } finally {runCatching {codec.stop()};codec.release();if(started)muxer.stop();muxer.release()}
    }
}
