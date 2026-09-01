package com.photocoach.app.creative

import android.content.Context

data class StylePreferences(val recent: List<CreativeStyle> = emptyList(), val favorites: Set<CreativeStyle> = emptySet())

class StylePreferenceStore(context: Context) {
    private val preferences = context.getSharedPreferences("photo_coach", Context.MODE_PRIVATE)

    fun load(): StylePreferences = StylePreferences(
        recent = preferences.getString(KEY_RECENT, null).orEmpty().split(',').mapNotNull(::parseStyle).take(MAX_RECENT),
        favorites = preferences.getStringSet(KEY_FAVORITES, emptySet()).orEmpty().mapNotNull(::parseStyle).toSet(),
    )

    fun recordUse(style: CreativeStyle): StylePreferences {
        if (style == CreativeStyle.ORIGINAL) return load()
        val updated = StylePreferencePolicy.recordUse(load(), style)
        write(updated)
        return updated
    }

    fun setFavorite(style: CreativeStyle, favorite: Boolean): StylePreferences {
        if (style == CreativeStyle.ORIGINAL) return load()
        val updated = StylePreferencePolicy.setFavorite(load(), style, favorite)
        write(updated)
        return updated
    }

    private fun write(value: StylePreferences) {
        preferences.edit().putString(KEY_RECENT, value.recent.joinToString(",", transform = CreativeStyle::name))
            .putStringSet(KEY_FAVORITES, value.favorites.map(CreativeStyle::name).toSet()).apply()
    }

    private fun parseStyle(value: String): CreativeStyle? = runCatching { CreativeStyle.valueOf(value) }.getOrNull()

    private companion object {
        const val KEY_RECENT = "creative_style_recent_v1"
        const val KEY_FAVORITES = "creative_style_favorites_v1"
        const val MAX_RECENT = 5
    }
}

object StylePreferencePolicy {
    fun recordUse(current: StylePreferences, style: CreativeStyle): StylePreferences =
        if (style == CreativeStyle.ORIGINAL) current
        else current.copy(recent = (listOf(style) + current.recent.filterNot { it == style }).take(5))

    fun setFavorite(current: StylePreferences, style: CreativeStyle, favorite: Boolean): StylePreferences =
        if (style == CreativeStyle.ORIGINAL) current
        else current.copy(favorites = if (favorite) current.favorites + style else current.favorites - style)
}
