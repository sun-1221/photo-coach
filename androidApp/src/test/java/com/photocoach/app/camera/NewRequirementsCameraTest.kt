package com.photocoach.app.camera

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class NewRequirementsCameraTest {
    @TempDir lateinit var root: File
    @Test fun afSuccessDoesNotConfirmAeAndOldResultsCannotConfirmNewRequest() {
        val locks = LockConfirmation(); locks.reset(true, true)
        val first = locks.begin(true); locks.focus(first, true)
        assertEquals(LockStatus.CONFIRMED, locks.state.af)
        assertFalse(locks.state.bothConfirmed)
        locks.exposureApplied(first, true, 10)
        locks.result(first, 10, true, true, true); assertFalse(locks.state.bothConfirmed)
        locks.result(first, 11, true, true, true); assertTrue(locks.state.bothConfirmed)
        val release = locks.begin(false)
        locks.result(first, 12, true, true, true); assertFalse(locks.state.bothConfirmed)
        locks.focus(release, true); locks.exposureApplied(release, true, 12)
        locks.result(release, 13, false, false, false)
        assertEquals(CameraLockState(LockStatus.IDLE, LockStatus.IDLE), locks.state)
    }
    @Test fun unsupportedAndTimeoutNeverBecomeDoubleLock() {
        val locks = LockConfirmation(); locks.reset(true, false)
        val token = locks.begin(true); locks.focus(token, true); locks.timeout(token)
        assertEquals(LockStatus.UNSUPPORTED, locks.state.ae); assertFalse(locks.state.bothConfirmed)
        locks.reset(true, true); val next = locks.begin(true); locks.timeout(next)
        assertEquals(LockStatus.FAILED, locks.state.ae)
        locks.focus(token, true); assertEquals(LockStatus.FAILED, locks.state.af)
    }
    @Test fun emergencyStopsNewCaptureButKeepsCapturedSave() {
        assertTrue(ThermalPolicy.forLevel(ThermalLevel.CRITICAL).preserveShutter)
        for (level in listOf(ThermalLevel.EMERGENCY, ThermalLevel.SHUTDOWN)) {
            val p = ThermalPolicy.forLevel(level)
            assertFalse(p.preserveShutter); assertTrue(p.preserveCapturedOriginalSave)
            assertFalse(p.allowNewBurst); assertFalse(p.allowNewLive)
        }
        assertEquals(ThermalLevel.EMERGENCY, ThermalPolicy.fromPlatformStatus(5))
        assertEquals(ThermalLevel.SHUTDOWN, ThermalPolicy.fromPlatformStatus(6))
    }
    @Test fun storageRefusesQuotaWithoutDeletingOldSourceAndPreservesLegacyBytes() {
        val dir = File(root, "durable"); dir.mkdirs()
        val limits = PendingStorageLimits(100, 20, 40, 60, 10)
        val store = PendingSourceStore(dir, limits)
        val old = File(dir, "pending.jpg").apply { writeBytes(ByteArray(81) { 7 }) }
        assertThrows(java.io.IOException::class.java) { store.checkSpace(1000, false, false) }
        assertEquals(81L, old.length())
        old.delete()
        assertThrows(java.io.IOException::class.java) { store.checkSpace(29, false, false) }
        val legacy = File(root, "cache.jpg").apply { writeBytes(byteArrayOf(1, 2, 3, 4)) }
        val copied = store.preserveLegacy(legacy)
        assertArrayEquals(legacy.readBytes(), copied.readBytes()); assertTrue(legacy.exists())
        assertEquals(copied, store.preserveLegacy(copied))
    }
    @Test fun actualCaptureTimestampRequiresMediaAnchorAndMustBeInsideClip() {
        assertNull(MotionTimestampMapping.presentationUs(15_000, null, 0, 10_000))
        val anchor = MotionTimestampAnchor(10_000, 0)
        assertEquals(5_000L, MotionTimestampMapping.presentationUs(15_000, anchor, 0, 10_000))
        assertNull(MotionTimestampMapping.presentationUs(25_000, anchor, 0, 10_000))
    }
}
