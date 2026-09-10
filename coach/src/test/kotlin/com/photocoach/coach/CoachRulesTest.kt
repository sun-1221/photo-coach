package com.photocoach.coach

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CoachRulesTest {
    private val engine = CoachEngine.loadDefault()
    @Test
    fun darkFaceDoesNotRepeatMeteringAfterSuccessfulFaceTap() {
        val dark = Signals(faceCount = 1, faceRatio = .18f, faceDarkerThanScene = true)
        assertTrue(engine.evaluate(dark, ShotIntent.CLOSE_UP).cues.any { it.id == CueId.FOCUS_FACE })
        val metered = dark.copy(faceMetered = true)
        assertFalse(engine.evaluate(metered, ShotIntent.CLOSE_UP).cues.any { it.id == CueId.FOCUS_FACE })
        assertTrue(engine.evaluate(metered.copy(focusOnFace = false), ShotIntent.CLOSE_UP)
            .cues.any { it.id == CueId.FOCUS_FACE })
    }


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
    fun poseOnlyFrameFallsBackToFindPersonAfterObservablePoseIssueClears() {
        val output = engine.evaluate(
            Signals(faceCount = 0, poseAvailable = true),
            ShotIntent.CLOSE_UP,
        )

        assertEquals(listOf(CueId.FIND_PERSON), output.cues.map(Cue::id))
        assertEquals("请露出脸，或靠近一点", output.cues.single().text)
    }

    @Test
    fun multipleFacesKeepOnlySafeFramingAndExposureCues() {
        val output = engine.evaluate(
            Signals(
                faceCount = 2,
                poseAvailable = true,
                shouldersSquare = true,
                subjectCutOff = true,
                skyOverexposed = true,
            ),
            ShotIntent.CLOSE_UP,
        )

        assertEquals(
            setOf(CueId.KEEP_SUBJECT_IN_FRAME, CueId.LOWER_EXPOSURE),
            output.cues.map(Cue::id).toSet(),
        )
        assertFalse(output.cues.any { it.channel == Channel.POSE })
        assertFalse(output.overlay.showSilhouette)
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

    @Test
    fun faceDetailsProduceOnlyObservedSubjectCuesWithoutRequiringPose() {
        val turned = engine.evaluate(
            Signals(faceCount = 1, faceRatio = 0.16f, faceTurnedAway = true),
            ShotIntent.CLOSE_UP,
        )
        val blink = engine.evaluate(
            Signals(faceCount = 1, faceRatio = 0.16f, eyesLikelyClosed = true),
            ShotIntent.CLOSE_UP,
        )
        val uncertain = engine.evaluate(
            Signals(faceCount = 1, faceRatio = 0.16f),
            ShotIntent.CLOSE_UP,
        )

        assertEquals(CueId.TURN_FACE_TO_CAMERA, turned.cues.single { it.channel == Channel.POSE }.id)
        assertEquals(CueId.OPEN_EYES, blink.cues.single { it.channel == Channel.POSE }.id)
        assertFalse(uncertain.cues.any { it.channel == Channel.POSE })
    }

    @Test
    fun unreliableExpressionAndBodyJudgementLanguageIsRejectedAtTheCatalogBoundary() {
        val defaultCatalog = ScenesLoader.loadFromClasspath()
        val unsafeTexts = listOf(
            "微笑一点",
            "这样显得更瘦",
            "把体重移到后面",
            "重心换到后腿",
        )

        unsafeTexts.forEach { unsafeText ->
            val catalog = defaultCatalog.copy(
                coreCues = defaultCatalog.coreCues.map { cue ->
                    if (cue.id == CueId.ANGLE_BODY) cue.copy(text = unsafeText) else cue
                },
            )
            val output = CoachEngine(catalog).evaluate(
                Signals(
                    faceCount = 1,
                    faceRatio = 0.16f,
                    poseAvailable = true,
                    shouldersSquare = true,
                ),
                ShotIntent.CLOSE_UP,
            )

            assertFalse(output.cues.any { it.text == unsafeText }, unsafeText)
        }
    }
}
