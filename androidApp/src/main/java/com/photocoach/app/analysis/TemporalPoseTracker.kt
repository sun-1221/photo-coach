package com.photocoach.app.analysis

import kotlin.math.abs
import kotlin.math.hypot

internal data class PoseMotionSample(
    val timestampMs: Long,
    val centerX: Float,
    val centerY: Float,
    val leftAnkleY: Float,
    val rightAnkleY: Float,
    val referenceSize: Float,
)

internal data class TemporalPoseSignals(
    val motionObserved: Boolean = false,
    val walkingMotionStable: Boolean = false,
    val subjectMotionHigh: Boolean = false,
)

internal class TemporalPoseTracker(private val windowMs: Long = 1_200L) {
    private val samples = ArrayDeque<PoseMotionSample>()

    fun update(sample: PoseMotionSample?): TemporalPoseSignals {
        if (sample == null || sample.referenceSize <= 0f) {
            samples.clear()
            return TemporalPoseSignals()
        }
        samples.addLast(sample)
        while (samples.size > 1 && sample.timestampMs - samples.first().timestampMs > windowMs) samples.removeFirst()
        if (samples.size < 3) return TemporalPoseSignals()
        val first = samples.first()
        val last = samples.last()
        val displacement = hypot(last.centerX - first.centerX, last.centerY - first.centerY) / last.referenceSize
        val ankleSigns = samples.zipWithNext().map { (a, b) ->
            val before = a.leftAnkleY - a.rightAnkleY
            val after = b.leftAnkleY - b.rightAnkleY
            before * after < 0f && abs(after - before) / b.referenceSize >= MIN_ANKLE_CHANGE
        }
        return TemporalPoseSignals(
            motionObserved = true,
            walkingMotionStable = displacement >= MIN_WALK_DISPLACEMENT && ankleSigns.count { it } >= 1,
            subjectMotionHigh = displacement >= HIGH_MOTION_DISPLACEMENT,
        )
    }

    fun reset() = samples.clear()

    private companion object {
        const val MIN_WALK_DISPLACEMENT = 0.18f
        const val HIGH_MOTION_DISPLACEMENT = 0.35f
        const val MIN_ANKLE_CHANGE = 0.08f
    }
}
