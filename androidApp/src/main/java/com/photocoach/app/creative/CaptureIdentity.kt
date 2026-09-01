package com.photocoach.app.creative

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@JvmInline
value class CaptureId(val value: String) {
    init {
        require(value.matches(VALID_PATTERN)) { "invalid captureId" }
    }

    companion object {
        private val VALID_PATTERN = Regex("[a-z0-9]{12,40}")
    }
}

enum class CaptureAssetKind(val suffix: String) {
    ORIGINAL("ORIG"),
    EFFECT("EFFECT_SDR"),
    EDITED("EDIT_SDR"),
    RECIPE("RECIPE"),
}

object CaptureIdentity {
    fun create(): CaptureId = CaptureId(UUID.randomUUID().toString().replace("-", "").lowercase(Locale.US))

    fun displayName(
        captureId: CaptureId,
        kind: CaptureAssetKind,
        sequence: Int = 1,
        takenAtMillis: Long = System.currentTimeMillis(),
    ): String {
        require(sequence in 1..99)
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date(takenAtMillis))
        return "IMG_${stamp}_${captureId.value}_S${sequence.toString().padStart(2, '0')}_${kind.suffix}.JPG"
    }

    fun motionPhotoDisplayName(
        captureId: CaptureId,
        sequence: Int = 1,
        takenAtMillis: Long = System.currentTimeMillis(),
    ): String {
        require(sequence in 1..99)
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date(takenAtMillis))
        return "MVIMG_${stamp}_${captureId.value}_S${sequence.toString().padStart(2, '0')}MP.JPG"
    }

    fun recipeFileName(captureId: CaptureId, sequence: Int = 1): String {
        require(sequence in 1..99)
        return "${captureId.value}_S${sequence.toString().padStart(2, '0')}.json"
    }

    val motionPhotoNamePattern = Regex("^([^\\s/\\\\][^/\\\\]*MP)\\.(JPG|jpg|JPEG|jpeg)$")
}
