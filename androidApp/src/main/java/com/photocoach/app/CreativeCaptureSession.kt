package com.photocoach.app

import com.photocoach.app.camera.CameraUserSettings
import com.photocoach.app.camera.CapturedPhoto
import com.photocoach.app.camera.CaptureSpec
import com.photocoach.app.creative.BurstPhoto
import com.photocoach.app.creative.BurstSession
import com.photocoach.app.creative.BurstState
import com.photocoach.app.creative.CaptureId
import com.photocoach.app.creative.CaptureIdentity
import com.photocoach.app.creative.CreativeStyle
import com.photocoach.app.creative.EditAdjustment
import com.photocoach.app.creative.EditHistory
import com.photocoach.app.creative.NormalizedFaceRegion

internal data class CreativeEditSnapshot(
    val edit: EditAdjustment,
    val canUndo: Boolean,
    val canRedo: Boolean,
    val canReset: Boolean,
)

/** Owns the state that must remain stable for one explicit capture or burst. */
internal class CreativeCaptureSession(
    private val captureIdFactory: () -> CaptureId = CaptureIdentity::create,
    private val wallClockMillis: () -> Long = System::currentTimeMillis,
) {
    private val burstSession = BurstSession()
    private val capturedPhotos = mutableListOf<CreativePhotoUi>()
    private var expectedCount = 1
    private var captureStyle = CreativeStyle.ORIGINAL
    private var captureId: CaptureId? = null
    private var captureTakenAtMillis = 0L
    private var liveRequested = false
    private var settings = CameraUserSettings.DEFAULT
    private var captureEdit = EditAdjustment()
    private var editHistory = EditHistory()

    val style: CreativeStyle get() = captureStyle
    val takenAtMillis: Long get() = captureTakenAtMillis
    val isBurst: Boolean get() = expectedCount == BurstSession.SHOT_COUNT
    val capturedCount: Int get() = capturedPhotos.size
    val isComplete: Boolean get() = capturedCount >= expectedCount
    val hasMoreShots: Boolean get() = capturedCount < expectedCount
    val photos: List<CreativePhotoUi> get() = capturedPhotos.toList()
    val requestedLivePhoto: Boolean get() = liveRequested

    fun begin(
        expectedCount: Int,
        style: CreativeStyle,
        settings: CameraUserSettings,
        styleStrength: Float,
        liveRequested: Boolean,
    ) {
        require(expectedCount == 1 || expectedCount == BurstSession.SHOT_COUNT)
        this.expectedCount = expectedCount
        captureStyle = style
        this.settings = settings
        captureEdit = EditAdjustment(styleStrength = styleStrength)
        captureId = captureIdFactory()
        captureTakenAtMillis = wallClockMillis()
        this.liveRequested = liveRequested
        capturedPhotos.clear()
        editHistory = EditHistory()
        burstSession.reset()
        if (isBurst) check(burstSession.start(userEnabledThreeShot = true))
    }

    fun nextCaptureSpec(portraitRegion: NormalizedFaceRegion?): CaptureSpec? {
        val activeId = captureId ?: return null
        return CaptureSpec(
            captureId = activeId,
            sequence = capturedCount + 1,
            takenAtMillis = captureTakenAtMillis,
            style = captureStyle,
            edit = captureEdit,
            saveStrategy = settings.saveStrategy,
            derivativeQuality = settings.derivativeQuality,
            livePhotoRequested = liveRequested,
            portraitRegion = portraitRegion,
            beautyPreset = settings.beautyPreset,
        )
    }

    fun record(photo: CapturedPhoto): CreativePhotoUi = record(
        CreativePhotoUi(
            id = photo.originalUri.toString(),
            captureId = photo.captureId.value,
            originalUri = photo.originalUri.toString(),
            displayUri = photo.displayUri.toString(),
            score = photo.score,
            sequence = capturedCount + 1,
            effectWasDownsampled = photo.effectWasDownsampled,
            warning = photo.warning,
            isMotionPhoto = photo.isMotionPhoto,
            beautyPreset = photo.beautyPreset,
            beautyEngineVersion = photo.beautyEngineVersion,
        ),
    )

    internal fun record(item: CreativePhotoUi): CreativePhotoUi {
        check(!isComplete) { "capture session already complete" }
        val sequence = capturedCount + 1
        val sequenced = item.copy(sequence = sequence)
        capturedPhotos += sequenced
        if (isBurst) burstSession.record(BurstPhoto(sequenced.id, sequenced.score, sequence))
        return sequenced
    }

    fun recommendedId(fallbackId: String): String =
        (burstSession.state as? BurstState.Complete)?.recommendedId ?: fallbackId

    fun resetEditing(): CreativeEditSnapshot {
        editHistory = EditHistory()
        return editSnapshot()
    }

    fun updateEdit(edit: EditAdjustment): CreativeEditSnapshot {
        editHistory.update(edit)
        return editSnapshot()
    }

    fun undoEdit(): CreativeEditSnapshot {
        editHistory.undo()
        return editSnapshot()
    }

    fun redoEdit(): CreativeEditSnapshot {
        editHistory.redo()
        return editSnapshot()
    }

    fun resetEdit(): CreativeEditSnapshot {
        editHistory.reset()
        return editSnapshot()
    }

    fun failBurst(message: String) {
        if (isBurst) burstSession.fail(message)
    }

    fun resumeBurstAfterRetry() {
        if (isBurst) burstSession.resumeAfterExplicitRetry()
    }

    private fun editSnapshot() = CreativeEditSnapshot(
        edit = editHistory.current,
        canUndo = editHistory.canUndo,
        canRedo = editHistory.canRedo,
        canReset = editHistory.canReset,
    )
}
