package com.photocoach.coach

class CoachEngine(
    private val catalog: SceneCatalog,
) {
    fun evaluate(signals: Signals, intent: ShotIntent): CoachOutput {
        val sceneId = SceneMatcher.match(signals, intent)
        val scene = catalog.scene(sceneId)
        val cues = CueSelector.select(catalog, scene, signals, intent)
        return CoachOutput(
            intent = intent,
            sceneId = sceneId,
            cues = cues,
            startParams = resolveStartParams(scene, signals, intent),
            overlay = OverlayHint(
                showSilhouette = signals.faceCount == 1 ||
                    signals.faceCount == 0 && signals.poseAvailable,
            ),
        )
    }

    private fun resolveStartParams(
        scene: SceneDefinition?,
        signals: Signals,
        intent: ShotIntent,
    ): SceneStartParams {
        val fallback = SceneStartParams(
            mode = SuggestedMode.PHOTO,
            preferTelephoto = intent == ShotIntent.CLOSE_UP,
            focus = FocusTarget.FACE,
            flashOff = true,
        )
        val base = scene?.startParams ?: fallback
        val mode = when {
            intent == ShotIntent.PERSON_WITH_SCENERY && base.mode != SuggestedMode.HDR -> SuggestedMode.PHOTO
            base.mode == SuggestedMode.PORTRAIT && signals.faceRatio < CueSelector.CLOSE_UP_MIN_FACE_RATIO -> SuggestedMode.PHOTO
            else -> base.mode
        }
        val preferTelephoto =
            intent == ShotIntent.CLOSE_UP && base.preferTelephoto && signals.hasTelephotoPreset
        return base.copy(mode = mode, preferTelephoto = preferTelephoto, flashOff = true)
    }

    companion object {
        fun loadDefault(): CoachEngine = CoachEngine(ScenesLoader.loadFromClasspath())
    }
}
