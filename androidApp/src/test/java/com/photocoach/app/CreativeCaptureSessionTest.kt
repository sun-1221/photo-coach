package com.photocoach.app

import com.photocoach.app.camera.CameraUserSettings
import com.photocoach.app.camera.DerivativeQuality
import com.photocoach.app.camera.SaveStrategy
import com.photocoach.app.creative.BurstSession
import com.photocoach.app.creative.CaptureId
import com.photocoach.app.creative.CreativeStyle
import com.photocoach.app.creative.EditAdjustment
import com.photocoach.app.creative.PhotoQualityScore
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CreativeCaptureSessionTest {
    @Test fun `each shot has a unique capture id and a stable batch id`() {
        val session = CreativeCaptureSession()
        session.begin(3, CreativeStyle.ORIGINAL, CameraUserSettings.DEFAULT, .4f, false)
        val first = requireNotNull(session.nextCaptureSpec(null))
        assertEquals(first, session.nextCaptureSpec(null))
        session.record(photo("first", .4))
        val second = requireNotNull(session.nextCaptureSpec(null))
        assertFalse(first.captureId == second.captureId)
        assertEquals(first.batchId, second.batchId)
    }

    @Test fun `save retry retains pause until explicit continue and never recaptures source`() {
        val session = CreativeCaptureSession()
        session.begin(3, CreativeStyle.ORIGINAL, CameraUserSettings.DEFAULT, .4f, false)
        val first = session.nextCaptureSpec(null)
        session.markSourceCaptured()
        session.failBurst("disk full")
        assertFalse(session.continueRemaining())
        session.resumeBurstAfterRetry()
        assertEquals(first, session.nextCaptureSpec(null))
        session.record(photo("first", .4))
        assertTrue(session.paused)
        assertEquals(null, session.nextCaptureSpec(null))
        assertTrue(session.continueRemaining())
        assertEquals(2, session.nextCaptureSpec(null)?.sequence)
    }

    @Test fun `editing resets to the captured strength and retains per photo history`() {
        val session = session()
        session.begin(3, CreativeStyle.CLEAR_TRAVEL, CameraUserSettings.DEFAULT, .4f, false)
        session.record(photo("first", .4).copy(edit = EditAdjustment(styleStrength = .4f)))
        session.record(photo("second", .5).copy(edit = EditAdjustment(styleStrength = .6f)))
        assertEquals(.4f, session.resetEditing("first").edit.styleStrength)
        session.updateEdit(EditAdjustment(styleStrength = .8f))
        assertEquals(.6f, session.resetEditing("second").edit.styleStrength)
        assertEquals(.8f, session.resetEditing("first").edit.styleStrength)
        assertEquals(.4f, session.resetEdit().edit.styleStrength)
    }
    @Test
    fun `capture start freezes identity settings style and time for the batch`() {
        val session = session()
        session.begin(
            expectedCount = BurstSession.SHOT_COUNT,
            style = CreativeStyle.CLEAR_TRAVEL,
            settings = CameraUserSettings.DEFAULT.copy(
                saveStrategy = SaveStrategy.ORIGINAL_AND_EFFECT,
                derivativeQuality = DerivativeQuality.SPACE_SAVER,
            ),
            styleStrength = 0.65f,
            liveRequested = true,
        )

        val first = requireNotNull(session.nextCaptureSpec(null))
        assertEquals(CaptureId(FIXED_CAPTURE_ID), first.captureId)
        assertEquals(1, first.sequence)
        assertEquals(FIXED_TIME, first.takenAtMillis)
        assertEquals(CreativeStyle.CLEAR_TRAVEL, first.style)
        assertEquals(0.65f, first.edit.styleStrength)
        assertEquals(SaveStrategy.ORIGINAL_AND_EFFECT, first.saveStrategy)
        assertEquals(DerivativeQuality.SPACE_SAVER, first.derivativeQuality)
        assertTrue(first.livePhotoRequested)

        session.record(photo("first", 0.4))
        assertEquals(2, requireNotNull(session.nextCaptureSpec(null)).sequence)
    }

    @Test
    fun `burst keeps all photos and recommends the highest scored photo after explicit retry`() {
        val session = session()
        session.begin(BurstSession.SHOT_COUNT, CreativeStyle.ORIGINAL, CameraUserSettings.DEFAULT, 0f, false)

        session.record(photo("first", 0.4))
        session.failBurst("storage failed")
        session.resumeBurstAfterRetry()
        session.record(photo("best", 0.9))
        val last = session.record(photo("last", 0.6))

        assertTrue(session.isComplete)
        assertFalse(session.hasMoreShots)
        assertEquals(listOf(1, 2, 3), session.photos.map(CreativePhotoUi::sequence))
        assertEquals("best", session.recommendedId(last.id))
    }

    @Test
    fun `edit snapshot owns undo redo and reset flags`() {
        val session = session()
        session.begin(1, CreativeStyle.ORIGINAL, CameraUserSettings.DEFAULT, 0f, false)

        val changed = session.updateEdit(EditAdjustment(exposureStops = 0.4f))
        assertEquals(0.4f, changed.edit.exposureStops)
        assertTrue(changed.canUndo)
        assertTrue(changed.canReset)

        val undone = session.undoEdit()
        assertEquals(0f, undone.edit.exposureStops)
        assertTrue(undone.canRedo)

        val redone = session.redoEdit()
        assertEquals(0.4f, redone.edit.exposureStops)
        assertTrue(redone.canUndo)

        val reset = session.resetEdit()
        assertEquals(EditAdjustment(styleStrength = 0f), reset.edit)
        assertFalse(reset.canReset)
    }

    private fun session() = CreativeCaptureSession(
        captureIdFactory = { CaptureId(FIXED_CAPTURE_ID) },
        wallClockMillis = { FIXED_TIME },
    )

    private fun photo(id: String, total: Double) = CreativePhotoUi(
        id = id,
        captureId = FIXED_CAPTURE_ID,
        originalUri = "content://original/$id",
        displayUri = "content://display/$id",
        score = PhotoQualityScore(total, sharpness = total, exposure = total),
        sequence = 99,
        effectWasDownsampled = false,
    )

    private companion object {
        const val FIXED_CAPTURE_ID = "abc123def456"
        const val FIXED_TIME = 1_700_000_000_000L
    }
}
