package com.photocoach.app.research
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
class ResearchRecoveryAssociationTest {
    @Test fun exportedBundleReadsActualLoggerAndReceiptFilesWithoutInventingPublicationTime() {
        val directory=java.nio.file.Files.createTempDirectory("research-bundle").toFile()
        try {
            val log=java.io.File(directory,"events.jsonl");val logger=ResearchEventLogger(log)
            val accepted=ResearchEvent("capture_accepted",0,1,"close_up",1,"capturing",sessionId="s",condition="DYNAMIC",scene="WINDOW",configurationId="synthetic",buildVersion="test",captureId="a")
            assertTrue(logger.record(accepted.copy(type="round_operable",elapsedMs=0,captureId=null)))
            assertTrue(logger.record(accepted));assertTrue(logger.record(accepted.copy(type="save",elapsedMs=2,result="failed_retryable")))
            val store=ResearchRecoveryStore(java.io.File(directory,"receipts"))
            store.record(ResearchRecoveryReceipt("a",ResearchCaptureContext("s",1,"close_up","DYNAMIC","WINDOW","synthetic","test")))
            val crashed=ResearchEvidenceBundles.read(ResearchEvidenceBundles.export(log,store))
            assertFalse(crashed.log.complete)
            assertEquals(ResearchRoundOutcome.INCOMPLETE,crashed.replay(setOf(ResearchRoundKey("s",1)),30000).single().outcome)
            assertTrue(logger.record(accepted.copy(type="session_exit",elapsedMs=3,captureId=null)))
            val complete=ResearchEvidenceBundles.read(ResearchEvidenceBundles.export(log,store))
            assertTrue(complete.log.complete,complete.log.issues.toString())
            val round=complete.replay(setOf(ResearchRoundKey("s",1)),30000).single()
            assertTrue(round.recovered);assertNull(round.captureLatencyMs);assertEquals(1,round.acceptedCaptures)
            assertFalse(ResearchEvidenceBundles.read("{broken").log.complete)
        } finally {directory.deleteRecursively()}
    }
    @Test fun recoveryKeepsOldOwnerAndDoesNotInventTimeOrCountNewCapture() {
        val accepted=ResearchEvent("capture_accepted",0,1,"close_up",7,"capturing",sessionId="old",condition="DYNAMIC",scene="WINDOW",configurationId="synthetic",buildVersion="test",captureId="a")
        val failed=accepted.copy(type="save",elapsedMs=2,result="failed_retryable")
        val context=ResearchCaptureContext("old",7,"close_up","DYNAMIC","WINDOW","synthetic","test")
        val receipt=ResearchRecoveryReceipt("a",context)
        assertEquals(setOf("a"),ResearchRecoveryAssociation.join(listOf(accepted,failed),listOf(receipt)).recoveredCaptureIds)
        assertTrue(ResearchRecoveryAssociation.join(listOf(accepted,failed),listOf(receipt.copy(context=context.copy(sessionId="new")))).issues.isNotEmpty())
        assertTrue(ResearchRecoveryAssociation.join(listOf(accepted),listOf(receipt)).recoveredCaptureIds.isEmpty())
        assertTrue(ResearchRecoveryAssociation.join(listOf(accepted,failed),listOf(receipt,receipt)).issues.isNotEmpty())
        assertTrue(ResearchRecoveryAssociation.join(listOf(accepted,accepted.copy(type="original_published"),failed),listOf(receipt)).recoveredCaptureIds.isEmpty())
        assertNull(receipt.originalSessionPublicationElapsedMs)
        val events=listOf(accepted.copy(type="round_operable",captureId=null,elapsedMs=0),accepted,failed,
            accepted.copy(type="session_exit",captureId=null,elapsedMs=3))
        val round=ResearchRecomputation.replay(ResearchLogImport(events,emptyList()),setOf(ResearchRoundKey("old",7)),30000,listOf(receipt)).single()
        assertEquals(ResearchRoundOutcome.FAILED,round.outcome);assertTrue(round.recovered)
        assertEquals(1,round.acceptedCaptures);assertNull(round.captureLatencyMs)
        assertEquals(ResearchRoundOutcome.INCOMPLETE,ResearchRecomputation.replay(ResearchLogImport(events,listOf("missing closure")),
            setOf(round.key),30000,listOf(receipt)).single().outcome)
    }
}
