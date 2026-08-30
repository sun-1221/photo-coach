package com.photocoach.app.camera

import java.io.File
import java.nio.file.Path
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class SaveJournalStoreTest {
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
