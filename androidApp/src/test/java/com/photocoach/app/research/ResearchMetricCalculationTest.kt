package com.photocoach.app.research
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ResearchMetricCalculationTest {
    @Test fun syntheticAllMetricFamiliesExposeDenominatorsAndPolicyChoicesWithoutGo() {
        val trials=mutableListOf<TrialMeasurement>()
        fun add(person:String,condition:String,order:Int,ms:Long?,failed:Boolean=false):ResearchRoundKey {
            val key=ResearchRoundKey("$person-$condition",1)
            trials+=TrialMeasurement(RegisteredTrial(key,person,condition,"WINDOW",order),
                RecomputedRound(key,if(ms!=null && ms<=30000)ResearchRoundOutcome.SUCCESS else ResearchRoundOutcome.FAILED,
                    ms,0,"synthetic",listOf(RecomputedCapture("$person-$condition",0,ms,failed&&ms==null,failed&&ms!=null,failed&&ms!=null))),setOf(1,2))
            return key
        }
        val system=add("p1","SYSTEM",1,20000);val static=add("p1","STATIC",2,20000);val dynamic=add("p1","DYNAMIC",3,10000)
        add("p2","STATIC",1,25000);add("p2","DYNAMIC",2,35000,true)
        add("p3","DYNAMIC",1,null,true)
        val ratings=listOf(BlindRating("ds",dynamic,system,"shooter",BlindChoice.FIRST),
            BlindRating("ds",dynamic,system,"subject",BlindChoice.TIE),
            BlindRating("dt",dynamic,static,"shooter",BlindChoice.FIRST),
            BlindRating("dt",dynamic,static,"subject",BlindChoice.MISSING))
        val follow=listOf(FollowUp("p1",7,true,false),FollowUp("p2",7,true,true),FollowUp("p3",null,false,false))
        val cues=listOf(CueReview("c1",4500,true,setOf("wrong audience","conflict"),true),
            CueReview("c2",6000,true,emptySet(),true),CueReview("c3",null,false,emptySet(),false))
        val answers=listOf(VoiceAnswer("a",true),VoiceAnswer("b",false),VoiceAnswer("c",null))
        val perf=listOf(PerformanceSample("p1","cold",500),PerformanceSample("p2","cold",null),
            PerformanceSample("p3","stable-to-visible",200),PerformanceSample("p4","scene-to-visible",900))
        val offline=listOf(OfflineAttempt("o1",true),OfflineAttempt("o2",false))
        val pairs=listOf(RegisteredBlindPair("ds",dynamic,system),RegisteredBlindPair("dt",dynamic,static))
        fun run(policy:CalculationPolicy?)=ResearchMetricCalculation.calculate(trials,ratings,follow,cues,answers,perf,offline,policy,pairs)
        assertNull(run(null).metrics)
        val policy=CalculationPolicy(false,MissingChoiceRule.COUNT_AS_NOT_PREFERRED,TieChoiceRule.HALF,
            FailedTimeRule.USE_DEADLINE,IncrementBaseline.DIRECT_DYNAMIC_STATIC_ABOVE_HALF,30000,6,8,true,5000)
        val result=run(policy);assertTrue(result.blockers.isEmpty());val m=result.metrics!!
        assertEquals(Fraction(1.0,3),m.withinDeadline);assertEquals(Fraction(1.0,1),m.twoSteps)
        assertEquals(1.0,m.preferencesByAudience.getValue("shooter").value)
        assertEquals(.5,m.preferencesByAudience.getValue("subject").value)
        assertEquals(.5,m.incrementByAudience["shooter"])
        assertEquals(1.0-20000.0/22500.0,m.pairedTimeReduction!!,.000001) // failed dynamic time uses the explicit 30s deadline.
        assertEquals(.5,run(policy.copy(failedTime=FailedTimeRule.EXCLUDE_PAIR)).metrics!!.pairedTimeReduction!!,.000001)
        assertEquals(Fraction(1.0,3),m.naturalReuse);assertEquals(Fraction(1.0,2),m.cueImprovement)
        assertEquals(Fraction(1.0,2),m.cueErrors);assertEquals(Fraction(1.0,2),m.disturbance)
        assertEquals(500L,m.p95ByKind["cold"]);assertEquals(1,m.unfinishedPerformance)
        assertEquals(Fraction(4.0,6),m.firstSave);assertEquals(Fraction(1.0,1),m.recovery)
        assertEquals(Fraction(1.0,2),m.offline);assertEquals(1,m.missingAnswers);assertEquals(1,m.unobservableCues)
        val other=run(policy.copy(ties=TieChoiceRule.EXCLUDE,missing=MissingChoiceRule.EXCLUDE,d7IncludeLost=false)).metrics!!
        assertNull(other.preferencesByAudience.getValue("subject").value);assertEquals(Fraction(1.0,2),other.naturalReuse)
        val invalid=ResearchMetricCalculation.calculate(trials+trials.first(),ratings,follow,cues,answers,perf,offline,policy)
        assertNull(invalid.metrics);assertTrue(invalid.blockers.isNotEmpty())
    }
}
