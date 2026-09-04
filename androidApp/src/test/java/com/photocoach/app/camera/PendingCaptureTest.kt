package com.photocoach.app.camera

import com.photocoach.app.creative.CaptureId
import com.photocoach.app.creative.CreativeStyle
import com.photocoach.app.creative.EditAdjustment
import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.io.path.writeBytes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class PendingCaptureTest {
    @TempDir
    lateinit var directory: Path

    @Test
    fun `journal snapshot preserves immutable identity completed stages and retry evidence`() {
        val source = directory.resolve("source.jpg").createFile().also { it.writeBytes(byteArrayOf(1, 2, 3)) }.toFile()
        val pending = PendingCapture(
            file = source,
            spec = CaptureSpec(
                captureId = CaptureId("abc123def456"),
                sequence = 2,
                takenAtMillis = 1_700_000_000_000L,
                style = CreativeStyle.CLEAR_TRAVEL,
                edit = EditAdjustment(styleStrength = 0.7f),
                saveStrategy = SaveStrategy.ORIGINAL_AND_EFFECT,
                derivativeQuality = DerivativeQuality.SPACE_SAVER,
                livePhotoRequested = true,
            ),
            onSaved = {},
            onSaveError = {},
            onSaveProgress = {},
            shutterElapsedMs = 100L,
            recordingStartedElapsedMs = 50L,
        )
        pending.coordinator.complete(SaveStage.SPACE_CHECK)
        pending.coordinator.fallbackFromMotionPhoto("encoder unavailable")
        pending.coordinator.complete(SaveStage.ORIGINAL_PUBLISH)
        pending.verifiedAssetStages += "original:verify-published"
        pending.stageRetryCounts[SaveStage.ORIGINAL_PUBLISH.name] = 1

        val journal = pending.toJournal()

        assertEquals("abc123def456", journal.captureId)
        assertEquals(2, journal.sequence)
        assertEquals(source.absolutePath, journal.sourcePath)
        assertTrue(journal.motionPhotoFallback)
        assertTrue(journal.derivativeRequested)
        assertTrue(SaveStage.ORIGINAL_PUBLISH.name in journal.completedStages)
        assertEquals(1, journal.stageRetryCounts[SaveStage.ORIGINAL_PUBLISH.name])
        assertEquals(setOf("original:verify-published"), journal.verifiedAssetStages)
    }
}
