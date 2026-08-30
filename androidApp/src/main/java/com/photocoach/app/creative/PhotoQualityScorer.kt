package com.photocoach.app.creative

import kotlin.math.abs
import kotlin.math.sqrt

data class LuminanceFrame(
    val width: Int,
    val height: Int,
    val values: IntArray,
) {
    init {
        require(width >= 3 && height >= 3)
        require(values.size == width * height)
        require(values.all { it in 0..255 })
    }
}

data class PhotoQualityScore(
    val total: Double,
    val sharpness: Double,
    val exposure: Double,
) {
    fun reasonComparedWith(other: PhotoQualityScore?): String = when {
        other == null -> "清晰度与曝光更均衡"
        sharpness > other.sharpness * 1.08 -> "清晰度更好"
        exposure > other.exposure + 0.04 -> "曝光更稳"
        else -> "清晰度与曝光更均衡"
    }
}

object PhotoQualityScorer {
    fun score(frame: LuminanceFrame): PhotoQualityScore {
        val sharpness = normalizedLaplacianVariance(frame)
        val exposure = exposureFitness(frame.values)
        return PhotoQualityScore(
            total = sharpness * SHARPNESS_WEIGHT + exposure * EXPOSURE_WEIGHT,
            sharpness = sharpness,
            exposure = exposure,
        )
    }

    private fun normalizedLaplacianVariance(frame: LuminanceFrame): Double {
        val samples = ArrayList<Double>((frame.width - 2) * (frame.height - 2))
        for (y in 1 until frame.height - 1) {
            for (x in 1 until frame.width - 1) {
                val center = frame.values[y * frame.width + x]
                val laplacian = frame.values[(y - 1) * frame.width + x] +
                    frame.values[(y + 1) * frame.width + x] +
                    frame.values[y * frame.width + x - 1] +
                    frame.values[y * frame.width + x + 1] -
                    4 * center
                samples += laplacian.toDouble()
            }
        }
        if (samples.isEmpty()) return 0.0
        val mean = samples.average()
        val variance = samples.sumOf { (it - mean) * (it - mean) } / samples.size
        return (sqrt(variance) / LAPLACIAN_NORMALIZER).coerceIn(0.0, 1.0)
    }

    private fun exposureFitness(values: IntArray): Double {
        val mean = values.average()
        val meanFitness = (1.0 - abs(mean - TARGET_LUMA) / TARGET_LUMA).coerceIn(0.0, 1.0)
        val clipped = values.count { it <= SHADOW_CLIP || it >= HIGHLIGHT_CLIP }.toDouble() / values.size
        return (meanFitness * (1.0 - clipped)).coerceIn(0.0, 1.0)
    }

    private const val SHARPNESS_WEIGHT = 0.65
    private const val EXPOSURE_WEIGHT = 0.35
    private const val LAPLACIAN_NORMALIZER = 96.0
    private const val TARGET_LUMA = 128.0
    private const val SHADOW_CLIP = 8
    private const val HIGHLIGHT_CLIP = 247
}
