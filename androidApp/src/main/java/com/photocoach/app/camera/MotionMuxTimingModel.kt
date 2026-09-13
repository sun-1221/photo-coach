package com.photocoach.app.camera

/** Integer model of AOSP MPEG4Writer's no-B-frame, zero-origin video stts path.
 * Source: platform/frameworks/av media/libstagefright/MPEG4Writer.cpp, duration tick
 * calculation/coalescing (4154-4211). This is a container rule, not a product tolerance.
 * A platform variation is rejected unless its entire actual table matches this model.
 */
internal object MotionMuxTimingModel {
    fun durationTicks(writtenPtsUs:List<Long>,timescale:Long):List<Long>? {
        if(timescale !in 1L..Int.MAX_VALUE.toLong() || writtenPtsUs.size !in 2..MotionFrameTimeline.MAX_FRAMES || writtenPtsUs.first()!=0L)return null
        val minDecodeStep=maxOf(100L,(1_000_000L+timescale-1)/timescale)
        if(writtenPtsUs.last()>8_000_000 || writtenPtsUs.zipWithNext().any {(a,b)->b-a<minDecodeStep})return null
        fun ticks(timeUs:Long)=(timeUs*timescale+500_000L)/1_000_000L
        var lastTimestamp=0L;var lastDuration=0L
        val durations=mutableListOf<Long>()
        for(index in 1 until writtenPtsUs.size) {
            var timestamp=writtenPtsUs[index]
            var duration=ticks(timestamp)-ticks(lastTimestamp)
            if(duration<=0)return null
            if(lastDuration!=0L && duration!=lastDuration) {
                // Kotlin signed division, like C++, truncates toward zero.
                val deltaUs=((lastDuration-duration)*1_000_000L+timescale/2)/timescale
                if(deltaUs in -99L..99L) {duration=lastDuration;timestamp+=deltaUs}
            }
            durations+=duration;lastDuration=duration;lastTimestamp=timestamp
        }
        durations+=lastDuration // No explicit final duration: MediaMuxer repeats the last delta.
        return durations
    }
    fun matches(writtenPtsUs:List<Long>,actual:InspectedMotionMp4):Boolean {
        val tables=actual.tables
        val expected=durationTicks(writtenPtsUs,tables.timescale) ?: return false
        if(tables.durations!=expected || tables.compositionOffsets.size!=expected.size ||
            tables.compositionOffsets.any {it!=0L} || tables.edits.isNotEmpty())return false
        if(actual.samples.size!=expected.size)return false
        var clock=0L
        for(index in expected.indices) {
            if(actual.samples[index].ptsUs!=clock*1_000_000L/tables.timescale)return false
            clock+=expected[index]
        }
        return true
    }
}
