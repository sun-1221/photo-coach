package com.photocoach.app.camera

import android.content.Context
import android.os.Build
import android.os.PowerManager
import androidx.core.content.ContextCompat

enum class ThermalLevel { NORMAL, LIGHT, MODERATE, SEVERE, CRITICAL, UNKNOWN }

data class ThermalLoadPolicy(
    val analysisIntervalMs: Long,
    val poseAndBackgroundEnabled: Boolean,
    val stylePreviewEnabled: Boolean,
    val preferSdLive: Boolean,
    val allowNewBurst: Boolean,
    val allowNewLive: Boolean,
    val allowCreativeExport: Boolean,
    val preservePreview: Boolean = true,
    val preserveShutter: Boolean = true,
    val preserveCapturedOriginalSave: Boolean = true,
)

object ThermalPolicy {
    fun forLevel(level: ThermalLevel): ThermalLoadPolicy = when (level) {
        ThermalLevel.NORMAL -> ThermalLoadPolicy(0, true, true, false, true, true, true)
        ThermalLevel.LIGHT -> ThermalLoadPolicy(150, true, false, true, true, true, true)
        ThermalLevel.MODERATE -> ThermalLoadPolicy(300, false, false, true, true, true, true)
        ThermalLevel.SEVERE -> ThermalLoadPolicy(600, false, false, true, false, false, true)
        ThermalLevel.CRITICAL -> ThermalLoadPolicy(1_000, false, false, true, false, false, false)
        ThermalLevel.UNKNOWN -> ThermalLoadPolicy(300, false, false, true, false, false, true)
    }

    fun fromPlatformStatus(status: Int): ThermalLevel = when (status) {
        PowerManager.THERMAL_STATUS_NONE -> ThermalLevel.NORMAL
        PowerManager.THERMAL_STATUS_LIGHT -> ThermalLevel.LIGHT
        PowerManager.THERMAL_STATUS_MODERATE -> ThermalLevel.MODERATE
        PowerManager.THERMAL_STATUS_SEVERE -> ThermalLevel.SEVERE
        PowerManager.THERMAL_STATUS_CRITICAL,
        PowerManager.THERMAL_STATUS_EMERGENCY,
        PowerManager.THERMAL_STATUS_SHUTDOWN -> ThermalLevel.CRITICAL
        else -> ThermalLevel.UNKNOWN
    }
}

class ThermalStateMonitor(context: Context, private val onChanged: (ThermalLevel) -> Unit) {
    private val powerManager = context.getSystemService(PowerManager::class.java)
    private val mainExecutor = ContextCompat.getMainExecutor(context)
    // Created only from the API 29+ guarded paths, never during construction on API 26/27.
    private val listener by lazy {
        PowerManager.OnThermalStatusChangedListener { onChanged(ThermalPolicy.fromPlatformStatus(it)) }
    }
    private var registered = false

    fun start() {
        if (registered) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && powerManager != null) {
            registered = true
            onChanged(ThermalPolicy.fromPlatformStatus(powerManager.currentThermalStatus))
            powerManager.addThermalStatusListener(mainExecutor, listener)
        } else onChanged(ThermalLevel.UNKNOWN)
    }

    fun close() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && registered) powerManager?.removeThermalStatusListener(listener)
        registered = false
    }
}
