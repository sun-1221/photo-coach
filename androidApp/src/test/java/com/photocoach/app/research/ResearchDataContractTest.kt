package com.photocoach.app.research

import java.nio.file.Files
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ResearchDataContractTest {
    private fun event(round: Int, type: String, time: Long, id: String? = null, result: String? = null) =
        ResearchEvent(type, time, time, "close_up", round, "capturing", result = result,
            sessionId = "synthetic", condition = "DYNAMIC", scene = "WINDOW", configurationId = "synthetic-v1",
            buildVersion = "test", captureId = id)
    private fun log(events: List<ResearchEvent>): String {
        val file = Files.createTempDirectory("research-contract").resolve("events.jsonl").toFile()
        try { val logger = ResearchEventLogger(file); events.forEach { assertTrue(logger.record(it)) }
            val last=events.last();assertTrue(logger.record(last.copy(type="session_exit",elapsedMs=last.elapsedMs+1)))
            return file.readText() }
        finally { file.parentFile.deleteRecursively() }
    }
    @Test fun realJsonlRetainsTimeoutExitCaptureFailureAndSaveRecovery() {
        val rows = listOf(event(1,"round_operable",0), event(1,"round_timeout",30_000),
            event(2,"round_operable",31_000),event(2,"session_exit",32_000),
            event(3,"round_operable",33_000),event(3,"capture_accepted",34_000,"c"),event(3,"save",35_000,"c","capture_failed"),
            event(4,"round_operable",36_000),event(4,"capture_accepted",37_000,"d"),event(4,"save",38_000,"d","failed_retryable"),
            event(4,"save_retry",39_000,"d","started"),event(4,"original_published",40_000,"d"))
        val imported = ResearchLogReader.read(log(rows))
        assertTrue(imported.complete, imported.issues.toString())
        val result = ResearchRecomputation.replay(imported,(1..5).map { ResearchRoundKey("synthetic",it) }.toSet(),30_000)
        assertEquals(listOf(ResearchRoundOutcome.FAILED,ResearchRoundOutcome.EXITED,ResearchRoundOutcome.FAILED,
            ResearchRoundOutcome.FAILED,ResearchRoundOutcome.INCOMPLETE),result.map { it.outcome })
        assertTrue(result[3].firstAttemptFailed); assertTrue(result[3].recovered)
        assertEquals(4_000L,result[3].captureLatencyMs)
        assertFalse(result[2].recovered)
    }
    @Test fun corruptionLostEventsMetadataAndCaptureReuseFailClosed() {
        val rows=listOf(event(1,"round_operable",0),event(1,"capture_accepted",1,"a"),event(1,"original_published",2,"a"))
        val valid=log(rows)
        for (bad in listOf(valid+"{bad}\n",valid.lineSequence().filterNot { it.contains("capture_accepted") }.joinToString("\n"),
            log(rows.map { it.copy(previousDroppedEvents=1) }),log(rows + event(2,"capture_accepted",3,"a")),
            log(rows.mapIndexed { i,e -> if(i==2)e.copy(condition="STATIC") else e }),
            valid.replace("\"captureId\":\"a\"","\"captureId\":\"missing\"").replace("\"type\":\"capture_accepted\"","\"type\":\"stage\""))) {
            val imported=ResearchLogReader.read(bad)
            assertFalse(imported.complete)
            assertEquals(ResearchRoundOutcome.INCOMPLETE,ResearchRecomputation.replay(imported,setOf(ResearchRoundKey("synthetic",1)),30000).single().outcome)
        }
        val duplicated=ResearchLogReader.read(valid+valid.lineSequence().first()+"\n")
        assertTrue(duplicated.complete)
        assertEquals(1,ResearchRecomputation.replay(duplicated,setOf(ResearchRoundKey("synthetic",1)),30000).single().duplicateEvents)
    }
    @Test fun loggerEscapesControlsAndRecordsFailedWrites() {
        val dir=Files.createTempDirectory("failed-log").toFile()
        try {
            val file=dir.resolve("events");file.mkdir()
            val logger=ResearchEventLogger(file)
            assertFalse(logger.record(event(1,"stage",0)))
            assertTrue(file.delete())
            assertTrue(logger.record(event(1,"stage",1,result="tab\tcontrol\u0001quote\"")))
            val imported=ResearchLogReader.read(file.readText())
            assertEquals("tab\tcontrol\u0001quote\"",imported.events.single().result)
            assertFalse(imported.complete);assertTrue(imported.issues.any { it.contains("lost events") })
        } finally { dir.deleteRecursively() }
    }
    @Test fun syntheticSideRecordsKeepMissingTiesDisagreementsAndLostFollowUpsSeparate() {
        val a=ResearchRoundKey("a",1);val b=ResearchRoundKey("b",1);val c=ResearchRoundKey("c",1)
        val trials=listOf(RegisteredTrial(a,"p1","SYSTEM","WINDOW",1),RegisteredTrial(b,"p1","DYNAMIC","WINDOW",2),
            RegisteredTrial(c,"p2","DYNAMIC","WINDOW",1))
        val ratings=listOf(BlindRating("pair1",a,b,"shooter",BlindChoice.FIRST),BlindRating("pair1",a,b,"subject",BlindChoice.SECOND),
            BlindRating("pair2",a,b,"shooter",BlindChoice.TIE),BlindRating("pair2",a,b,"subject",BlindChoice.MISSING,false))
        val summary=ResearchSideContract.inspect(trials,ratings,listOf(FollowUp("p1",7,true,false),FollowUp("p2",null,false,false)))
        assertEquals(2,summary.participants);assertEquals(3,summary.trials);assertEquals(1,summary.ties)
        assertEquals(1,summary.missingRatings);assertEquals(1,summary.missingImages);assertEquals(1,summary.disagreements)
        assertEquals(1,summary.lostFollowUps);assertNull(summary.naturalReturns);assertTrue(summary.blockers.isNotEmpty())
        val invalid=ResearchSideContract.inspect(trials+trials.first(),ratings+ratings.first(),listOf(FollowUp("unknown",7,true,false)))
        assertTrue(invalid.issues.containsAll(listOf("duplicate trial registration","duplicate rating","invalid follow-up association")))
        val roster=listOf(RegisteredBlindPair("pair1",a,b),RegisteredBlindPair("no-response",a,b))
        val absent=ResearchSideContract.inspect(trials,listOf(ratings.first()),emptyList(),pairs=roster)
        assertEquals(3,absent.missingRatings) // absent subject plus both wholly missing pair slots
        assertEquals(4,ResearchSideContract.completeRatings(listOf(ratings.first()),roster).size)
    }
}
