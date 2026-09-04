package com.photocoach.app.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Shared presentation only; permission and consent actions remain owned by the caller. */
@Composable
internal fun CameraAccessLayout(
    title: String,
    body: String,
    icon: ImageVector,
    primaryLabel: String,
    onPrimary: () -> Unit,
    secondaryLabel: String? = null,
    onSecondary: (() -> Unit)? = null,
    notice: String? = null,
) {
    val colors = PhotoCoachTokens.colors
    val spacing = PhotoCoachTokens.spacing
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surfaceContainerHigh, colors.surfaceBase)))
            .safeDrawingPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.space6, vertical = spacing.space8),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.space6),
        ) {
            Box(
                modifier = Modifier.size(88.dp).background(colors.accentAction, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, Modifier.size(38.dp), tint = MaterialTheme.colorScheme.onPrimary)
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.space2),
            ) {
                Text("拍照教练", style = MaterialTheme.typography.labelLarge, color = colors.accentAction)
                Text(title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            }
            Surface(
                color = colors.surfaceRaised,
                shape = MaterialTheme.shapes.large,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = PhotoCoachTokens.masks.subtle)),
            ) {
                Column(Modifier.padding(spacing.space6), verticalArrangement = Arrangement.spacedBy(spacing.space4)) {
                    Text(body, style = MaterialTheme.typography.bodyLarge, color = colors.textSecondary)
                    notice?.let { Text(it, style = MaterialTheme.typography.labelLarge, color = colors.accentAction) }
                }
            }
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
                Button(onClick = onPrimary, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                    Text(primaryLabel, fontWeight = FontWeight.Bold)
                }
                if (secondaryLabel != null && onSecondary != null) {
                    TextButton(onClick = onSecondary, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                        Text(secondaryLabel, color = colors.textSecondary)
                    }
                }
            }
        }
    }
}
