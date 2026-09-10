package com.photocoach.app.analysis

import kotlin.math.atan2
import kotlin.math.hypot

/** Rotation is Surface.ROTATION_* (0..3). Flat/invalid gravity has no observable roll. */
internal fun displayRoll(x: Float, y: Float, z: Float, rotation: Int): Float {
    if (!x.isFinite() || !y.isFinite() || !z.isFinite() || rotation !in 0..3) return Float.NaN
    if (hypot(x, y) < 1f) return Float.NaN
    val raw = Math.toDegrees(atan2(x, y).toDouble()).toFloat() - rotation * 90f
    return ((raw + 540f) % 360f) - 180f
}
