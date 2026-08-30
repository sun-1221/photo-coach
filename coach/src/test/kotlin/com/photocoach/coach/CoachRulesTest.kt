package com.photocoach.coach

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CoachRulesTest {
    private val engine = CoachEngine.loadDefault()

    @Test
    fun userIntentHardFiltersSceneryAndNeverUsesTelephotoForIt() {
        val signals = Signals(
            faceCount = 1,
            faceRatio = 0.08f,
            coarseScene = CoarseScene.OUTDOOR,
            hasLargeEnvironment = true,
            hasTelephotoPreset = true,
            personCentered = true,
            poseAvailable = true,
        )

        val closeUp = engine.evaluate(signals, ShotIntent.CLOSE_UP)
        val scenery = engine.evaluate(signals, ShotIntent.PERSON_WITH_SCENERY)

        assertNull(closeUp.sceneId)
        assertEquals(SceneId.OUTDOOR_WITH_SCENERY, scenery.sceneId)
        assertFalse(scenery.startParams.preferTelephoto)
        assertFalse(scenery.cues.any { it.text.contains("长焦") })
    }

    @Test
    fun noVerifiedTelephotoNeverOffersTelephoto() {
        val output = engine.evaluate(
            Signals(faceCount = 1, faceRatio = 0.05f, hasTelephotoPreset = false),
            ShotIntent.CLOSE_UP,
        )

        assertTrue(output.cues.any {
            it.id == CueId.MOVE_CLOSER && it.text == "走近一步，减少无关空白"
        })
        assertFalse(output.cues.any { it.text.contains("长焦") })
        assertFalse(output.startParams.preferTelephoto)
    }

    @Test
    fun closeUpMovesAVisiblyLowFaceTowardTheUpperThird() {
        val output = engine.evaluate(
            Signals(faceCount = 1, faceRatio = 0.16f, faceTooLowInFrame = true),
            ShotIntent.CLOSE_UP,
        )

        assertTrue(output.cues.any {
            it.id == CueId.PLACE_FACE_ON_UPPER_THIRD && it.text == "把脸放到上方三分线"
        })
    }

    @Test
    fun verifiedTelephotoCanBeOfferedForCloseUpOnly() {
        val output = engine.evaluate(
            Signals(faceCount = 1, faceRatio = 0.05f, hasTelephotoPreset = true),
            ShotIntent.CLOSE_UP,
        )

        assertTrue(output.cues.any { it.text == "走近一步，或切长焦" })
        assertTrue(output.startParams.preferTelephoto)
    }

    @Test
    fun reliablePoseSurvivesFaceMissButMultipleFacesStillBlockSinglePersonPose() {
        val faceMiss = engine.evaluate(
            Signals(faceCount = 0, poseAvailable = true, shouldersSquare = true),
            ShotIntent.CLOSE_UP,
        )
        assertEquals(listOf(CueId.ANGLE_BODY), faceMiss.cues.map(Cue::id))
        assertTrue(faceMiss.overlay.showSilhouette)

        val multiple = engine.evaluate(
            Signals(faceCount = 2, poseAvailable = true, shouldersSquare = true),
            ShotIntent.CLOSE_UP,
        )
        assertTrue(multiple.cues.isEmpty())
        assertFalse(multiple.overlay.showSilhouette)
    }

    @Test
    fun faceAndPoseMissProduceVisibleRecoveryInsteadOfReady() {
        val output = engine.evaluate(Signals(faceCount = 0, poseAvailable = false), ShotIntent.CLOSE_UP)

        assertEquals(listOf(CueId.FIND_PERSON), output.cues.map(Cue::id))
        assertEquals("请露出脸，或靠近一点", output.cues.single().text)
        assertTrue(output.cues.single().critical)
    }

    @Test
    fun conservativeLensWarningCanAppearWhenTheCoveredLensHidesTheSubject() {
        val output = engine.evaluate(
            Signals(faceCount = 0, lensObscured = true),
            ShotIntent.CLOSE_UP,
        )

        assertEquals(listOf(CueId.CLEAN_LENS), output.cues.map(Cue::id))
        assertTrue(output.cues.single().text.contains("可能"))
        assertTrue(output.cues.single().text.contains("检查"))
    }

    @Test
    fun atMostOneCandidatePerChannelAndNoForbiddenProductLanguage() {
        val output = engine.evaluate(
            Signals(
                faceCount = 1,
                faceRatio = 0.16f,
                coarseScene = CoarseScene.INDOOR,
                oneSideBrighter = true,
                faceDarkerThanScene = true,
                poseAvailable = true,
                headTiltedBack = true,
            ),
            ShotIntent.CLOSE_UP,
        )
        assertTrue(output.cues.size <= 3)
        Channel.entries.forEach { channel -> assertTrue(output.cues.count { it.channel == channel } <= 1) }
        val allText = output.cues.joinToString { it.text }
        listOf("评分", "百分比", "自动拍", "开闪光", "光圈", "ISO").forEach {
            assertFalse(allText.contains(it, ignoreCase = true))
        }
        assertTrue(output.cues.none { it.audience == Audience.PROXY })
    }

    @Test
    fun poseCandidateTracksCurrentPoseInsteadOfUsingOneFrozenSceneCue() {
        val square = engine.evaluate(
            Signals(
                faceCount = 1,
                faceRatio = 0.16f,
                poseAvailable = true,
                shouldersSquare = true,
            ),
            ShotIntent.CLOSE_UP,
        )
        val headBack = engine.evaluate(
            Signals(
                faceCount = 1,
                faceRatio = 0.16f,
                poseAvailable = true,
                headTiltedBack = true,
            ),
            ShotIntent.CLOSE_UP,
        )
        val acceptable = engine.evaluate(
            Signals(faceCount = 1, faceRatio = 0.16f, poseAvailable = true),
            ShotIntent.CLOSE_UP,
        )

        assertEquals(CueId.ANGLE_BODY, square.cues.first { it.channel == Channel.POSE }.id)
        assertEquals(CueId.CHIN_DOWN, headBack.cues.first { it.channel == Channel.POSE }.id)
        assertFalse(acceptable.cues.any { it.channel == Channel.POSE })
    }
}
