package com.photocoach.app.camera
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MotionSessionBudgetTest {
    @Test fun delayedStartupDoesNotConsumeRecordingAndNeverReadyRemainsBounded() {
        val budget=MotionSessionBudget(1000)
        assertFalse(budget.expired(12999));assertTrue(budget.expired(13000))
        assertTrue(budget.ready(12000));assertEquals(8000L,budget.stopDelay(12000))
        assertEquals(32000L,budget.deadlineMs());assertFalse(budget.ready(12500))
        assertEquals(7500L,budget.stopDelay(12500))
    }
    @Test fun earlyFinishAndDelayedStopDoNotExtendTheirAbsoluteBudgets() {
        val budget=MotionSessionBudget(1000);assertTrue(budget.ready(2000))
        budget.finishing(5000);assertEquals(17000L,budget.deadlineMs())
        budget.finishing(8000);assertEquals(17000L,budget.deadlineMs())
        assertFalse(budget.expired(16999));assertTrue(budget.expired(17000))
        val late=MotionSessionBudget(1000);late.ready(2000);late.finishing(15000)
        assertEquals(22000L,late.deadlineMs());assertEquals(0L,late.stopDelay(15000))
    }
    @Test fun lateReadyAndOtherSessionEventsCannotResetThisSession() {
        val old=MotionSessionBudget(1000);assertFalse(old.ready(13000));assertFalse(old.ready(999))
        val current=MotionSessionBudget(20000);assertTrue(current.ready(21000))
        old.finishing(21000);assertEquals(13000L,old.deadlineMs());assertEquals(41000L,current.deadlineMs())
    }
}
