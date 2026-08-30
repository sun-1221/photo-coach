package com.photocoach.app.creative

data class BurstPhoto(
    val id: String,
    val score: PhotoQualityScore,
    val sequence: Int,
)

sealed interface BurstState {
    data object Idle : BurstState
    data class Capturing(val completed: List<BurstPhoto>) : BurstState
    data class Complete(val photos: List<BurstPhoto>, val recommendedId: String) : BurstState
    data class Failed(val completed: List<BurstPhoto>, val message: String) : BurstState
}

class BurstSession {
    var state: BurstState = BurstState.Idle
        private set

    fun start(userEnabledThreeShot: Boolean): Boolean {
        if (!userEnabledThreeShot || state is BurstState.Capturing) return false
        state = BurstState.Capturing(emptyList())
        return true
    }

    fun record(photo: BurstPhoto): BurstState {
        val active = state as? BurstState.Capturing ?: return state
        if (active.completed.size >= SHOT_COUNT) return state
        val photos = active.completed + photo.copy(sequence = active.completed.size + 1)
        state = if (photos.size == SHOT_COUNT) {
            val recommended = photos.sortedWith(
                compareByDescending<BurstPhoto> { it.score.total }
                    .thenByDescending { it.score.sharpness }
                    .thenByDescending { it.score.exposure }
                    .thenBy { it.sequence },
            ).first()
            BurstState.Complete(photos, recommended.id)
        } else {
            BurstState.Capturing(photos)
        }
        return state
    }

    fun fail(message: String): BurstState {
        val completed = (state as? BurstState.Capturing)?.completed.orEmpty()
        state = BurstState.Failed(completed, message)
        return state
    }

    fun resumeAfterExplicitRetry(): Boolean {
        val failed = state as? BurstState.Failed ?: return false
        state = BurstState.Capturing(failed.completed)
        return true
    }

    fun reset() {
        state = BurstState.Idle
    }

    companion object {
        const val SHOT_COUNT = 3
    }
}
