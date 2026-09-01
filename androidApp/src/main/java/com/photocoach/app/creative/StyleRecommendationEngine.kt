package com.photocoach.app.creative

enum class CreativeSceneTag { PORTRAIT, TRAVEL, FOREST, SUNSET, FOOD, URBAN, NIGHT, UNKNOWN }
enum class PortraitRisk { NONE, LOW, MODERATE, HIGH }

data class StyleProfile(
    val style: CreativeStyle,
    val sceneTags: Set<CreativeSceneTag>,
    val portraitRisk: PortraitRisk,
    val darkRisk: Boolean,
    val highlightRisk: Boolean,
    val suggestedStrength: Float,
    val uncalibratedSafeRange: ClosedFloatingPointRange<Float>,
)

data class StyleRecommendationInput(
    val scene: CreativeSceneTag,
    val meanLuma: Float? = null,
    val faceCount: Int? = null,
    val highlightRatio: Float? = null,
    val recent: List<CreativeStyle> = emptyList(),
    val favorites: Set<CreativeStyle> = emptySet(),
)

data class RecommendedStyle(val style: CreativeStyle, val reason: String, val suggestedStrength: Float)
data class StyleDiscovery(val orderedStyles: List<CreativeStyle>, val recommendations: List<RecommendedStyle>)

object StyleProfiles {
    val all: List<StyleProfile> = listOf(
        profile(CreativeStyle.ORIGINAL, emptySet(), PortraitRisk.NONE, 0f, 0f..0f),
        profile(CreativeStyle.NATURAL_PORTRAIT, setOf(CreativeSceneTag.PORTRAIT), PortraitRisk.LOW, .55f, 0f..0.65f),
        profile(CreativeStyle.SOFT_LIGHT_PORTRAIT, setOf(CreativeSceneTag.PORTRAIT), PortraitRisk.MODERATE, .45f, 0f..0.55f, highlight = true),
        profile(CreativeStyle.COOL_PORTRAIT, setOf(CreativeSceneTag.PORTRAIT, CreativeSceneTag.URBAN), PortraitRisk.MODERATE, .42f, 0f..0.5f),
        profile(CreativeStyle.CLEAR_TRAVEL, setOf(CreativeSceneTag.TRAVEL), PortraitRisk.LOW, .62f, 0f..0.7f, highlight = true),
        profile(CreativeStyle.FOREST_FRESH, setOf(CreativeSceneTag.FOREST, CreativeSceneTag.TRAVEL), PortraitRisk.MODERATE, .5f, 0f..0.6f),
        profile(CreativeStyle.SUNSET_GOLD, setOf(CreativeSceneTag.SUNSET, CreativeSceneTag.PORTRAIT), PortraitRisk.MODERATE, .48f, 0f..0.55f, highlight = true),
        profile(CreativeStyle.WARM_FOOD, setOf(CreativeSceneTag.FOOD), PortraitRisk.HIGH, .58f, 0f..0.65f),
        profile(CreativeStyle.URBAN_COOL, setOf(CreativeSceneTag.URBAN), PortraitRisk.HIGH, .52f, 0f..0.6f),
        profile(CreativeStyle.NEON_NIGHT, setOf(CreativeSceneTag.NIGHT, CreativeSceneTag.URBAN), PortraitRisk.HIGH, .44f, 0f..0.5f, dark = true),
        profile(CreativeStyle.DOCUMENTARY_MONOCHROME, setOf(CreativeSceneTag.URBAN, CreativeSceneTag.TRAVEL), PortraitRisk.MODERATE, .58f, 0f..0.65f),
        profile(CreativeStyle.HIGH_CONTRAST_MONOCHROME, setOf(CreativeSceneTag.URBAN), PortraitRisk.HIGH, .4f, 0f..0.5f, highlight = true),
    )

    fun forStyle(style: CreativeStyle): StyleProfile = all.first { it.style == style }

    private fun profile(style: CreativeStyle, tags: Set<CreativeSceneTag>, portraitRisk: PortraitRisk,
        strength: Float, range: ClosedFloatingPointRange<Float>, dark: Boolean = false, highlight: Boolean = false) =
        StyleProfile(style, tags, portraitRisk, dark, highlight, strength, range)
}

object StyleRecommendationEngine {
    fun discover(input: StyleRecommendationInput): StyleDiscovery {
        val candidates = StyleProfiles.all.asSequence()
            .filter { it.style != CreativeStyle.ORIGINAL }
            .map { profile -> profile to score(profile, input) }
            .filter { it.second > 0 }
            .sortedWith(compareByDescending<Pair<StyleProfile, Int>> { it.second }.thenBy { it.first.style.ordinal })
            .take(MAX_RECOMMENDATIONS)
            .map { (profile, _) -> RecommendedStyle(profile.style, reason(profile, input), profile.suggestedStrength) }
            .toList()
        val preferenceOrder = (input.recent + input.favorites.sortedBy(CreativeStyle::ordinal)).distinct()
        val ordered = (listOf(CreativeStyle.ORIGINAL) + candidates.map { it.style } + preferenceOrder + CreativeStyle.entries)
            .distinct()
        return StyleDiscovery(ordered, candidates)
    }

    private fun score(profile: StyleProfile, input: StyleRecommendationInput): Int {
        var score = if (input.scene in profile.sceneTags) 10 else 0
        if (profile.style in input.favorites) score += 3
        val recentIndex = input.recent.indexOf(profile.style)
        if (recentIndex >= 0) score += (5 - recentIndex).coerceAtLeast(1)
        if (input.faceCount == 1 && profile.portraitRisk == PortraitRisk.HIGH) score -= 4
        if ((input.meanLuma ?: 128f) < 60f && profile.darkRisk) score -= 4
        if ((input.highlightRatio ?: 0f) > 0.08f && profile.highlightRisk) score -= 4
        return score
    }

    private fun reason(profile: StyleProfile, input: StyleRecommendationInput): String = when {
        input.scene in profile.sceneTags -> "适合当前${sceneLabel(input.scene)}画面"
        profile.style in input.favorites -> "你已收藏这个风格"
        profile.style in input.recent -> "你最近用过这个风格"
        else -> "作为克制的备选效果"
    }

    private fun sceneLabel(scene: CreativeSceneTag): String = when (scene) {
        CreativeSceneTag.PORTRAIT -> "人像"
        CreativeSceneTag.TRAVEL -> "旅行"
        CreativeSceneTag.FOREST -> "林间"
        CreativeSceneTag.SUNSET -> "日落"
        CreativeSceneTag.FOOD -> "美食"
        CreativeSceneTag.URBAN -> "都市"
        CreativeSceneTag.NIGHT -> "夜景"
        CreativeSceneTag.UNKNOWN -> ""
    }

    private const val MAX_RECOMMENDATIONS = 3
}
