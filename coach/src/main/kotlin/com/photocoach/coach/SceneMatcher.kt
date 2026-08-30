package com.photocoach.coach

object SceneMatcher {
    fun match(signals: Signals, intent: ShotIntent): SceneId? {
        if (signals.faceCount != 1) return null
        if (signals.faceDarkerThanScene) return SceneId.BACKLIT_PORTRAIT
        if (intent == ShotIntent.PERSON_WITH_SCENERY) return SceneId.OUTDOOR_WITH_SCENERY
        if (signals.coarseScene == CoarseScene.INDOOR && signals.oneSideBrighter) {
            return SceneId.WINDOW_PORTRAIT
        }
        return null
    }
}
