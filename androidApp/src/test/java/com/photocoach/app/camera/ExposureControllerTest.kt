package com.photocoach.app.camera

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ExposureControllerTest {
    private val capability = ExposureCapability(-2f, 2f, 1f / 3f)
    @Test fun waitsForHardwareAndIgnoresReplacedRequest() {
        val controller = ExposureController()
        val callbacks = mutableListOf<(Result<Int>) -> Unit>()
        val indices = mutableListOf<Int>()
        val results = mutableListOf<Result<Float>>()
        val driver: (Int, (Result<Int>) -> Unit) -> Unit = { index, done -> indices += index; callbacks += done }
        controller.request(.6f, capability, driver) { results += it }
        controller.request(1f, capability, driver) { results += it }
        assertTrue(results.isEmpty())
        assertEquals(listOf(2, 3), indices)
        callbacks[0](Result.success(2))
        assertTrue(results.isEmpty())
        callbacks[1](Result.success(3))
        assertEquals(1f, results.single().getOrThrow())
        callbacks[0](Result.failure(IllegalStateException("cancelled")))
        assertEquals(1, results.size)
    }
    @Test fun failureIsNeverReportedAsApplied() {
        val results = mutableListOf<Result<Float>>()
        ExposureController().request(1f, capability, { _, done -> done(Result.failure(IllegalStateException("camera closed"))) }) { results += it }
        assertTrue(results.single().isFailure)
    }
    @Test fun rebindDiscardsOldCameraCompletion() {
        val controller = ExposureController()
        var done: ((Result<Int>) -> Unit)? = null
        val results = mutableListOf<Result<Float>>()
        controller.request(1f, capability, { _, callback -> done = callback }) { results += it }
        controller.invalidate()
        done!!(Result.success(3))
        assertTrue(results.isEmpty())
    }
    @Test fun clampsToHardwareRangeAndRejectsUnavailableCapability() {
        var requested = 0
        val controller = ExposureController()
        controller.request(99f, capability, { index, done -> requested = index; done(Result.success(index)) }) {
            assertEquals(2f, it.getOrThrow())
        }
        assertEquals(6, requested)
        controller.request(1f, ExposureCapability(), { _, _ -> fail<Unit>("must not call hardware") }) { assertTrue(it.isFailure) }
    }
}
