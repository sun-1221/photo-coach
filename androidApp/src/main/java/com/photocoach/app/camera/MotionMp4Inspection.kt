package com.photocoach.app.camera

import android.media.MediaExtractor
import android.media.MediaFormat
import java.io.File
import java.io.RandomAccessFile

internal data class MotionEdit(val duration:Long,val mediaTime:Long,val rateInteger:Int,val rateFraction:Int)
internal data class MotionTimeTables(val timescale:Long,val durations:List<Long>,val compositionOffsets:List<Long>,val edits:List<MotionEdit>)
internal data class InspectedMotionMp4(val tables:MotionTimeTables,val samples:List<MuxedVideoSample>) {
    val trackTimescale:Long get()=tables.timescale
}

/** Read actual container timing; no Recorder event or declared frame rate supplies an anchor. */
internal object MotionMp4Inspection {
    fun read(file:File):InspectedMotionMp4 {
        require(file.length() in 1..MotionTemporaryPolicy.MAX_RECORDING_BYTES)
        val tables=readVideoTimescale(file)
        val extractor=MediaExtractor()
        try {
            extractor.setDataSource(file.absolutePath)
            require(extractor.trackCount==1){"Live must have exactly one silent video track"}
            require(extractor.getTrackFormat(0).getString(MediaFormat.KEY_MIME)?.startsWith("video/")==true)
            extractor.selectTrack(0)
            val legacyBuffer=if(android.os.Build.VERSION.SDK_INT<28)java.nio.ByteBuffer.allocateDirect(file.length().toInt()) else null
            val samples=mutableListOf<MuxedVideoSample>()
            while(extractor.sampleTime>=0) {
                require(samples.size<MotionFrameTimeline.MAX_FRAMES)
                val sampleSize=if(android.os.Build.VERSION.SDK_INT>=28)extractor.sampleSize else {
                    requireNotNull(legacyBuffer).clear();extractor.readSampleData(legacyBuffer,0).toLong()
                }
                require(sampleSize>0 && extractor.sampleTrackIndex==0)
                samples+=MuxedVideoSample(extractor.sampleTime,extractor.sampleFlags and MediaExtractor.SAMPLE_FLAG_SYNC!=0)
                if(!extractor.advance())break
            }
            require(samples.size>=2)
            require(tables.durations.size==samples.size && tables.compositionOffsets.size==samples.size)
            return InspectedMotionMp4(tables,samples)
        } finally {extractor.release()}
    }
    private data class Box(val type:String,val body:Long,val end:Long)
    private fun readVideoTimescale(file:File):MotionTimeTables = RandomAccessFile(file,"r").use {input ->
        var remainingBoxes=256
        fun boxes(start:Long,end:Long):List<Box> {
            val boxes=mutableListOf<Box>();var offset=start
            while(offset<end) {
                require(--remainingBoxes>=0 && end-offset>=8)
                input.seek(offset)
                val shortSize=input.readInt().toLong() and 0xffffffffL
                val type=ByteArray(4).also(input::readFully).toString(Charsets.US_ASCII)
                val header=if(shortSize==1L)16L else 8L
                val size=when(shortSize){0L->end-offset;1L->input.readLong();else->shortSize}
                require(size>=header && size<=end-offset)
                boxes+=Box(type,offset+header,offset+size);offset+=size
            }
            return boxes
        }
        val moov=boxes(0,input.length()).single {it.type=="moov"}
        val track=boxes(moov.body,moov.end).single {it.type=="trak"}
        val trackChildren=boxes(track.body,track.end)
        val media=trackChildren.single {it.type=="mdia"}
        val children=boxes(media.body,media.end)
        val handler=children.single {it.type=="hdlr"};require(handler.end-handler.body>=12)
        input.seek(handler.body+8)
        require(ByteArray(4).also(input::readFully).toString(Charsets.US_ASCII)=="vide")
        val header=children.single {it.type=="mdhd"}
        input.seek(header.body);val version=input.readUnsignedByte();require(version==0 || version==1)
        val position=header.body+if(version==1)20 else 12
        require(position+4<=header.end);input.seek(position)
        val timescale=(input.readInt().toLong() and 0xffffffffL).also {require(it in 1L..Int.MAX_VALUE.toLong())}
        val info=children.single {it.type=="minf"}
        val sampleTable=boxes(info.body,info.end).single {it.type=="stbl"}
        val sampleChildren=boxes(sampleTable.body,sampleTable.end)
        fun expand(box:Box,signed:Boolean):List<Long> {
            require(box.end-box.body>=8);input.seek(box.body)
            val flags=input.readInt();val tableVersion=flags ushr 24
            require(if(signed)tableVersion in 0..1 else tableVersion==0)
            val count=input.readInt();require(count in 1..MotionFrameTimeline.MAX_FRAMES && box.end-box.body==8+count*8L)
            val values=mutableListOf<Long>()
            repeat(count) {
                val samples=input.readInt();val raw=input.readInt()
                require(samples>0 && samples<=MotionFrameTimeline.MAX_FRAMES-values.size)
                val value=if(signed && tableVersion==1)raw.toLong() else raw.toLong() and 0xffffffffL
                repeat(samples){values+=value}
            }
            return values
        }
        val durations=expand(sampleChildren.single {it.type=="stts"},false)
        val offsets=sampleChildren.singleOrNull {it.type=="ctts"}?.let {expand(it,true)} ?: List(durations.size){0L}
        val edits=trackChildren.singleOrNull {it.type=="edts"}?.let {edts ->
            val edit=boxes(edts.body,edts.end).single {it.type=="elst"}
            require(edit.end-edit.body>=8);input.seek(edit.body)
            val editVersion=input.readInt() ushr 24;require(editVersion in 0..1)
            val count=input.readInt();require(count in 0..2)
            require(edit.end-edit.body==8+count*(if(editVersion==1)20L else 12L))
            List(count) {
                val duration=if(editVersion==1)input.readLong() else input.readInt().toLong() and 0xffffffffL
                val mediaTime=if(editVersion==1)input.readLong() else input.readInt().toLong()
                MotionEdit(duration,mediaTime,input.readShort().toInt(),input.readShort().toInt())
            }
        } ?: emptyList()
        MotionTimeTables(timescale,durations,offsets,edits)
    }
}
