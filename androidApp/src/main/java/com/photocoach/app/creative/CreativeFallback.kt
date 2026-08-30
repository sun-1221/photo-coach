package com.photocoach.app.creative

data class CreativeFallbackResult<T>(
    val display: T,
    val warning: String?,
)

object CreativeFallback {
    fun <T> choose(original: T, effect: T?, failureMessage: String?): CreativeFallbackResult<T> =
        if (effect != null) {
            CreativeFallbackResult(effect, null)
        } else {
            CreativeFallbackResult(original, failureMessage ?: "创意效果失败，已回退原片")
        }
}
