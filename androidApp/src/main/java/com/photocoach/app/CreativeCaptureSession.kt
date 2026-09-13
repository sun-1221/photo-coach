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
    private var batchId: CaptureId? = null
    private var activeSpec: CaptureSpec? = null
    private var pendingSource = false
    var paused: Boolean = false
        private set
    private val editHistories = mutableMapOf<String, EditHistory>()
    private var captureTakenAtMillis = 0L
    private var liveRequested = false
    private var settings = CameraUserSettings.DEFAULT
    private var captureEdit = EditAdjustment()
    private var editHistory = EditHistory()

    val style: CreativeStyle get() = captureStyle
    val takenAtMillis: Long get() = captureTakenAtMillis
    val isBurst: Boolean get() = expectedCount == BurstSession.SHOT_COUNT
    val capturedCount: Int get() = capturedPhotos.size
    val sourceCount: Int get() = capturedCount + if (pendingSource) 1 else 0
    val remainingCount: Int get() = expectedCount - sourceCount
    val isComplete: Boolean get() = capturedCount >= expectedCount
    val hasMoreShots: Boolean get() = capturedCount < expectedCount
    val photos: List<CreativePhotoUi> get() = capturedPhotos.toList()
    val requestedLivePhoto: Boolean get() = liveRequested
    val activeCaptureId: String? get() = activeSpec?.captureId?.value

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
        batchId = CaptureIdentity.create()
        captureId = null
        activeSpec = null
        pendingSource = false
        paused = false
        captureTakenAtMillis = wallClockMillis()
        this.liveRequested = liveRequested
        capturedPhotos.clear()
        editHistory = EditHistory(captureEdit)
        editHistories.clear()
        burstSession.reset()
        if (isBurst) check(burstSession.start(userEnabledThreeShot = true))
    }

    fun nextCaptureSpec(portraitRegion: NormalizedFaceRegion?): CaptureSpec? {
        activeSpec?.let { return it }
        if (batchId == null || isComplete || paused) return null
        val activeId = captureIdFactory().also { captureId = it }
        return CaptureSpec(
            captureId = activeId,
            batchId = requireNotNull(batchId).value,
            holdBatchCapture = isBurst,
            sequence = capturedCount + 1,
            takenAtMillis = captureTakenAtMillis,
            style = captureStyle,
            edit = captureEdit,
            saveStrategy = settings.saveStrategy,
            derivativeQuality = settings.derivativeQuality,
            livePhotoRequested = liveRequested,
            portraitRegion = portraitRegion,
            beautyPreset = settings.beautyPreset,
        ).also { activeSpec = it }
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
            edit = photo.edit,
        ),
    )

    internal fun record(item: CreativePhotoUi): CreativePhotoUi {
        check(!isComplete) { "capture session already complete" }
        val sequence = capturedCount + 1
        val sequenced = item.copy(sequence = sequence)
        capturedPhotos += sequenced
        activeSpec = null
        pendingSource = false
        if (isBurst) burstSession.record(BurstPhoto(sequenced.id, sequenced.score, sequence))
        return sequenced
    }

    fun recommendedId(fallbackId: String): String =
        (burstSession.state as? BurstState.Complete)?.recommendedId ?: fallbackId

    fun resetEditing(photoId: String? = null): CreativeEditSnapshot {
        editHistory = if (photoId == null) EditHistory(captureEdit) else
            editHistories.getOrPut(photoId) { EditHistory(photos.first { it.id == photoId }.edit) }
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
        paused = true
        if (isBurst) burstSession.fail(message)
    }

    fun markSourceCaptured() { if (activeSpec != null) pendingSource = true }
    fun captureFailed() { activeSpec = null; pendingSource = false }
    fun owns(id: CaptureId): Boolean = activeSpec?.captureId == id
    fun continueRemaining(): Boolean {
        if (!paused || pendingSource || remainingCount <= 0) return false
        paused = false
        if (isBurst) burstSession.resumeAfterExplicitRetry()
        return true
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
