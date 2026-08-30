package com.photocoach.coach

data class SceneDefinition(
    val id: SceneId,
    val startParams: SceneStartParams,
    val shooterCues: List<Cue>,
    val subjectCues: List<Cue>,
    val forbiddenPhrases: List<String>,
)

data class SceneCatalog(
    val coreCues: List<Cue>,
    val scenes: List<SceneDefinition>,
) {
    private val cueById = coreCues.associateBy(Cue::id)
    private val sceneById = scenes.associateBy(SceneDefinition::id)

    fun cue(id: CueId): Cue = cueById[id] ?: error("scenes.json missing cue $id")

    fun scene(id: SceneId?): SceneDefinition? = id?.let(sceneById::get)
}
