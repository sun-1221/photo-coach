package com.photocoach.app.creative

object ImageDecodePolicy {
    /** Matches the architecture's 12 MP export ceiling (about 48 MiB for one ARGB bitmap). */
    const val EXPORT_MAX_PIXELS = 12_000_000L

    /** Result-page source preview ceiling (about 8 MiB for one ARGB bitmap). */
    const val RESULT_PREVIEW_MAX_PIXELS = 2_000_000L

    /** Recent-photo and burst-thumbnail ceiling (about 1 MiB for one ARGB bitmap). */
    const val THUMBNAIL_MAX_PIXELS = 262_144L

    fun inSampleSize(width: Int, height: Int, maximumPixels: Long): Int {
        require(width > 0 && height > 0) { "image dimensions must be positive" }
        require(maximumPixels > 0) { "pixel limit must be positive" }
        var sample = 1
        while (decodedPixels(width, height, sample) > maximumPixels) sample *= 2
        return sample
    }

    fun decodedPixels(width: Int, height: Int, sampleSize: Int): Long {
        require(sampleSize > 0) { "sample size must be positive" }
        return (width.toLong() / sampleSize).coerceAtLeast(1L) *
            (height.toLong() / sampleSize).coerceAtLeast(1L)
    }
}
