package com.photocoach.app.camera

enum class SaveStage {
    SPACE_CHECK,
    MOTION_PACKAGE,
    ORIGINAL_PUBLISH,
    RECIPE_WRITE,
    DERIVATIVE_GENERATE,
    DERIVATIVE_PUBLISH,
    COMPLETE,
}

data class CaptureSaveProgress(val captureId: String, val snapshot: SaveSnapshot, val originalUri: String? = null,
    val captureReleased: Boolean = false)

data class SavePlan(
    val motionPhotoRequested: Boolean,
    val derivativeRequested: Boolean,
) {
    val stages: List<SaveStage> = buildList {
        add(SaveStage.SPACE_CHECK)
        if (motionPhotoRequested) add(SaveStage.MOTION_PACKAGE)
        add(SaveStage.ORIGINAL_PUBLISH)
        add(SaveStage.RECIPE_WRITE)
        if (derivativeRequested) {
            add(SaveStage.DERIVATIVE_GENERATE)
            add(SaveStage.DERIVATIVE_PUBLISH)
        }
        add(SaveStage.COMPLETE)
    }
}

data class SaveSnapshot(
    val plan: SavePlan,
    val completed: Set<SaveStage> = emptySet(),
    val failedStage: SaveStage? = null,
    val error: String? = null,
    val motionPhotoFallback: Boolean = false,
) {
    val nextStage: SaveStage?
        get() = failedStage ?: plan.stages.firstOrNull { it !in completed }

    val isComplete: Boolean get() = SaveStage.COMPLETE in completed
    val isPartialSuccess: Boolean get() = SaveStage.ORIGINAL_PUBLISH in completed && failedStage != null
}

class SaveCoordinator(initial: SaveSnapshot) {
    var snapshot: SaveSnapshot = initial
        private set

    constructor(plan: SavePlan) : this(SaveSnapshot(plan))

    fun complete(stage: SaveStage): SaveSnapshot {
        require(snapshot.failedStage == null) { "retry failed stage before continuing" }
        require(stage == snapshot.nextStage) { "expected ${snapshot.nextStage}, got $stage" }
        snapshot = snapshot.copy(completed = snapshot.completed + stage, error = null)
        return snapshot
    }

    fun fail(stage: SaveStage, error: String): SaveSnapshot {
        require(stage == snapshot.nextStage) { "only the active stage may fail" }
        snapshot = snapshot.copy(failedStage = stage, error = error)
        return snapshot
    }

    fun retryFailed(): SaveStage? {
        val failed = snapshot.failedStage ?: return null
        snapshot = snapshot.copy(failedStage = null, error = null)
        return failed
    }

    fun fallbackFromMotionPhoto(error: String): SaveSnapshot {
        require(snapshot.nextStage == SaveStage.MOTION_PACKAGE ||
            snapshot.nextStage == SaveStage.ORIGINAL_PUBLISH && SaveStage.ORIGINAL_PUBLISH !in snapshot.completed)
        val fallbackPlan = SavePlan(
            motionPhotoRequested = false,
            derivativeRequested = snapshot.plan.derivativeRequested,
        )
        snapshot = SaveSnapshot(
            plan = fallbackPlan,
            completed = snapshot.completed - SaveStage.MOTION_PACKAGE,
            error = error,
            motionPhotoFallback = true,
        )
        return snapshot
    }
}

data class SpaceEstimate(
    val sourceBytes: Long,
    val processingPeakBytes: Long,
    val derivativeBytes: Long,
    val safetyReserveBytes: Long = DEFAULT_SAFETY_RESERVE_BYTES,
) {
    init {
        require(sourceBytes >= 0 && processingPeakBytes >= 0 && derivativeBytes >= 0 && safetyReserveBytes >= 0)
    }

    val requiredBytes: Long
        get() = sourceBytes + processingPeakBytes + derivativeBytes + safetyReserveBytes

    fun fits(availableBytes: Long): Boolean = availableBytes >= requiredBytes

    companion object {
        const val DEFAULT_SAFETY_RESERVE_BYTES = 16L * 1024L * 1024L
    }
}

object SafeExifPolicy {
    val copiedTags: Set<String> = setOf(
        "Orientation",
        "ImageWidth",
        "ImageLength",
        "DateTime",
        "DateTimeOriginal",
        "DateTimeDigitized",
        "Make",
        "Model",
    )

    val blockedTagFamilies: Set<String> = setOf("GPS", "MakerNote", "Thumbnail", "UserComment")

    fun shouldCopy(tag: String): Boolean = tag in copiedTags
}
