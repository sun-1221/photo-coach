package com.photocoach.app.analysis

import com.photocoach.coach.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class QualitySignalPipelineTest {
    private fun measured(focus: Boolean = true, exposure: Boolean = true, visibility: Boolean = true,
        sensorAt: Long? = 1000, motionSamples: Int = 3, frameAt: Long = 1000): Signals {
        val af = FaceFocusSignalState().apply { if (focus) { onTap(true); onResult(true, 900) } }
        val motion = TemporalPoseTracker()
        var temporal = TemporalPoseSignals()
        repeat(motionSamples) { temporal = motion.update(PoseMotionSample(800L + it * 100, 50f, 50f, 0f, 0f, 20f)) }
        val device = DeviceMotionStabilityTracker()
        repeat(2) { device.update(0f, 9.8f, 0f) }
        val stable = device.update(0f, 9.8f, 0f)
        val luma = FaceLuminanceClassifier.measureDarkerThanBackground(
            LumaGrid(20, 20, 1, 0, ByteArray(400) { 110.toByte() }), LumaRegion(5f, 5f, 15f, 15f))
        return QualitySignalAssembler.assemble(Signals(faceCount = 1, faceReliable = true, faceRatio = .18f,
            focusOnFace = af.focusOnFace, faceDarkerThanScene = luma == true,
            tiltDegrees = displayRoll(0f, 9.8f, 0f, 0), handheldStable = stable,
            subjectMotionHigh = temporal.subjectMotionHigh), frameAt, 1000, visibility, exposure && luma != null,
            temporal.motionObserved, sensorAt.takeIf { device.isObserved }, af.observedAtMs)
    }
    private val output = CoachOutput(ShotIntent.CLOSE_UP, null, emptyList(),
        SceneStartParams(SuggestedMode.PHOTO, false, FocusTarget.FACE), OverlayHint(false))

    @Test fun measuredCompleteProductionSignalsReachReadyAndOptional() {
        val signals = measured()
        assertEquals(QualitySource.entries.toSet(), signals.qualityObservedAtMs.keys)
        val session = GuidanceSession().apply { onCameraReady(0) }
        session.onCandidates(output, signals, 1000)
        session.offerOptionalPose("肩放松一点", "half-shoulders")
        session.tick(1500)
        assertTrue((session.snapshot().stage as GuidanceStage.Ready).qualityConfirmed)
        assertTrue(session.requestOptional(1550))
    }
    @Test fun eachMissingMeasurementPreventsConfirmation() {
        for (signals in listOf(measured(focus = false), measured(exposure = false), measured(visibility = false),
            measured(sensorAt = null), measured(motionSamples = 1), measured(frameAt = -2000))) {
            assertFalse(signals.hasCurrentQualityEvidence(1500))
            val session = GuidanceSession().apply { onCameraReady(0) }
            session.onCandidates(output, signals, 1000)
            session.tick(1500)
            assertFalse((session.snapshot().stage as? GuidanceStage.Ready)?.qualityConfirmed == true)
        }
    }
    @Test fun staleSensorAndFocusStayUnknownEvenWithFreshFrames() {
        assertFalse(measured(sensorAt = -1000).hasCurrentQualityEvidence(1500))
        val current = measured()
        val later = QualitySignalAssembler.assemble(current, 10000, 10000, true, true, true, 10000, 900)
        assertFalse(later.hasCurrentQualityEvidence(10000))
    }
}
