package com.photocoach.app.camera

import android.annotation.SuppressLint
import android.hardware.camera2.CameraCharacteristics
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import kotlin.math.abs

internal data class DiscoveredCameras(
    val presets: List<QuickFocalPreset>,
    val cameraInfoById: Map<String, CameraInfo>,
)

// Camera2 metadata is deliberately isolated here; callers only receive stable project models.
@SuppressLint("UnsafeOptInUsageError")
internal object CameraCapabilityDiscovery {
    fun discoverRearCameras(
        provider: ProcessCameraProvider,
        targetVerifier: TargetFocalVerifier = TargetFocalVerifier.NONE,
    ): DiscoveredCameras {
        val infos = provider.availableCameraInfos.filter { it.lensFacing == CameraSelector.LENS_FACING_BACK }
        val idToInfo = buildMap {
            infos.forEach { info ->
                runCatching { Camera2CameraInfo.from(info).cameraId }
                    .getOrNull()
                    ?.let { put(it, info) }
            }
        }
        val candidates = infos.mapNotNull { info ->
            runCatching {
                val camera2 = Camera2CameraInfo.from(info)
                CameraCandidate(
                    cameraId = camera2.cameraId,
                    lensFacing = info.lensFacing,
                    intrinsicZoomRatio = info.intrinsicZoomRatio,
                    focalLengthsMm = camera2
                        .getCameraCharacteristic(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
                        ?.filter { it.isFinite() && it > 0f }
                        .orEmpty(),
                )
            }.getOrNull()
        }
        val discovered = QuickFocalPolicy.discover(
            candidates,
            CameraSelector.LENS_FACING_BACK,
            targetVerifier,
        )
        if (discovered.isNotEmpty()) return DiscoveredCameras(discovered, idToInfo)

        val fallbackInfo = infos.minByOrNull { abs(it.intrinsicZoomRatio - 1f) }
            ?: return DiscoveredCameras(emptyList(), idToInfo)
        val fallbackId = runCatching { Camera2CameraInfo.from(fallbackInfo).cameraId }.getOrNull()
            ?: return DiscoveredCameras(emptyList(), idToInfo)
        return DiscoveredCameras(
            presets = listOf(
                QuickFocalPreset(
                    cameraId = fallbackId,
                    label = "1×",
                    relativeZoom = 1f,
                    focalLengthMm = 0f,
                    isDefault = true,
                    verification = QuickFocalVerification.SAFE_DEFAULT,
                ),
            ),
            cameraInfoById = idToInfo + (fallbackId to fallbackInfo),
        )
    }

    fun selectorFor(info: CameraInfo): CameraSelector = CameraSelector.Builder()
        .addCameraFilter { cameras -> cameras.filter { it == info } }
        .build()

    fun fallbackRearSelector(): CameraSelector = CameraSelector.Builder()
        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
        .build()
}
