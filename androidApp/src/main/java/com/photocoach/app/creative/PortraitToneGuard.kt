package com.photocoach.app.creative

import kotlin.math.abs

data class NormalizedFaceRegion(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    init {
        require(listOf(left, top, right, bottom).all(Float::isFinite))
        require(left in 0f..1f && top in 0f..1f && right in 0f..1f && bottom in 0f..1f)
        require(right > left && bottom > top)
    }
}

data class FaceToneSample(val luma: Float, val hueDegrees: Float, val chroma: Float, val clippedRatio: Float) {
    init { require(listOf(luma, hueDegrees, chroma, clippedRatio).all(Float::isFinite)) }
}

sealed interface PortraitToneDecision {
    data class Conservative(val strength: Float, val reason: String?) : PortraitToneDecision
    data object Unknown : PortraitToneDecision
}

object PortraitToneGuard {
    fun constrain(style: CreativeStyle, requestedStrength: Float, faceCount: Int,
        reliableSingleFace: Boolean, before: FaceToneSample?, afterAtFullStrength: FaceToneSample?): PortraitToneDecision {
        if (faceCount != 1 || !reliableSingleFace || before == null || afterAtFullStrength == null) return PortraitToneDecision.Unknown
        val profile = StyleProfiles.forStyle(style)
        if (style == CreativeStyle.ORIGINAL) return PortraitToneDecision.Conservative(0f, null)
        var safe = requestedStrength.coerceIn(profile.uncalibratedSafeRange)
        val risk = abs(afterAtFullStrength.luma - before.luma) > MAX_LUMA_SHIFT ||
            hueDistance(before.hueDegrees, afterAtFullStrength.hueDegrees) > MAX_HUE_SHIFT ||
            abs(afterAtFullStrength.chroma - before.chroma) > MAX_CHROMA_SHIFT ||
            afterAtFullStrength.clippedRatio - before.clippedRatio > MAX_CLIP_INCREASE
        if (risk) safe = minOf(safe, RISK_STRENGTH_CAP)
        return PortraitToneDecision.Conservative(safe, if (safe < requestedStrength) "已使用人像保守强度" else null)
    }

    private fun hueDistance(first: Float, second: Float): Float {
        val distance = abs(first - second).mod(360f)
        return minOf(distance, 360f - distance)
    }

    private const val MAX_LUMA_SHIFT = 18f
    private const val MAX_HUE_SHIFT = 12f
    private const val MAX_CHROMA_SHIFT = 18f
    private const val MAX_CLIP_INCREASE = 0.04f
    private const val RISK_STRENGTH_CAP = 0.35f
}
