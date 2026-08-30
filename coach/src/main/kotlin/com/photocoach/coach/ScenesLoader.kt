package com.photocoach.coach

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object ScenesLoader {
    private val json = Json { ignoreUnknownKeys = true }

    fun loadFromClasspath(resourceName: String = "scenes.json"): SceneCatalog {
        val stream = checkNotNull(ScenesLoader::class.java.classLoader.getResourceAsStream(resourceName)) {
            "Missing classpath resource $resourceName"
        }
        return stream.bufferedReader().use { load(it.readText()) }
    }

    fun load(text: String): SceneCatalog {
        val dto = json.decodeFromString<SceneCatalogDto>(text)
        return SceneCatalog(
            coreCues = dto.coreCues.map(CueDto::toDomain),
            scenes = dto.scenes.map(SceneDto::toDomain),
        )
    }
}

@Serializable
private data class SceneCatalogDto(
    val coreCues: List<CueDto>,
    val scenes: List<SceneDto>,
)

@Serializable
private data class CueDto(
    val id: CueId,
    val text: String,
    val audience: Audience,
    val channel: Channel,
    val priority: Int = 0,
    val directionGroup: String? = null,
    val direction: Int = 0,
    val critical: Boolean = false,
) {
    fun toDomain(): Cue = Cue(
        id = id,
        text = text,
        audience = audience,
        channel = channel,
        priority = priority,
        directionGroup = directionGroup,
        direction = direction,
        critical = critical,
    )
}

@Serializable
private data class SceneDto(
    val id: SceneId,
    val startParams: SceneStartParams,
    val shooterCues: List<CueDto> = emptyList(),
    val subjectCues: List<CueDto> = emptyList(),
    val forbiddenPhrases: List<String> = emptyList(),
) {
    fun toDomain(): SceneDefinition = SceneDefinition(
        id = id,
        startParams = startParams,
        shooterCues = shooterCues.map(CueDto::toDomain),
        subjectCues = subjectCues.map(CueDto::toDomain),
        forbiddenPhrases = forbiddenPhrases,
    )
}
