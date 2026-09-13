package com.photocoach.app.camera

import com.photocoach.coach.SuggestedMode
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

data class QuickFocalPreset(
    val cameraId: String,
    val label: String,
    val relativeZoom: Float,
    val focalLengthMm: Float,
    val isDefault: Boolean,
    val verification: QuickFocalVerification = if (isDefault) {
        QuickFocalVerification.SAFE_DEFAULT
    } else {
        QuickFocalVerification.CALIBRATION_CANDIDATE
    },
) {
    val isTargetValidated: Boolean
        get() = verification == QuickFocalVerification.TARGET_VERIFIED

    val isQuickControlAvailable: Boolean
        get() = isDefault || isTargetValidated
}

enum class QuickFocalVerification {
    /** The default rear camera is the safe 1x fallback, not a calibrated optical claim. */
    SAFE_DEFAULT,

    /** Verified on Xiaomi 14 Pro with preview, captured field-of-view, EXIF, and quality checks. */
    TARGET_VERIFIED,

    /** Runtime metadata found a possible physical camera, but target-device calibration is still NotRun. */
    CALIBRATION_CANDIDATE,
}

fun interface TargetFocalVerifier {
    fun isVerified(candidate: CameraCandidate, relativeZoom: Float): Boolean

    companion object {
        val NONE = TargetFocalVerifier { _, _ -> false }
    }
}

data class CameraCandidate(
    val cameraId: String,
    val lensFacing: Int?,
    val intrinsicZoomRatio: Float,
    val focalLengthsMm: List<Float>,
)

data class ExposureCapability(
    val minimumStops: Float = 0f,
    val maximumStops: Float = 0f,
    val stepStops: Float = 0f,
) {
    val supported: Boolean get() = maximumStops > minimumStops && stepStops > 0f

    fun clamp(stops: Float): Float = if (supported) stops.coerceIn(minimumStops, maximumStops) else 0f
}

data class ZoomCapability(
    val minimumRatio: Float = 1f,
    val maximumRatio: Float = 1f,
) {
    val supported: Boolean get() = maximumRatio > minimumRatio
}

data class CameraCapabilities(
    val analysisSessionId: Long = 0L,
    val focalPresets: List<QuickFocalPreset> = emptyList(),
    val focalCandidates: List<QuickFocalPreset> = emptyList(),
    val selectedFocalId: String? = null,
    val availableModes: Set<SuggestedMode> = setOf(SuggestedMode.PHOTO),
    val activeMode: SuggestedMode = SuggestedMode.PHOTO,
    val exposure: ExposureCapability = ExposureCapability(),
    val zoom: ZoomCapability = ZoomCapability(),
    val extensionFallback: Boolean = false,
    val afLockSupported: Boolean = false,
    val aeLockSupported: Boolean = false,
    val livePhotoAvailable: Boolean = false,
    val livePhotoFallbackReason: String? = null,
) {
    val hasTelephotoPreset: Boolean
        get() = focalPresets.any { it.isTargetValidated && it.relativeZoom > 1.05f }

    val selectedFocal: QuickFocalPreset?
        get() = focalPresets.firstOrNull { it.cameraId == selectedFocalId }
            ?: focalPresets.firstOrNull { it.isDefault }
}

object QuickFocalPolicy {
    fun discover(
        candidates: List<CameraCandidate>,
        rearLensFacing: Int,
        targetVerifier: TargetFocalVerifier = TargetFocalVerifier.NONE,
    ): List<QuickFocalPreset> {
        val usable = candidates.filter { candidate ->
            candidate.lensFacing == rearLensFacing &&
                candidate.intrinsicZoomRatio.isFinite() && candidate.intrinsicZoomRatio > 0f &&
                candidate.focalLengthsMm.any { it.isFinite() && it > 0f }
        }
        val default = usable.minByOrNull { abs(it.intrinsicZoomRatio - 1f) } ?: return emptyList()
        val baseZoom = default.intrinsicZoomRatio
        return usable
            .map { candidate ->
                val relative = candidate.intrinsicZoomRatio / baseZoom
                QuickFocalPreset(
                    cameraId = candidate.cameraId,
                    label = formatZoom(relative),
                    relativeZoom = relative,
                    focalLengthMm = candidate.focalLengthsMm.minOrNull() ?: 0f,
                    isDefault = candidate.cameraId == default.cameraId,
                    verification = when {
                        candidate.cameraId == default.cameraId -> QuickFocalVerification.SAFE_DEFAULT
                        targetVerifier.isVerified(candidate, relative) -> QuickFocalVerification.TARGET_VERIFIED
                        else -> QuickFocalVerification.CALIBRATION_CANDIDATE
                    },
                )
            }
            .sortedBy(QuickFocalPreset::relativeZoom)
            .fold(emptyList()) { result, preset ->
                val duplicateIndex = result.indexOfFirst {
                    abs(it.relativeZoom - preset.relativeZoom) < DISTINCT_ZOOM_DELTA
                }
                when {
                    duplicateIndex < 0 -> result + preset
                    preset.isTargetValidated && !result[duplicateIndex].isTargetValidated ->
                        result.toMutableList().apply { this[duplicateIndex] = preset }
                    else -> result
                }
            }
    }

    fun quickControls(presets: List<QuickFocalPreset>): List<QuickFocalPreset> =
        presets.filter(QuickFocalPreset::isQuickControlAvailable)

    fun calibrationCandidates(presets: List<QuickFocalPreset>): List<QuickFocalPreset> =
        presets.filter { it.verification == QuickFocalVerification.CALIBRATION_CANDIDATE }

    fun safestSelection(presets: List<QuickFocalPreset>, requestedId: String?): QuickFocalPreset? {
        val controls = quickControls(presets)
        return controls.firstOrNull { it.cameraId == requestedId }
            ?: controls.firstOrNull { it.isDefault }
    }

    fun preferredTelephoto(presets: List<QuickFocalPreset>): QuickFocalPreset? =
        presets
            .filter { it.isTargetValidated && it.relativeZoom > 1.05f }
            .minByOrNull(QuickFocalPreset::relativeZoom)

    private fun formatZoom(ratio: Float): String {
        val tenths = (ratio * 10f).roundToInt() / 10f
        val text = if (abs(tenths - tenths.roundToInt()) < 0.05f) {
            tenths.roundToInt().toString()
        } else {
            String.format(Locale.US, "%.1f", tenths)
        }
        return "${text}×"
    }

    private const val DISTINCT_ZOOM_DELTA = 0.08f
}
