package com.photocoach.app.analysis

import com.photocoach.coach.QualitySource
import com.photocoach.coach.Signals
import com.photocoach.coach.hasCurrentQualityEvidence

/** Shared by the actual analyzer and JVM signal-pipeline regressions; no synthetic pass default. */
internal object QualitySignalAssembler {
    fun assemble(signals: Signals, frameAtMs: Long, nowMs: Long,
        faceVisibilityKnown: Boolean, exposureKnown: Boolean, subjectMotionKnown: Boolean,
        sensorAtMs: Long?, focusAtMs: Long?): Signals {
        val observed = buildMap {
            if (signals.faceReliable && signals.faceCount == 1) put(QualitySource.PERSON, frameAtMs)
            if (signals.faceReliable && signals.faceRatio.isFinite() && signals.faceRatio > 0f) put(QualitySource.GEOMETRY, frameAtMs)
            if (faceVisibilityKnown) put(QualitySource.FACE_VISIBILITY, frameAtMs)
            if (exposureKnown) put(QualitySource.EXPOSURE, frameAtMs)
            if (subjectMotionKnown) put(QualitySource.SUBJECT_MOTION, frameAtMs)
            if (signals.tiltDegrees.isFinite() && sensorAtMs != null) put(QualitySource.LEVEL, sensorAtMs)
            if (sensorAtMs != null) put(QualitySource.DEVICE_MOTION, sensorAtMs)
            if (focusAtMs != null) put(QualitySource.FOCUS, focusAtMs)
        }
        val result = signals.copy(qualityObservedAtMs = observed, qualityEvidenceComplete = false, observedAtMs = frameAtMs)
        return result.copy(qualityEvidenceComplete = result.hasCurrentQualityEvidence(nowMs))
    }
}
