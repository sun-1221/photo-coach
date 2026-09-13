package com.photocoach.app.camera

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MotionFrameTimelineTest {
    @Test fun producerCanStopBeforeLateFrameWithoutInvalidatingRetainedSegment() {
        val timeline=ledger()
        timeline.sensorResult(source,origin,0)
        assertEquals(0L,timeline.submit(origin));timeline.encoded(0,true,100)
        assertFalse(timeline.exceedsInputDuration(origin+8_000_000_000L))
        assertTrue(timeline.exceedsInputDuration(origin+8_001_000_000L))
        // The producer does not submit this frame; no clamp and no extra sample is recorded.
        assertNull(timeline.failure);assertEquals(1,timeline.writtenSamples().size)
    }
    @Test fun postbufferWaitsForRetainedSensorCoverageRatherThanButtonElapsedTime() {
        val source=MotionSensorSource("camera",1)
        val timeline=MotionFrameTimeline(1,source)
        val origin=80_000_000_000L
        fun frame(index:Int) {
            val sensor=origin+index*33_333_333L
            timeline.sensorResult(source,sensor,index.toLong())
            timeline.submit(sensor)?.let {timeline.encoded(it,index==0,100)}
        }
        (0..104).forEach(::frame)
        val capture=origin+2_000_000_000L
        assertFalse(timeline.hasPostbuffer(source,capture))
        frame(105);assertFalse(timeline.hasPostbuffer(source,capture))
        frame(106);assertTrue(timeline.hasPostbuffer(source,capture))
        assertFalse(timeline.hasPostbuffer(source.copy(generation=2),capture))
        assertFalse(timeline.hasPostbuffer(source,Long.MAX_VALUE))
    }
    private val source=MotionSensorSource("back",7)
    private val origin=90_000_000_000L
    private fun ledger()=MotionFrameTimeline(3,source)
    @Test fun muxRoundingRequiresMeasuredTrackTimebaseAndTimeScalingIsRejected() {
        fun populated(times:List<Long>):MotionFrameTimeline = ledger().also {ledger ->
            times.forEachIndexed {index,time ->
                ledger.sensorResult(source,origin+time*1000,index.toLong())
                ledger.encoded(ledger.submit(origin+time*1000)!!,index==0,100)
            }
        }
        val quantized=listOf(MuxedVideoSample(0,true),MuxedVideoSample(33_333,false),MuxedVideoSample(66_677,false))
        assertNull(populated(listOf(0,33_337,66_679)).verifyMux(quantized))
        assertNotNull(populated(listOf(0,33_337,66_679)).verifyMux(quantized,90_000))
        assertNull(populated(listOf(0,1_000_000,2_000_000,3_000_000)).verifyMux(
            listOf(MuxedVideoSample(0,true),MuxedVideoSample(2_000_000,false),MuxedVideoSample(4_000_000,false),MuxedVideoSample(6_000_000,false)),90_000))
    }
    @Test fun droppedFirstInputAndNonKeyOutputsDoNotBecomeTheRetainedOrigin() {
        val ledger=ledger()
        val times=listOf(0L,23_000L,57_000L,102_000L,159_000L)
        val submitted=times.mapIndexed {i,t ->
            ledger.sensorResult(source,origin+t*1000,i.toLong())
            ledger.submit(origin+t*1000)!!
        }
        // Input 0 was dropped by the encoder. Output 1 is not a random-access point.
        assertNull(ledger.encoded(submitted[1],false,100))
        assertEquals(0L,ledger.encoded(submitted[2],true,100))
        assertEquals(45_000L,ledger.encoded(submitted[3],false,100))
        assertNull(ledger.encoded(submitted[3],false,0)) // empty EOS/config is not a sample
        assertEquals(102_000L,ledger.encoded(submitted[4],false,100))
        val verified=ledger.verifyMux(listOf(MuxedVideoSample(0,true),MuxedVideoSample(45_000,false),MuxedVideoSample(102_000,false)))!!
        assertEquals(origin+57_000_000,verified.frames.first().sensorNs)
        assertEquals(57_000L,verified.frames.first().submittedPtsUs)
        assertNull(verified.presentationUs(source,origin))
        assertEquals(45_000L,verified.presentationUs(source,origin+102_000_000))
    }
    @Test fun prebufferCanUseACorrelatedSuffixWhileEarlierResultsAreStillPending() {
        val ledger=ledger()
        for(i in 0..4)ledger.encoded(ledger.submit(origin+i*1_000_000_000L)!!,i==0,100)
        assertFalse(ledger.hasPrebuffer())
        for(i in 2..4)ledger.sensorResult(source,origin+i*1_000_000_000L,i.toLong())
        assertTrue(ledger.hasPrebuffer())
        val incomplete=ledger()
        for(i in 0..4)incomplete.encoded(incomplete.submit(origin+i*1_000_000_000L)!!,i==0,100)
        for(i in 2..4)incomplete.sensorResult(source,origin+i*1_000_000_000L,i.toLong())
        assertNull(incomplete.verifyMux((0..4).map {MuxedVideoSample(it*1_000_000L,it==0)}))
    }
    @Test fun prebufferIsNotReadyWhenRemainingMediaSpanCannotHoldPostbuffer() {
        val late=ledger()
        for(i in 0..7)late.encoded(late.submit(origin+i*1_000_000_000L)!!,i==0,100)
        for(i in 5..7)late.sensorResult(source,origin+i*1_000_000_000L,i.toLong())
        assertFalse(late.hasPrebuffer())
        val room=ledger()
        for(i in 0..5)room.encoded(room.submit(origin+i*1_000_000_000L)!!,i==0,100)
        for(i in 3..5)room.sensorResult(source,origin+i*1_000_000_000L,i.toLong())
        assertTrue(room.hasPrebuffer())
    }
    @Test fun sensorCorrelationCanArriveAfterEncodingButEveryRetainedSampleMustMatch() {
        val ledger=ledger()
        for(i in 0..4) {
            val ns=origin+i*1_000_000_000L
            ledger.encoded(ledger.submit(ns)!!,i==0,100)
        }
        assertFalse(ledger.hasPrebuffer())
        for(i in 0..4)ledger.sensorResult(source,origin+i*1_000_000_000L,i.toLong())
        assertTrue(ledger.hasPrebuffer())
        val verified=ledger.verifyMux((0..4).map {MuxedVideoSample(it*1_000_000L,it==0)})!!
        assertEquals(MotionClipWindow(500_000,3_500_000,1_500_000),verified.fullWindow(source,origin+2_000_000_000))
        assertNull(verified.fullWindow(source,origin+1_000_000_000))
        assertNull(verified.fullWindow(source.copy(generation=8),origin+2_000_000_000))
    }
    @Test fun unknownOrAmbiguousAssociationAndOldGenerationFailClosed() {
        for(mode in 0..2) {
            val ledger=ledger()
            for(i in 0..1)ledger.encoded(ledger.submit(origin+i*100_000L)!!,i==0,100)
            if(mode==1) {
                ledger.sensorResult(source,origin,1);ledger.sensorResult(source,origin,2)
            }
            if(mode==2)ledger.sensorResult(source.copy(generation=8),origin,1)
            assertNull(ledger.verifyMux(listOf(MuxedVideoSample(0,true),MuxedVideoSample(100,false))))
        }
    }
    @Test fun duplicateCoalescedInputIsIgnoredButBackwardsAndUnmatchedOutputsInvalidate() {
        val duplicate=ledger()
        assertEquals(0L,duplicate.submit(origin));assertNull(duplicate.submit(origin));assertNull(duplicate.failure)
        assertNull(duplicate.submit(origin-1));assertNotNull(duplicate.failure)
        val unmatched=ledger();unmatched.submit(origin)
        assertNull(unmatched.encoded(1,true,100));assertNotNull(unmatched.failure)
        val reordered=ledger();reordered.submit(origin);reordered.submit(origin+100_000)
        reordered.encoded(100,true,100);assertNull(reordered.encoded(0,true,100));assertNotNull(reordered.failure)
    }
    @Test fun byteFrameDurationAndMuxMismatchQuotasFailClosed() {
        val bytes=ledger();bytes.submit(origin)
        assertNull(bytes.encoded(0,true,(MotionTemporaryPolicy.MAX_RECORDING_BYTES+1).toInt()))
        assertNotNull(bytes.failure)
        val frames=ledger()
        repeat(MotionFrameTimeline.MAX_FRAMES){assertNotNull(frames.submit(origin+it*1000L))}
        assertNull(frames.submit(origin+MotionFrameTimeline.MAX_FRAMES*1000L));assertNotNull(frames.failure)
        val duration=ledger();duration.submit(origin)
        assertNull(duration.submit(origin+8_001_000_000));assertNotNull(duration.failure)
        val mismatch=ledger()
        for(i in 0..1) {
            mismatch.sensorResult(source,origin+i*100_000L,i.toLong())
            mismatch.encoded(mismatch.submit(origin+i*100_000L)!!,i==0,100)
        }
        assertNull(mismatch.verifyMux(listOf(MuxedVideoSample(0,true))))
    }
}
