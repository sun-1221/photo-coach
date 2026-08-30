package com.photocoach.app.ui.viewfinder

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.photocoach.app.analysis.OverlayGeometry
import com.photocoach.coach.OverlayHint
import kotlin.math.abs

@Composable
fun CoachOverlay(
    geometry: OverlayGeometry?,
    hint: OverlayHint?,
    tiltDegrees: Float,
    showGrid: Boolean,
    showLevel: Boolean,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier.fillMaxSize()) {
        if (showGrid && hint?.showThirds != false) {
            val xs = listOf(size.width / 3f, size.width * 2f / 3f)
            val ys = listOf(size.height / 3f, size.height * 2f / 3f)
            xs.forEach { x ->
                drawLine(Color.White.copy(alpha = 0.28f), Offset(x, 0f), Offset(x, size.height), 2f)
            }
            ys.forEach { y ->
                drawLine(Color.White.copy(alpha = 0.28f), Offset(0f, y), Offset(size.width, y), 2f)
            }
        }
        if (showLevel && hint?.showHorizon != false) {
            val shift = (tiltDegrees / 45f).coerceIn(-1f, 1f) * 36f
            drawLine(
                color = if (abs(tiltDegrees) > 3f) Color(0xFFFFC857) else Color(0xFF8FE388),
                start = Offset(0f, size.height / 2f + shift),
                end = Offset(size.width, size.height / 2f + shift),
                strokeWidth = 3f,
            )
        }
        val overlay = geometry
        val showSilhouette = hint?.showSilhouette == true && overlay?.showSilhouette == true
        if (showSilhouette) {
            overlay.faceRects.firstOrNull()?.let { rect ->
                drawRect(
                    color = Color.White.copy(alpha = 0.85f),
                    topLeft = Offset(rect.left, rect.top),
                    size = androidx.compose.ui.geometry.Size(rect.width(), rect.height()),
                    style = Stroke(width = 3f),
                )
            }
            val points = overlay.posePoints
            if (points.size >= 3) {
                val path = Path()
                points.forEachIndexed { index, point ->
                    if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
                }
                drawPath(path, Color.White.copy(alpha = 0.7f), style = Stroke(width = 4f))
            }
        }
    }
}
