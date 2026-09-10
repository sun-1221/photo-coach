package com.photocoach.coach

/** Runtime validity guards are provisional engineering defaults, not calibrated acceptance values. */
enum class QualitySource(val maximumAgeMs: Long) {
    PERSON(1500), GEOMETRY(1500), FACE_VISIBILITY(1500), EXPOSURE(1500),
    LEVEL(1500), DEVICE_MOTION(1500), SUBJECT_MOTION(1500), FOCUS(8000),
}

fun Signals.hasCurrentQualityEvidence(nowMs: Long): Boolean =
    if (qualityObservedAtMs.isEmpty()) qualityEvidenceComplete
    else QualitySource.entries.all { source ->
        qualityObservedAtMs[source]?.let { nowMs - it in 0..source.maximumAgeMs } == true
    }
