package com.photocoach.app.creative

import kotlin.math.pow

enum class CreativeStyle(
    val label: String,
    val description: String,
    val parameters: StyleParameters,
) {
    ORIGINAL("原图", "保留 CameraX 原始颜色", StyleParameters()),
    NATURAL_PORTRAIT("自然人像", "克制暖调与轻柔反差，不磨皮", StyleParameters(0.06f, 0.96f, 0.97f, 0.14f)),
    SOFT_LIGHT_PORTRAIT("柔光人像", "柔和高光并轻微褪色", StyleParameters(0.10f, 0.88f, 0.95f, 0.08f, fade = 0.20f)),
    COOL_PORTRAIT("清冷人像", "轻冷、低饱和，保持肤色可辨", StyleParameters(0.04f, 1.02f, 0.90f, -0.20f, 0.04f)),
    CLEAR_TRAVEL("清透旅行", "适度提亮并增加通透感", StyleParameters(0.12f, 1.08f, 1.10f, -0.03f)),
    FOREST_FRESH("森林清新", "轻微冷绿与柔和反差", StyleParameters(0.06f, 0.96f, 1.03f, -0.10f, -0.16f, 0.06f)),
    SUNSET_GOLD("日落暖金", "暖金高光与克制饱和", StyleParameters(0.02f, 1.04f, 1.08f, 0.42f, 0.05f)),
    WARM_FOOD("美食暖色", "轻暖、适度饱和与对比", StyleParameters(0.06f, 1.07f, 1.12f, 0.24f, 0.02f)),
    URBAN_COOL("都市冷调", "冷色、低饱和与清晰反差", StyleParameters(-0.02f, 1.12f, 0.86f, -0.28f, 0.03f)),
    NEON_NIGHT("夜色霓虹", "保留暗部并增强色彩反差", StyleParameters(-0.06f, 1.18f, 1.20f, -0.10f, 0.14f)),
    DOCUMENTARY_MONOCHROME("纪实黑白", "黑白并轻微增加对比", StyleParameters(contrast = 1.12f, saturation = 0f)),
    HIGH_CONTRAST_MONOCHROME("高反差黑白", "黑白并显著拉开明暗", StyleParameters(-0.02f, 1.32f, 0f)),
}

data class StyleParameters(
    val exposureStops: Float = 0f,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val temperature: Float = 0f,
    val tint: Float = 0f,
    val fade: Float = 0f,
)

data class EditAdjustment(
    val exposureStops: Float = 0f,
    val contrast: Float = 0f,
    val saturation: Float = 0f,
    val temperature: Float = 0f,
    val tint: Float = 0f,
    val fade: Float = 0f,
    val styleStrength: Float = 1f,
) {
    fun normalized(): EditAdjustment = copy(
        exposureStops = exposureStops.coerceIn(MIN_EXPOSURE, MAX_EXPOSURE),
        contrast = contrast.coerceIn(MIN_CONTRAST, MAX_CONTRAST),
        saturation = saturation.coerceIn(MIN_SATURATION, MAX_SATURATION),
        temperature = temperature.coerceIn(MIN_TEMPERATURE, MAX_TEMPERATURE),
        tint = tint.coerceIn(MIN_TINT, MAX_TINT),
        fade = fade.coerceIn(MIN_FADE, MAX_FADE),
        styleStrength = styleStrength.coerceIn(0f, 1f),
    )

    companion object {
        const val MIN_EXPOSURE = -1f
        const val MAX_EXPOSURE = 1f
        const val MIN_CONTRAST = -0.5f
        const val MAX_CONTRAST = 0.5f
        const val MIN_SATURATION = -1f
        const val MAX_SATURATION = 1f
        const val MIN_TEMPERATURE = -1f
        const val MAX_TEMPERATURE = 1f
        const val MIN_TINT = -1f
        const val MAX_TINT = 1f
        const val MIN_FADE = 0f
        const val MAX_FADE = 1f
    }
}

object CreativeColorMatrix {
    private val identity = floatArrayOf(
        1f, 0f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f, 0f,
        0f, 0f, 1f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f,
    )

    fun forSelection(style: CreativeStyle, edit: EditAdjustment = EditAdjustment()): FloatArray {
        val safe = edit.normalized()
        val strength = safe.styleStrength
        val preset = style.parameters
        val exposure = safe.exposureStops + preset.exposureStops * strength
        val contrast = (1f + safe.contrast + (preset.contrast - 1f) * strength).coerceIn(0.25f, 1.75f)
        val saturation = (1f + safe.saturation + (preset.saturation - 1f) * strength).coerceIn(0f, 2f)
        val temperature = (safe.temperature + preset.temperature * strength).coerceIn(-1f, 1f)
        val tint = (safe.tint + preset.tint * strength).coerceIn(-1f, 1f)
        val fade = (safe.fade + preset.fade * strength).coerceIn(0f, 1f)

        return multiply(
            fadeMatrix(fade),
            multiply(
                contrastMatrix(contrast),
                multiply(
                    saturationMatrix(saturation),
                    multiply(tintMatrix(tint), multiply(temperatureMatrix(temperature), exposureMatrix(exposure))),
                ),
            ),
        )
    }

    fun isIdentity(matrix: FloatArray, tolerance: Float = 0.0001f): Boolean =
        matrix.size == MATRIX_SIZE && matrix.indices.all { kotlin.math.abs(matrix[it] - identity[it]) <= tolerance }

    private fun exposureMatrix(stops: Float): FloatArray {
        val scale = 2f.pow(stops)
        return scaleMatrix(scale, scale, scale)
    }

    private fun temperatureMatrix(value: Float): FloatArray = scaleMatrix(
        1f + value * TEMPERATURE_SCALE,
        1f + value * TEMPERATURE_GREEN_SCALE,
        1f - value * TEMPERATURE_SCALE,
    )

    private fun tintMatrix(value: Float): FloatArray = scaleMatrix(
        1f + value * TINT_RED_BLUE_SCALE,
        1f - value * TINT_GREEN_SCALE,
        1f + value * TINT_RED_BLUE_SCALE,
    )

    private fun scaleMatrix(red: Float, green: Float, blue: Float): FloatArray = floatArrayOf(
        red, 0f, 0f, 0f, 0f,
        0f, green, 0f, 0f, 0f,
        0f, 0f, blue, 0f, 0f,
        0f, 0f, 0f, 1f, 0f,
    )

    private fun saturationMatrix(saturation: Float): FloatArray {
        val inverse = 1f - saturation
        val red = LUMA_RED * inverse
        val green = LUMA_GREEN * inverse
        val blue = LUMA_BLUE * inverse
        return floatArrayOf(
            red + saturation, green, blue, 0f, 0f,
            red, green + saturation, blue, 0f, 0f,
            red, green, blue + saturation, 0f, 0f,
            0f, 0f, 0f, 1f, 0f,
        )
    }

    private fun contrastMatrix(contrast: Float): FloatArray {
        val translation = 128f * (1f - contrast)
        return floatArrayOf(
            contrast, 0f, 0f, 0f, translation,
            0f, contrast, 0f, 0f, translation,
            0f, 0f, contrast, 0f, translation,
            0f, 0f, 0f, 1f, 0f,
        )
    }

    private fun fadeMatrix(fade: Float): FloatArray {
        val scale = 1f - fade * FADE_CONTRAST_REDUCTION
        val translation = 255f * fade * FADE_BLACK_LIFT
        return floatArrayOf(
            scale, 0f, 0f, 0f, translation,
            0f, scale, 0f, 0f, translation,
            0f, 0f, scale, 0f, translation,
            0f, 0f, 0f, 1f, 0f,
        )
    }

    /** Multiplies affine 4x5 color matrices so that [right] is applied first. */
    private fun multiply(left: FloatArray, right: FloatArray): FloatArray {
        require(left.size == MATRIX_SIZE && right.size == MATRIX_SIZE)
        val result = FloatArray(MATRIX_SIZE)
        for (row in 0 until 4) {
            for (column in 0 until 5) {
                result[row * 5 + column] = if (column == 4) {
                    left[row * 5 + 4] + (0 until 4).sumOf { index ->
                        (left[row * 5 + index] * right[index * 5 + 4]).toDouble()
                    }.toFloat()
                } else {
                    (0 until 4).sumOf { index ->
                        (left[row * 5 + index] * right[index * 5 + column]).toDouble()
                    }.toFloat()
                }
            }
        }
        return result
    }

    private const val MATRIX_SIZE = 20
    private const val LUMA_RED = 0.213f
    private const val LUMA_GREEN = 0.715f
    private const val LUMA_BLUE = 0.072f
    private const val TEMPERATURE_SCALE = 0.12f
    private const val TEMPERATURE_GREEN_SCALE = 0.02f
    private const val TINT_RED_BLUE_SCALE = 0.04f
    private const val TINT_GREEN_SCALE = 0.08f
    private const val FADE_CONTRAST_REDUCTION = 0.22f
    private const val FADE_BLACK_LIFT = 0.10f
}
