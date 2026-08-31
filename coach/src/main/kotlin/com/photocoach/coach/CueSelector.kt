package com.photocoach.coach

import kotlin.math.abs

object CueSelector {
    const val CLOSE_UP_MIN_FACE_RATIO = 0.12f
    const val SCENERY_MIN_FACE_RATIO = 0.05f
    const val TILT_THRESHOLD = 3f

    private val bannedTerms = listOf(
        "光圈", "ISO", "iso", "f/", "开尔文", "评分", "百分比",
        "微笑", "胖", "瘦", "体重", "重心",
    )

    fun select(
        catalog: SceneCatalog,
        scene: SceneDefinition?,
        signals: Signals,
        intent: ShotIntent,
    ): List<Cue> {
        if (signals.lensObscured) {
            return listOfNotNull(sanitize(catalog.cue(CueId.CLEAN_LENS), emptyList()))
        }
        if (signals.faceCount > 1) return pickSafeMultiPersonCues(catalog, signals)
        if (signals.faceCount == 0) {
            if (!signals.poseAvailable) {
                return listOfNotNull(sanitize(catalog.cue(CueId.FIND_PERSON), emptyList()))
            }
            return listOfNotNull(pickPose(catalog, scene = null, signals))
                .mapNotNull { sanitize(it, emptyList()) }
                .take(1)
        }
        val selected = listOfNotNull(
            pickComposition(catalog, scene, signals, intent),
            pickLight(catalog, signals),
            pickPose(catalog, scene, signals),
        )
        return selected
            .mapNotNull { sanitize(it, scene?.forbiddenPhrases.orEmpty()) }
            .distinctBy(Cue::channel)
            .take(3)
    }

    private fun pickComposition(
        catalog: SceneCatalog,
        scene: SceneDefinition?,
        signals: Signals,
        intent: ShotIntent,
    ): Cue? = when {
        signals.subjectCutOff -> catalog.cue(CueId.KEEP_SUBJECT_IN_FRAME)
        abs(signals.tiltDegrees) > TILT_THRESHOLD -> catalog.cue(CueId.LEVEL_PHONE)
        intent == ShotIntent.CLOSE_UP && signals.faceRatio < CLOSE_UP_MIN_FACE_RATIO -> {
            val base = catalog.cue(CueId.MOVE_CLOSER)
            if (signals.hasTelephotoPreset) base.copy(text = "走近一步，或切长焦") else base
        }
        intent == ShotIntent.CLOSE_UP && signals.faceTooLowInFrame ->
            catalog.cue(CueId.PLACE_FACE_ON_UPPER_THIRD)
        intent == ShotIntent.PERSON_WITH_SCENERY && signals.faceRatio < SCENERY_MIN_FACE_RATIO ->
            catalog.cue(CueId.MOVE_CLOSER_KEEP_SCENERY)
        intent == ShotIntent.PERSON_WITH_SCENERY && signals.personCentered ->
            catalog.cue(CueId.PLACE_ON_THIRDS)
        scene?.id == SceneId.WINDOW_PORTRAIT -> scene.shooterCues.firstOrNull { it.channel == Channel.COMPOSITION }
        scene?.id == SceneId.BACKLIT_PORTRAIT -> scene.shooterCues.firstOrNull { it.channel == Channel.COMPOSITION }
        else -> null
    }

    private fun pickLight(catalog: SceneCatalog, signals: Signals): Cue? = when {
        signals.faceDarkerThanScene || !signals.focusOnFace -> catalog.cue(CueId.FOCUS_FACE)
        signals.skyOverexposed -> catalog.cue(CueId.LOWER_EXPOSURE)
        else -> null
    }

    private fun pickPose(
        catalog: SceneCatalog,
        scene: SceneDefinition?,
        signals: Signals,
    ): Cue? {
        return when {
            signals.faceTurnedAway -> catalog.cue(CueId.TURN_FACE_TO_CAMERA)
            signals.eyesLikelyClosed -> catalog.cue(CueId.OPEN_EYES)
            !signals.poseAvailable -> null
            signals.headTiltedBack -> catalog.cue(CueId.CHIN_DOWN)
            signals.shouldersSquare -> sceneAngleCue(scene) ?: catalog.cue(CueId.ANGLE_BODY)
            signals.shouldersRaised -> catalog.cue(CueId.RELAX_SHOULDERS)
            signals.handsIdle -> catalog.cue(CueId.REST_HANDS)
            else -> null
        }
    }

    private fun pickSafeMultiPersonCues(catalog: SceneCatalog, signals: Signals): List<Cue> {
        val cues = listOfNotNull(
            when {
                signals.subjectCutOff -> catalog.cue(CueId.KEEP_SUBJECT_IN_FRAME)
                abs(signals.tiltDegrees) > TILT_THRESHOLD -> catalog.cue(CueId.LEVEL_PHONE)
                else -> null
            },
            if (signals.skyOverexposed) catalog.cue(CueId.LOWER_EXPOSURE) else null,
        )
        return cues.mapNotNull { sanitize(it, emptyList()) }.distinctBy(Cue::channel).take(2)
    }

    private fun sceneAngleCue(scene: SceneDefinition?): Cue? = scene?.subjectCues?.firstOrNull {
        it.id == CueId.TURN_TO_WINDOW ||
            it.id == CueId.ANGLE_BODY ||
            it.id == CueId.ANGLE_BODY_BACKLIT
    }

    private fun sanitize(cue: Cue, forbiddenPhrases: List<String>): Cue? {
        if (cue.text.isBlank() || cue.text.length > 24) return null
        if (bannedTerms.any { cue.text.contains(it, ignoreCase = true) }) return null
        if (forbiddenPhrases.any { cue.text.contains(it, ignoreCase = true) }) return null
        if (cue.audience == Audience.PROXY) return null
        return cue
    }
}
