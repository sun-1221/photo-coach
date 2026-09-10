package com.photocoach.app.camera

import kotlin.math.roundToInt

/** Serial request ownership; a replaced request or camera session cannot publish stale UI state. */
internal class ExposureController {
    private var generation = 0L
    fun invalidate() { generation++ }

    fun request(stops: Float, capability: ExposureCapability,
        apply: (Int, (Result<Int>) -> Unit) -> Unit, onResult: (Result<Float>) -> Unit) {
        val token = ++generation
        if (!stops.isFinite() || !capability.supported) {
            onResult(Result.failure(IllegalArgumentException("当前相机无法调整曝光")))
            return
        }
        val step = capability.stepStops
        val index = (capability.clamp(stops) / step).roundToInt()
        var delivered = false
        val complete: (Result<Int>) -> Unit = { result ->
            if (token == generation && !delivered) {
                delivered = true
                onResult(result.map { it * step })
            }
        }
        try { apply(index, complete) } catch (error: Exception) { complete(Result.failure(error)) }
    }
}
