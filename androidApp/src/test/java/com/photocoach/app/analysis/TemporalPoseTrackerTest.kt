package com.photocoach.app.analysis

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TemporalPoseTrackerTest {
    @Test fun `alternating ankles plus displacement produces walking evidence`() {
        val tracker = TemporalPoseTracker()
        assertFalse(tracker.update(sample(0, 0f, -10f)).walkingMotionStable)
        assertFalse(tracker.update(sample(400, 12f, 10f)).walkingMotionStable)
        assertTrue(tracker.update(sample(800, 24f, -10f)).walkingMotionStable)
    }

    @Test fun `single frame and missing evidence never invent motion`() {
        val tracker = TemporalPoseTracker()
        assertFalse(tracker.update(sample(0, 0f, 0f)).subjectMotionHigh)
        assertFalse(tracker.update(null).walkingMotionStable)
    }

    private fun sample(time: Long, x: Float, ankleDifference: Float) = PoseMotionSample(
        timestampMs = time,
        centerX = x,
        centerY = 100f,
        leftAnkleY = 500f + ankleDifference / 2f,
        rightAnkleY = 500f - ankleDifference / 2f,
        referenceSize = 100f,
    )
}
