package com.photocoach.app.research
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
class CaptureRecomputationTest {
    private fun e(type:String,t:Long,id:String?=null,result:String?=null)=ResearchEvent(type,t,t,"close_up",1,"capturing",
        result=result,sessionId="s",condition="DYNAMIC",scene="WINDOW",configurationId="synthetic",buildVersion="test",captureId=id)
    private fun replay(events:List<ResearchEvent>)=ResearchRecomputation.replay(events,setOf(ResearchRoundKey("s",1)),30000).single()
    @Test fun aFailedCaptureAndNewBPublicationIsNotARecovery() {
        val r=replay(listOf(e("round_operable",0),e("capture_accepted",1,"a"),e("save",2,"a","capture_failed"),
            e("capture_accepted",3,"b"),e("original_published",4,"b")))
        assertEquals(2,r.acceptedCaptures);assertFalse(r.recovered)
        assertFalse(r.captures[0].firstAttemptSucceeded);assertTrue(r.captures[1].firstAttemptSucceeded)
    }
    @Test fun sameCaptureRetryAndLatePublicationRetainExitAndAcceptedLedger() {
        val r=replay(listOf(e("round_operable",0),e("capture_accepted",1,"a"),e("save",2,"a","failed_retryable"),
            e("save_retry",3,"a"),e("session_exit",4),e("original_published",5,"a"),e("capture_accepted",6,"b")))
        assertEquals(ResearchRoundOutcome.FAILED,r.outcome);assertEquals(4L,r.exitAtMs);assertTrue(r.recovered)
        val policy=CalculationPolicy(true,MissingChoiceRule.EXCLUDE,TieChoiceRule.EXCLUDE,FailedTimeRule.EXCLUDE_PAIR,
            IncrementBaseline.DIRECT_DYNAMIC_STATIC_ABOVE_HALF,30000,6,8,true,5000)
        val m=ResearchMetricCalculation.calculate(listOf(TrialMeasurement(RegisteredTrial(r.key,"p","DYNAMIC","WINDOW",1),r,setOf(1))),
            emptyList(),emptyList(),emptyList(),emptyList(),emptyList(),emptyList(),policy).metrics!!
        assertEquals(1,m.withinDeadline.denominator)
        assertEquals(0.0,m.withinDeadline.numerator)
        assertEquals(0,m.twoSteps.denominator)
        assertEquals(Fraction(0.0,2),m.firstSave);assertEquals(Fraction(1.0,1),m.recovery)
    }
    @Test fun rejectedRequestIsFailedRoundWithoutInventingAcceptedCapture() {
        val r=replay(listOf(e("round_operable",0),e("capture_rejected",1,"a","capture_failed")))
        assertEquals(ResearchRoundOutcome.FAILED,r.outcome);assertEquals(0,r.acceptedCaptures)
    }
    @Test fun publicationThenErrorAtSameMillisecondDoesNotRewriteFirstSave() {
        val r=replay(listOf(e("round_operable",0),e("capture_accepted",1,"a"),
            e("original_published",2,"a").copy(sequence=2),e("save",2,"a","failed_retryable").copy(sequence=3)))
        assertTrue(r.captures.single().firstAttemptSucceeded)
        assertFalse(r.firstAttemptFailed)
    }
    @Test fun timeoutThenExitRemainsFailedAndPureEarlyExitRemainsSeparate() {
        assertEquals(ResearchRoundOutcome.FAILED,replay(listOf(e("round_operable",0),e("round_timeout",30000),e("session_exit",30001))).outcome)
        assertEquals(ResearchRoundOutcome.FAILED,replay(listOf(e("round_operable",0),e("session_exit",30001))).outcome)
        assertEquals(ResearchRoundOutcome.EXITED,replay(listOf(e("round_operable",0),e("session_exit",1))).outcome)
    }
    @Test fun sameMillisecondExitBeforePublicationKeepsExitOrder() {
        val r=replay(listOf(e("round_operable",0),e("capture_accepted",1,"a"),
            e("session_exit",2).copy(sequence=2),e("original_published",2,"a").copy(sequence=3)))
        assertEquals(ResearchRoundOutcome.EXITED,r.outcome);assertTrue(r.exitedBeforePublication)
        assertTrue(r.captures.single().firstAttemptSucceeded)
    }
}
