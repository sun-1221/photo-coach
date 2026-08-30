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
        fun compute(image: ImageProxy): FrameStats {
            val yPlane = image.planes[0]
            val yBuffer = yPlane.buffer.duplicate()
            val rowStride = yPlane.rowStride
            val width = image.width
            val height = image.height
            val step = 8
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
            val midX = width / 2
            val topH = height / 3
            for (row in 0 until height step step) {
                val rowStart = row * rowStride
                var prev = -1
                for (col in 0 until width step step) {
                    val value = yBuffer.get(rowStart + col).toInt() and 0xFF
                    sum += value
                    sumSq += value * value
                    count++
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
                    val value = yBuffer.get(row * rowStride + col).toInt() and 0xFF
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
