package com.photocoach.coach

/** Provisional clipping guard, not a sky detector or a calibrated aesthetic score. */
object ExposureGuidancePolicy {
    fun hasClipping(highlightRatio: Float?): Boolean =
        highlightRatio != null && highlightRatio.isFinite() && highlightRatio in 0.02f..1f

    fun shouldLower(highlightRatio: Float?, faceDarkerThanScene: Boolean?): Boolean =
        faceDarkerThanScene == false && hasClipping(highlightRatio)
}
