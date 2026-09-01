package com.photocoach.app.analysis

import androidx.camera.core.ImageProxy
import kotlin.math.abs
import kotlin.math.sqrt

data class FrameStats(
    val meanY: Float,
    val leftMean: Float,
    val rightMean: Float,
    val topMean: Float,
    val stdY: Float,
    val warmScore: Float,
    val verticalEnergy: Float,
    val horizontalEnergy: Float,
    val lumaGrid: LumaGrid? = null,
    val highlightRatio: Float = 0f,
) {
    val oneSideBrighter: Boolean get() = abs(leftMean - rightMean) > 18f
    val sceneBright: Boolean get() = meanY > 100f
    val harshLight: Boolean get() = stdY > 55f
    val softEvenLight: Boolean get() = stdY < 28f
    val warmColor: Boolean get() = warmScore > 8f
    val darkHandheld: Boolean get() = meanY < 40f
    val hasLargeArchitecture: Boolean get() = verticalEnergy > horizontalEnergy * 1.25f && verticalEnergy > 12f
    val hasHorizon: Boolean get() = horizontalEnergy > verticalEnergy * 1.15f && horizontalEnergy > 10f
    val hasGuidingLine: Boolean get() = (verticalEnergy + horizontalEnergy) > 22f && abs(verticalEnergy - horizontalEnergy) < 8f
    val mottledLight: Boolean get() = stdY in 28f..55f && sceneBright

    companion object {
        fun compute(image: ImageProxy, rotationDegrees: Int = 0): FrameStats {
            val yPlane = image.planes[0]
            val yBuffer = yPlane.buffer.duplicate()
            val rowStride = yPlane.rowStride
            val pixelStride = yPlane.pixelStride
            val width = image.width
            val height = image.height
            val step = 8
            val sampleColumns = (width + step - 1) / step
            val sampleRows = (height + step - 1) / step
            val lumaSamples = ByteArray(sampleColumns * sampleRows)
            var sampleIndex = 0
            var sum = 0L
            var sumSq = 0L
            var left = 0L
            var right = 0L
            var top = 0L
            var count = 0
            var leftCount = 0
            var rightCount = 0
            var topCount = 0
            var vert = 0L
            var hor = 0L
            var edgeCount = 0
            var highlightCount = 0
            val midX = width / 2
            val topH = height / 3
            for (row in 0 until height step step) {
                val rowStart = row * rowStride
                var prev = -1
                for (col in 0 until width step step) {
                    val value = yBuffer.get(rowStart + col * pixelStride).toInt() and 0xFF
                    lumaSamples[sampleIndex++] = value.toByte()
                    sum += value
                    sumSq += value * value
                    count++
                    if (value >= 245) highlightCount++
                    if (col < midX) {
                        left += value
                        leftCount++
                    } else {
                        right += value
                        rightCount++
                    }
                    if (row < topH) {
                        top += value
                        topCount++
                    }
                    if (prev >= 0) {
                        hor += abs(value - prev)
                        edgeCount++
                    }
                    prev = value
                }
            }
            for (col in 0 until width step step * 2) {
                var prev = -1
                for (row in 0 until height step step) {
                    val value = yBuffer.get(row * rowStride + col * pixelStride).toInt() and 0xFF
                    if (prev >= 0) vert += abs(value - prev)
                    prev = value
                }
            }
            val mean = if (count == 0) 0f else sum.toFloat() / count
            val variance = if (count == 0) 0f else (sumSq.toFloat() / count) - mean * mean
            val uvWarm = sampleWarm(image)
            return FrameStats(
                meanY = mean,
                leftMean = if (leftCount == 0) mean else left.toFloat() / leftCount,
                rightMean = if (rightCount == 0) mean else right.toFloat() / rightCount,
                topMean = if (topCount == 0) mean else top.toFloat() / topCount,
                stdY = sqrt(variance.coerceAtLeast(0f)),
                warmScore = uvWarm,
                verticalEnergy = if (edgeCount == 0) 0f else vert.toFloat() / (edgeCount + 1),
                horizontalEnergy = if (edgeCount == 0) 0f else hor.toFloat() / edgeCount,
                lumaGrid = LumaGrid(width, height, step, rotationDegrees, lumaSamples),
                highlightRatio = if (count == 0) 0f else highlightCount.toFloat() / count,
            )
        }

        private fun sampleWarm(image: ImageProxy): Float {
            if (image.planes.size < 3) return 0f
            val vPlane = image.planes[2]
            val uPlane = image.planes[1]
            val vBuf = vPlane.buffer.duplicate()
            val uBuf = uPlane.buffer.duplicate()
            if (!vBuf.hasRemaining() || !uBuf.hasRemaining()) return 0f
            var vSum = 0
            var uSum = 0
            var n = 0
            val step = 16
            for (i in 0 until vBuf.remaining() step step) {
                vSum += vBuf.get(i).toInt() and 0xFF
                n++
            }
            var un = 0
            for (i in 0 until uBuf.remaining() step step) {
                uSum += uBuf.get(i).toInt() and 0xFF
                un++
            }
            if (n == 0 || un == 0) return 0f
            return (vSum.toFloat() / n) - (uSum.toFloat() / un)
        }
    }
}

data class LumaRegion(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top

    fun inset(horizontalFraction: Float, verticalFraction: Float): LumaRegion {
        val dx = width * horizontalFraction
        val dy = height * verticalFraction
        return LumaRegion(left + dx, top + dy, right - dx, bottom - dy)
    }

    fun expand(fraction: Float): LumaRegion {
        val dx = width * fraction
        val dy = height * fraction
        return LumaRegion(left - dx, top - dy, right + dx, bottom + dy)
    }
}

class LumaGrid internal constructor(
    private val sourceWidth: Int,
    private val sourceHeight: Int,
    private val step: Int,
    rotationDegrees: Int,
    private val samples: ByteArray,
) {
    private val rotation = rotationDegrees.mod(360)
    private val sampleColumns = (sourceWidth + step - 1) / step
    private val sampleRows = (sourceHeight + step - 1) / step

    val width: Int = if (rotation == 90 || rotation == 270) sourceHeight else sourceWidth
    val height: Int = if (rotation == 90 || rotation == 270) sourceWidth else sourceHeight

    init {
        require(sourceWidth > 0 && sourceHeight > 0 && step > 0)
        require(rotation == 0 || rotation == 90 || rotation == 180 || rotation == 270)
        require(samples.size == sampleColumns * sampleRows)
    }

    fun mean(region: LumaRegion, minSamples: Int = 1): Float? =
        aggregate(region, includeInside = true, minSamples = minSamples)

    fun meanOutside(region: LumaRegion, minSamples: Int = 1): Float? =
        aggregate(region, includeInside = false, minSamples = minSamples)

    private fun aggregate(region: LumaRegion, includeInside: Boolean, minSamples: Int): Float? {
        if (
            !region.left.isFinite() || !region.top.isFinite() ||
            !region.right.isFinite() || !region.bottom.isFinite() ||
            region.width <= 0f || region.height <= 0f || minSamples <= 0
        ) {
            return null
        }
        val clippedLeft = region.left.coerceIn(0f, width.toFloat())
        val clippedTop = region.top.coerceIn(0f, height.toFloat())
        val clippedRight = region.right.coerceIn(0f, width.toFloat())
        val clippedBottom = region.bottom.coerceIn(0f, height.toFloat())
        val hasClippedArea = clippedRight > clippedLeft && clippedBottom > clippedTop
        if (includeInside && !hasClippedArea) return null
        var sum = 0L
        var count = 0
        for (sampleRow in 0 until sampleRows) {
            val sourceY = sampleRow * step
            for (sampleColumn in 0 until sampleColumns) {
                val sourceX = sampleColumn * step
                val (displayX, displayY) = when (rotation) {
                    90 -> sourceHeight - 1 - sourceY to sourceX
                    180 -> sourceWidth - 1 - sourceX to sourceHeight - 1 - sourceY
                    270 -> sourceY to sourceWidth - 1 - sourceX
                    else -> sourceX to sourceY
                }
                val inside = hasClippedArea &&
                    displayX >= clippedLeft && displayX < clippedRight &&
                    displayY >= clippedTop && displayY < clippedBottom
                if (inside == includeInside) {
                    sum += (samples[sampleRow * sampleColumns + sampleColumn].toInt() and 0xff)
                    count += 1
                }
            }
        }
        return if (count >= minSamples) sum.toFloat() / count else null
    }
}

internal object FaceLuminanceClassifier {
    private const val FACE_DARKER_DIFFERENCE = 18f
    private const val MIN_FACE_SAMPLES = 6
    private const val MIN_BACKGROUND_SAMPLES = 12

    fun isDarkerThanBackground(grid: LumaGrid, detectedFace: LumaRegion): Boolean {
        val faceCore = detectedFace.inset(horizontalFraction = 0.18f, verticalFraction = 0.2f)
        val excludedBackground = detectedFace.expand(0.12f)
        val faceMean = grid.mean(faceCore, MIN_FACE_SAMPLES) ?: return false
        val backgroundMean = grid.meanOutside(excludedBackground, MIN_BACKGROUND_SAMPLES) ?: return false
        return backgroundMean - faceMean >= FACE_DARKER_DIFFERENCE
    }
}
