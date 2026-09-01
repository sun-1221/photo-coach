package com.photocoach.app.analysis

import kotlin.math.sqrt

/**
 * Converts raw accelerometer samples into a conservative handheld-stability signal.
 * It removes gravity with a low-pass filter and requires consecutive quiet samples
 * before reporting stable again.
 */
internal class DeviceMotionStabilityTracker(
    private val gravityAlpha: Float = 0.8f,
    private val movementThreshold: Float = 0.75f,
    private val stableSamplesRequired: Int = 3,
) {
    private val gravity = FloatArray(3)
    private var initialized = false
    private var stableSamples = stableSamplesRequired

    fun update(x: Float, y: Float, z: Float): Boolean {
        val sample = floatArrayOf(x, y, z)
        if (!initialized) {
            sample.copyInto(gravity)
            initialized = true
            stableSamples = stableSamplesRequired
            return true
        }
        var residualSquared = 0f
        for (index in sample.indices) {
            gravity[index] = gravityAlpha * gravity[index] + (1f - gravityAlpha) * sample[index]
            val residual = sample[index] - gravity[index]
            residualSquared += residual * residual
        }
        if (sqrt(residualSquared) <= movementThreshold) {
            stableSamples = (stableSamples + 1).coerceAtMost(stableSamplesRequired)
        } else {
            stableSamples = 0
        }
        return stableSamples >= stableSamplesRequired
    }

    fun reset() {
        initialized = false
        stableSamples = stableSamplesRequired
        gravity.fill(0f)
    }
}
