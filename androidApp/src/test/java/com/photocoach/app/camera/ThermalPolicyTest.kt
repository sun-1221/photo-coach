package com.photocoach.app.camera

import android.os.PowerManager
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ThermalPolicyTest {
    @Test fun `load sheds progressively without removing core camera guarantees`() {
        val levels = listOf(ThermalLevel.NORMAL, ThermalLevel.LIGHT, ThermalLevel.MODERATE, ThermalLevel.SEVERE, ThermalLevel.CRITICAL)
        val policies = levels.map(ThermalPolicy::forLevel)
        assertTrue(policies.zipWithNext().all { (before, after) -> before.analysisIntervalMs <= after.analysisIntervalMs })
        assertTrue(policies.all { it.preservePreview && it.preserveShutter && it.preserveCapturedOriginalSave })
        assertFalse(ThermalPolicy.forLevel(ThermalLevel.SEVERE).allowNewLive)
        assertFalse(ThermalPolicy.forLevel(ThermalLevel.SEVERE).allowNewBurst)
        assertFalse(ThermalPolicy.forLevel(ThermalLevel.CRITICAL).allowCreativeExport)
    }

    @Test fun `unknown status is conservative rather than reported normal`() {
        assertTrue(ThermalPolicy.fromPlatformStatus(PowerManager.THERMAL_STATUS_NONE) == ThermalLevel.NORMAL)
        assertTrue(ThermalPolicy.fromPlatformStatus(Int.MAX_VALUE) == ThermalLevel.UNKNOWN)
        assertFalse(ThermalPolicy.forLevel(ThermalLevel.UNKNOWN).allowNewLive)
    }
}
