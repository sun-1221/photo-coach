package com.photocoach.app.camera

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MotionMuxTimingModelTest {
    @Test fun completeActualAvdContainerFixtureMatchesWrittenInputsAndRejectsTampering() {
        val lines=javaClass.getResourceAsStream("/motion/encoder-vfr-avd-api36.txt")!!.bufferedReader().use {it.readLines()}
        fun values(prefix:String)=lines.first {it.startsWith(prefix)}.substringAfter('[').substringBefore(']')
            .split(',').filter {it.isNotBlank()}.map {it.trim().toLong()}
        val rows=lines.drop(5).map {it.split(',')}
        val tables=MotionTimeTables(90_000,values("stts="),values("ctts="),emptyList())
        assertEquals("elst=[]",lines[3]);assertEquals(119,rows.size)
        val actual=InspectedMotionMp4(tables,rows.map {MuxedVideoSample(it[4].toLong(),it[5].toBooleanStrict())})
        val written=rows.map {it[3].toLong()}
        assertTrue(MotionMuxTimingModel.matches(written,actual))
        val changed=actual.samples.toMutableList().also {it[52]=it[52].copy(ptsUs=it[52].ptsUs+1)}
        assertFalse(MotionMuxTimingModel.matches(written,actual.copy(samples=changed)))
        assertFalse(MotionMuxTimingModel.matches(written.drop(1),actual))
        assertFalse(MotionMuxTimingModel.matches(written,actual.copy(samples=actual.samples.map {it.copy(ptsUs=it.ptsUs*2)})))
    }
    @Test fun integerCoalescingReproducesIndependentlyCalculatedVfrPoints() {
        val inputs=(1..119).map {it*33_333L+(it%3)*500L}.let {list->list.map {it-list.first()}}
        val durations=MotionMuxTimingModel.durationTicks(inputs,90_000)!!
        var clock=0L
        val pts=durations.map {duration->(clock*1_000_000L/90_000).also {clock+=duration}}
        assertEquals(1_699_977L,pts[51]);assertEquals(1_733_800L,pts[52]);assertEquals(1_766_133L,pts[53])
        assertEquals(1_733_816L,inputs[52])
        val actual=InspectedMotionMp4(MotionTimeTables(90_000,durations,List(119){0},emptyList()),
            pts.mapIndexed {i,time->MuxedVideoSample(time,i==0)})
        assertTrue(MotionMuxTimingModel.matches(inputs,actual))
        assertFalse(MotionMuxTimingModel.matches(inputs,actual.copy(samples=actual.samples.map {it.copy(ptsUs=it.ptsUs*2)})))
        assertFalse(MotionMuxTimingModel.matches(inputs,actual.copy(tables=actual.tables.copy(durations=durations.map {it*2}))))
        assertFalse(MotionMuxTimingModel.matches(inputs,actual.copy(tables=actual.tables.copy(compositionOffsets=List(119){1}))))
    }
    @Test fun unmodelledDecodeClampingAndMalformedInputRemainRejected() {
        assertNull(MotionMuxTimingModel.durationTicks(listOf(0,1,2),90_000))
        assertNull(MotionMuxTimingModel.durationTicks(listOf(1,1000),90_000))
        assertNull(MotionMuxTimingModel.durationTicks(listOf(0,1000),0))
        assertNull(MotionMuxTimingModel.durationTicks(listOf(0,8_000_001),90_000))
    }
}
