package com.photocoach.app.analysis

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DeviceMotionStabilityTrackerTest {
    @Test fun `movement becomes unstable immediately and needs consecutive quiet samples to recover`() {
        val tracker = DeviceMotionStabilityTracker(gravityAlpha = 0.8f, movementThreshold = 0.75f, stableSamplesRequired = 3)
        assertTrue(tracker.update(0f, 9.8f, 0f))
        assertFalse(tracker.update(4f, 9.8f, 0f))
        assertFalse(tracker.update(0f, 9.8f, 0f))
        assertFalse(tracker.update(0f, 9.8f, 0f))
        assertTrue(tracker.update(0f, 9.8f, 0f))
    }
}
