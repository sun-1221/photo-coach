package com.photocoach.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val scheme = darkColorScheme(
    primary = Color(0xFFFFC857),
    onPrimary = Color(0xFF1A1A1A),
    background = Color.Black,
    surface = Color(0xCC111111),
    onSurface = Color.White,
)

@Composable
fun PhotoCoachTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = scheme, content = content)
}
