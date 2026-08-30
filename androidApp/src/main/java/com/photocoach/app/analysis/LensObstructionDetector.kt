package com.photocoach.app.analysis

/**
 * Deliberately conservative: a single dark or low-detail frame is not enough to accuse the lens.
 * The user-facing copy also says "可能" because these signals cannot distinguish a cover from dirt.
 */
class LensObstructionDetector(
    private val requiredObscuredFrames: Int = 3,
    private val requiredClearFrames: Int = 2,
) {
    private var obscuredFrames = 0
    private var clearFrames = 0
    private var obscured = false

    fun update(stats: FrameStats): Boolean {
        val suspicious = stats.meanY < MAX_DARK_MEAN && stats.stdY < MAX_DARK_DEVIATION
        if (suspicious) {
            obscuredFrames++
            clearFrames = 0
            if (obscuredFrames >= requiredObscuredFrames) obscured = true
        } else {
            clearFrames++
            obscuredFrames = 0
            if (clearFrames >= requiredClearFrames) obscured = false
        }
        return obscured
    }

    private companion object {
        const val MAX_DARK_MEAN = 18f
        const val MAX_DARK_DEVIATION = 4f
    }
}
