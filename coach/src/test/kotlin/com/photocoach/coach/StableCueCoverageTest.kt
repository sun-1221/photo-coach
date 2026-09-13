package com.photocoach.coach

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class StableCueCoverageTest {
    private val neutral = Signals(faceCount=1,faceRatio=.2f,faceReliable=true,poseReliable=true,poseAvailable=true,
        atLeastOneHandOutsideTorso=true,torsoUpright=true)
    private data class Case(val id:CueId,val signal:Signals,val intent:ShotIntent=ShotIntent.CLOSE_UP)
    private val cases get() = listOf(
        Case(CueId.FIND_PERSON,Signals()),Case(CueId.CLEAN_LENS,neutral.copy(lensObscured=true)),
        Case(CueId.KEEP_SUBJECT_IN_FRAME,neutral.copy(subjectCutOff=true)),Case(CueId.FOCUS_FACE,neutral.copy(focusOnFace=false)),
        Case(CueId.MOVE_CLOSER,neutral.copy(faceRatio=.03f)),Case(CueId.MOVE_CLOSER_KEEP_SCENERY,neutral.copy(faceRatio=.03f),ShotIntent.PERSON_WITH_SCENERY),
        Case(CueId.LEVEL_PHONE,neutral.copy(tiltDegrees=6f)),Case(CueId.PLACE_ON_THIRDS,neutral.copy(personCentered=true),ShotIntent.PERSON_WITH_SCENERY),
        Case(CueId.PLACE_FACE_ON_UPPER_THIRD,neutral.copy(faceTooLowInFrame=true)),Case(CueId.LOWER_EXPOSURE,neutral.copy(skyOverexposed=true)),
        Case(CueId.TURN_FACE_TO_CAMERA,neutral.copy(faceTurnedAway=true)),Case(CueId.OPEN_EYES,neutral.copy(eyesLikelyClosed=true)),
        Case(CueId.CHIN_DOWN,neutral.copy(headTiltedBack=true)),Case(CueId.ANGLE_BODY,neutral.copy(shouldersSquare=true)),
        Case(CueId.RELAX_SHOULDERS,neutral.copy(shouldersRaised=true)),Case(CueId.REST_HANDS,neutral.copy(handsIdle=true)),
        Case(CueId.MOVE_TO_WINDOW,neutral.copy(coarseScene=CoarseScene.INDOOR,oneSideBrighter=true)),
        Case(CueId.TURN_TO_WINDOW,neutral.copy(coarseScene=CoarseScene.INDOOR,oneSideBrighter=true,shouldersSquare=true)),
        Case(CueId.STEP_SIDEWAYS,neutral.copy(faceDarkerThanScene=true)),
        Case(CueId.ANGLE_BODY_BACKLIT,neutral.copy(faceDarkerThanScene=true,shouldersSquare=true)))
    @Test fun everyCatalogIdHasAReachableCaseOrExplicitDormantBoundary() {
        val catalog=ScenesLoader.loadFromClasspath()
        val ids=(catalog.coreCues+catalog.scenes.flatMap {it.shooterCues+it.subjectCues}).map {it.id}.toSet()
        assertEquals(ids,cases.map {it.id}.toSet()+CueId.LOOK_AT_LANDMARK)
        // This catalog phrase is deliberately not selected automatically: no landmark/gaze detector exists.
        for(intent in ShotIntent.entries) for(case in cases)
            assertFalse(CoachEngine.loadDefault().evaluate(case.signal,intent).cues.any {it.id==CueId.LOOK_AT_LANDMARK})
    }
    @TestFactory fun enabledCatalogTriggerExitAndUnknown()=cases.map { case -> DynamicTest.dynamicTest("catalog-${case.id}-trigger-exit-unknown") {
        val engine=CoachEngine.loadDefault()
        assertTrue(engine.evaluate(case.signal,case.intent).cues.any {it.id==case.id})
        assertFalse(engine.evaluate(neutral,case.intent).cues.any {it.id==case.id})
        val unknown=engine.evaluate(Signals(),case.intent).cues
        assertEquals(listOf(CueId.FIND_PERSON),unknown.map {it.id})
        val multi=engine.evaluate(neutral.copy(faceCount=2),case.intent).cues
        assertFalse(multi.any {it.audience==Audience.SUBJECT})
    }}
    private val poseInputs get()=mapOf(
        "close-face" to (neutral.copy(faceTurnedAway=true) to neutral),
        "close-eyes" to (neutral.copy(eyesLikelyClosed=true) to neutral),
        "half-shoulders" to (neutral.copy(shouldersRaised=true) to neutral),
        "half-hand" to (neutral.copy(handsIdle=true,atLeastOneHandOutsideTorso=false) to neutral),
        "half-angle" to (neutral.copy(shouldersSquare=true) to neutral),
        "full-feet" to (neutral.copy(anklesVisible=true,anklesNearBottomEdge=true) to neutral.copy(anklesVisible=true)),
        "full-joints" to (neutral.copy(jointsNearFrameEdge=true) to neutral),
        "full-step" to (neutral to neutral),
        "seated-upright" to (neutral.copy(seatedCandidate=true,torsoUpright=false) to neutral.copy(seatedCandidate=true)),
        "seated-chair" to (neutral to neutral),
        "walking-step" to (neutral.copy(walkingCandidate=true) to neutral.copy(walkingCandidate=true,walkingMotionStable=true)),
        "solo-interaction" to (neutral to neutral))
    @TestFactory fun everyPoseRunsReducerAndWithdrawsUnknownAndMultiplePeople()=PoseCueCatalog.cues.map { cue ->
        DynamicTest.dynamicTest("pose-${cue.id}-entry-exit-invalid-unknown") {
            val (trigger,improved)=poseInputs.getValue(cue.id)
            fun active():PoseGuidanceReducer {
                val r=PoseGuidanceReducer();r.select(cue.category,0)
                r.update(trigger,0);val eligible=r.update(trigger,600) as PoseGuidanceState.Eligible
                assertEquals(cue.id,eligible.cue.id)
                assertTrue(r.update(trigger,601) is PoseGuidanceState.CueActive);return r
            }
            val r=active()
            if(cue.completion==PoseCompletion.OBSERVABLE){r.update(improved,700);assertTrue(r.update(improved,1200) is PoseGuidanceState.Satisfied)}
            else {assertFalse(cue.isSatisfied(improved));assertTrue(r.update(improved,700) is PoseGuidanceState.CueActive)
                assertTrue(r.update(improved,601+cue.timeoutMs) is PoseGuidanceState.Satisfied)}
            assertTrue(active().update(Signals(),602) is PoseGuidanceState.LowConfidence)
            assertTrue(active().update(trigger.copy(faceCount=2),602) is PoseGuidanceState.MultiPersonSuppressed)
            assertTrue(active().update(trigger,601+cue.timeoutMs) is PoseGuidanceState.Satisfied)
        }
    }
    @TestFactory fun techniqueIdsHavePositiveNegativeAndUnknownInputs():List<DynamicTest> {
        val inputs=listOf(CueId.P1_FRAME to neutral.copy(subjectCutOff=true),CueId.P1_JOINTS to neutral.copy(jointsNearFrameEdge=true),
            CueId.P1_FACE_LIGHT to neutral.copy(faceDarkerThanScene=true),CueId.P1_HIGHLIGHTS to neutral.copy(skyOverexposed=true),
            CueId.P1_DISTANCE to neutral.copy(faceRatio=.03f),CueId.P1_BACKGROUND to neutral.copy(backgroundEdgeDensityHigh=true),
            CueId.P1_NIGHT to neutral.copy(meanLuma=30f,handheldStable=false),CueId.P1_MOTION to neutral.copy(subjectMotionHigh=true),
            CueId.P1_MOTION_BURST to neutral.copy(subjectMotionHigh=true),CueId.P1_MULTI_FRAME to neutral.copy(faceCount=2,subjectCutOff=true),
            CueId.P1_MULTI_LEVEL to neutral.copy(faceCount=2,tiltDegrees=6f),CueId.P1_MULTI_HIGHLIGHTS to neutral.copy(faceCount=2,skyOverexposed=true))
        return inputs.map { (id,signal) -> DynamicTest.dynamicTest("technique-$id-trigger-exit-unknown") {
            val capability=TechniqueCapabilities("已验收长焦",id==CueId.P1_MOTION_BURST)
            assertEquals(id,PhotoTechniqueEngine.suggest(signal,capability)?.cueId)
            assertNull(PhotoTechniqueEngine.suggest(neutral,capability))
            assertNull(PhotoTechniqueEngine.suggest(Signals(),capability))
            if(id==CueId.P1_DISTANCE) assertNull(PhotoTechniqueEngine.suggest(signal,TechniqueCapabilities()))
        }} + DynamicTest.dynamicTest("camera-position-manual-only") {
            assertNull(PhotoTechniqueEngine.suggest(neutral,TechniqueCapabilities()))
            assertEquals(CueId.P1_CAMERA_POSITION,PhotoTechniqueEngine.cameraPositionInspiration().cueId)
        }
    }
}
