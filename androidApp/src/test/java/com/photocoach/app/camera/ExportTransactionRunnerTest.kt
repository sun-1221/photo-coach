package com.photocoach.app.camera

import java.io.File
import java.nio.file.Files
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ExportTransactionRunnerTest {
    private class Fixture {
        val dir = Files.createTempDirectory("export-transactions").toFile()
        val store = SaveJournalStore(File(dir, "journal"))
        val rows = mutableSetOf<String>()
        val strengths = mutableListOf<Float>()
        var failPublish = false
        var failCompletion = false
        var failCleanup = false
        fun request(id: String = "derivative000001", strength: Float = .4f) = SaveJournal(
            captureId = "capture00000001", sequence = 1, takenAtMillis = 100,
            sourcePath = "", displayName = "$id.JPG", style = "NATURAL_PORTRAIT",
            derivativeId = id, exportSourceUri = "content://original/1", styleStrength = strength)
        fun runner() = ExportTransactionRunner(store,
            generate = { record ->
                strengths += record.styleStrength
                GeneratedExport(File(dir, "${record.derivativeId}.jpg").apply { writeText("jpeg") }, true)
            },
            publish = { record, _, pending ->
                val uri = record.derivativePendingUri?.takeIf { it in rows } ?: "content://derived/${rows.size + 1}".also {
                    rows += it; pending(it)
                }
                if (failPublish) error("provider interrupted")
                uri
            },
            verify = { _, uri -> check(uri in rows) },
            write = { record ->
                if (failCompletion && SaveStage.COMPLETE.name in record.completedStages) error("journal full")
                store.write(record); Unit
            },
            cleanup = { file -> if (failCleanup) error("cleanup refused") else file.delete(); Unit },
        )
    }
    @Test fun newExportAfterFailureUsesNewIdentityAndNewRecipeWhileRetryRetainsOldRecipe() {
        val f = Fixture()
        f.failPublish = true
        val old = f.request()
        assertThrows(IllegalStateException::class.java) { f.runner().create(old) }
        f.failPublish = false
        f.runner().create(f.request("derivative000002", .8f))
        f.runner().retry(old.copy(styleStrength = 1f))
        assertEquals(listOf(.4f, .8f), f.strengths)
        assertEquals(2, f.rows.size)
        assertEquals(.4f, f.store.read(old)!!.styleStrength)
    }
    @Test fun recoveryCompletionAndStaleRetryDoNotResurrectOrRepublish() {
        val f = Fixture(); val request = f.request()
        val saved = f.runner().create(request)
        assertEquals(saved, f.runner().retry(request))
        assertEquals(1, f.rows.size)
        assertEquals(1, f.strengths.size)
        f.store.delete(request)
        assertThrows(IllegalStateException::class.java) { f.runner().retry(request) }
        assertTrue(f.store.readAll().isEmpty())
    }
    @Test fun commitThenJournalOrCleanupFailureRemainsIdempotentAfterRestart() {
        val f = Fixture(); val request = f.request()
        f.failCompletion = true
        assertThrows(IllegalStateException::class.java) { f.runner().create(request) }
        assertEquals(1, f.rows.size)
        f.failCompletion = false; f.failCleanup = true
        val result = f.runner().retry(request)
        assertEquals(result, f.runner().retry(request))
        f.failCleanup = false
        assertEquals(result, f.runner().retry(request))
        assertEquals(1, f.rows.size)
        assertEquals(1, f.strengths.size)
    }
    @Test fun onlineAndRecoveryShareExactLeaseAndCannotOverwriteSameTemporaryJournal() {
        val f = Fixture(); val request = f.request(); f.store.write(request)
        SaveTransactionRegistry.tryAcquire(f.store.transactionKey(request))!!.use {
            assertThrows(IllegalStateException::class.java) { f.runner().retry(request) }
            assertThrows(IllegalStateException::class.java) { f.runner().create(request) }
            assertEquals(request, f.store.read(request))
        }
        f.runner().retry(request)
        assertEquals(1, f.rows.size)
    }
}
