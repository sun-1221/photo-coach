package com.photocoach.app.creative

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class StyleRecommendationEngineTest {
    @Test fun `original is always first and recommendation is bounded`() {
        val result = StyleRecommendationEngine.discover(
            StyleRecommendationInput(CreativeSceneTag.PORTRAIT, faceCount = 1),
        )
        assertEquals(CreativeStyle.ORIGINAL, result.orderedStyles.first())
        assertTrue(result.recommendations.size <= 3)
        assertFalse(result.recommendations.any { it.style == CreativeStyle.ORIGINAL })
        assertTrue(result.recommendations.all { it.reason.isNotBlank() && it.suggestedStrength < 1f })
    }

    @Test fun `recommendation does not select or mutate a style`() {
        val input = StyleRecommendationInput(CreativeSceneTag.NIGHT, recent = listOf(CreativeStyle.NATURAL_PORTRAIT))
        val result = StyleRecommendationEngine.discover(input)
        assertEquals(listOf(CreativeStyle.NATURAL_PORTRAIT), input.recent)
        assertEquals(CreativeStyle.ORIGINAL, result.orderedStyles.first())
    }

    @Test fun `recent keeps five unique entries and favorites are explicit`() {
        var preferences = StylePreferences()
        CreativeStyle.entries.filterNot { it == CreativeStyle.ORIGINAL }.take(6).forEach {
            preferences = StylePreferencePolicy.recordUse(preferences, it)
        }
        assertEquals(5, preferences.recent.size)
        preferences = StylePreferencePolicy.recordUse(preferences, preferences.recent.last())
        assertEquals(5, preferences.recent.distinct().size)
        preferences = StylePreferencePolicy.setFavorite(preferences, CreativeStyle.NATURAL_PORTRAIT, true)
        assertTrue(CreativeStyle.NATURAL_PORTRAIT in preferences.favorites)
        assertEquals(preferences, StylePreferencePolicy.setFavorite(preferences, CreativeStyle.ORIGINAL, true))
    }
}
