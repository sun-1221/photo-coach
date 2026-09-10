package com.photocoach.app.camera

import java.io.File
import java.nio.file.Path
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class SaveJournalStoreTest {
    @Test fun legacySchemaFixturesPreserveOriginalKeysAndPublishedIdentityOnRetry() {
        for (version in 1..3) {
            val directory = temporaryDirectory.resolve("legacy-$version").toFile().apply { mkdirs() }
            val fixture = requireNotNull(javaClass.getResource("/save-journal/v$version.json")).readText()
            File(directory, "legacycapture001_S02.json").writeText(fixture)
            val store = SaveJournalStore(directory)
            val record = store.readAll().single()
            assertEquals(version, record.schemaVersion)
            assertEquals("legacycapture001_S02", record.key)
            assertEquals(null, record.batchId)
            assertEquals(null, record.derivativeId)
            store.write(record.copy(stageRetryCounts = mapOf("RECIPE_WRITE" to 1)))
            val retry = store.readAll().single()
            assertEquals(record.originalUri, retry.originalUri)
            assertEquals(record.completedStages, retry.completedStages)
            assertEquals(record.key, retry.key)
            assertEquals(1, directory.listFiles()!!.size)
        }
    }
    @Test
    fun journalCarriesVersionedVerificationAndRetryState() {
        val store = SaveJournalStore(temporaryDirectory.resolve("versioned-journals").toFile())
        val record = SaveJournal(
            captureId = "feed1234cafe5678",
            sequence = 1,
            takenAtMillis = 1234,
            sourcePath = "pending.jpg",
            displayName = "IMG_test_S01_ORIG.JPG",
            style = "ORIGINAL",
            outputLength = 1234,
            verifiedAssetStages = setOf(AssetPublishStage.VERIFY_PENDING.name),
            stageRetryCounts = mapOf(SaveStage.ORIGINAL_PUBLISH.name to 2),
        )
        store.write(record)
        val restored = store.readAll().single()
        assertEquals(SaveJournal.CURRENT_SCHEMA_VERSION, restored.schemaVersion)
        assertEquals(1234, restored.outputLength)
        assertEquals(2, restored.stageRetryCounts[SaveStage.ORIGINAL_PUBLISH.name])
    }
    @TempDir
    lateinit var temporaryDirectory: Path

    @Test
    fun `recovery record atomically preserves capture stage pending uri and retry identity`() {
        val store = SaveJournalStore(temporaryDirectory.resolve("journals").toFile())
        val record = SaveJournal(
            captureId = "abc123def4567890",
            sequence = 2,
            takenAtMillis = 1234,
            sourcePath = "pending.jpg",
            displayName = "IMG_test_S02_ORIG.JPG",
            style = "NATURAL_PORTRAIT",
            completedStages = setOf(SaveStage.SPACE_CHECK.name),
            failedStage = SaveStage.ORIGINAL_PUBLISH.name,
            pendingUri = "content://media/pending/7",
            derivativeRequested = true,
        )

        val file = store.write(record)
        assertTrue(file.isFile)
        assertFalse(File(file.parentFile, "${file.name}.tmp").exists())
        assertEquals(record, store.readAll().single())
        store.delete(record)
        assertTrue(store.readAll().isEmpty())
    }
}
