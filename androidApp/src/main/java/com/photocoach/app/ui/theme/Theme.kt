package com.photocoach.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val scheme = darkColorScheme(
    primary = Color(0xFFFFC96B),
    onPrimary = Color(0xFF241A00),
    primaryContainer = Color(0xFF493700),
    onPrimaryContainer = Color(0xFFFFE19E),
    secondary = Color(0xFF9ED9D4),
    onSecondary = Color(0xFF003735),
    background = Color(0xFF090A0C),
    onBackground = Color(0xFFF4F4F5),
    surface = Color(0xFF15171A),
    surfaceVariant = Color(0xFF23262B),
    surfaceContainerLowest = Color(0xFF090A0C),
    surfaceContainerLow = Color(0xFF111316),
    surfaceContainer = Color(0xFF1B1E23),
    surfaceContainerHigh = Color(0xFF26292F),
    surfaceContainerHighest = Color(0xFF32363C),
    onSurface = Color(0xFFF4F4F5),
    onSurfaceVariant = Color(0xFFC6C8CE),
    outline = Color(0xFF696C73),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

@Immutable
data class PhotoCoachSemanticColors(
    val surfaceBase: Color = Color(0xFF090A0C),
    val surfaceRaised: Color = Color(0xFF15171A),
    val surfaceOverlay: Color = Color(0xFF23262B),
    val textPrimary: Color = Color(0xFFF4F4F5),
    val textSecondary: Color = Color(0xFFC6C8CE),
    val accentAction: Color = Color(0xFFFFC96B),
    val feedbackPositive: Color = Color(0xFF9ED9D4),
    val feedbackWarning: Color = Color(0xFFFFC96B),
    val feedbackCritical: Color = Color(0xFFFFB4AB),
    val shutterEnabled: Color = Color.White,
    val shutterDisabled: Color = Color(0xFF666970),
)

@Immutable
data class PhotoCoachSpacing(
    val space1: Dp = 4.dp,
    val space2: Dp = 8.dp,
    val space3: Dp = 12.dp,
    val space4: Dp = 16.dp,
    val space6: Dp = 24.dp,
    val space8: Dp = 32.dp,
    val compactDockVertical: Dp = 3.dp,
)

@Immutable
data class PhotoCoachRadii(
    val status: Dp = 8.dp,
    val control: Dp = 12.dp,
    val card: Dp = 16.dp,
    val panel: Dp = 24.dp,
)

@Immutable
data class PhotoCoachMasks(
    val subtle: Float = 0.4f,
    val overlay: Float = 0.78f,
    val dock: Float = 0.95f,
    val blocking: Float = 0.86f,
    val modal: Float = 0.92f,
)

private val semanticColors = PhotoCoachSemanticColors()
private val spacing = PhotoCoachSpacing()
private val radii = PhotoCoachRadii()
private val masks = PhotoCoachMasks()

private val LocalPhotoCoachColors = staticCompositionLocalOf { semanticColors }
private val LocalPhotoCoachSpacing = staticCompositionLocalOf { spacing }
private val LocalPhotoCoachRadii = staticCompositionLocalOf { radii }
private val LocalPhotoCoachMasks = staticCompositionLocalOf { masks }

object PhotoCoachTokens {
    val colors: PhotoCoachSemanticColors
        @Composable
        @ReadOnlyComposable
        get() = LocalPhotoCoachColors.current

    val spacing: PhotoCoachSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalPhotoCoachSpacing.current

    val radii: PhotoCoachRadii
        @Composable
        @ReadOnlyComposable
        get() = LocalPhotoCoachRadii.current

    val masks: PhotoCoachMasks
        @Composable
        @ReadOnlyComposable
        get() = LocalPhotoCoachMasks.current
}

private val shapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
)

private val typography = Typography(
    displayLarge = TextStyle(fontSize = 32.sp, lineHeight = 38.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 16.sp, lineHeight = 23.sp),
    bodySmall = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 19.sp, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontSize = 14.sp, lineHeight = 18.sp),
    labelSmall = TextStyle(fontSize = 12.sp, lineHeight = 16.sp),
)

@Composable
fun PhotoCoachTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalPhotoCoachColors provides semanticColors,
        LocalPhotoCoachSpacing provides spacing,
        LocalPhotoCoachRadii provides radii,
        LocalPhotoCoachMasks provides masks,
    ) {
        MaterialTheme(
            colorScheme = scheme,
            shapes = shapes,
            typography = typography,
        ) {
            CompositionLocalProvider(androidx.compose.material3.LocalContentColor provides scheme.onSurface,
                content = content)
        }
    }
}
