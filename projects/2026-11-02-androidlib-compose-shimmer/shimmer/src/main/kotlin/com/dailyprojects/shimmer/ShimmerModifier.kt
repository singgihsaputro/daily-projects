package com.dailyprojects.shimmer

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.RectangleShape

/**
 * Paints a moving highlight band over this element's own content.
 * Use it on real content that is already laid out; for pure placeholders use [skeleton].
 */
fun Modifier.shimmer(
    enabled: Boolean = true,
    spec: ShimmerSpec = ShimmerSpec.Light,
): Modifier = if (!enabled) this else composed {
    val progress by rememberInfiniteTransition(label = "shimmer").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(spec.durationMillis, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmer-progress",
    )
    drawWithContent {
        drawContent()
        val band = ShimmerMath.band(progress, size.width, size.height, spec)
        val brush = Brush.linearGradient(
            colors = listOf(spec.highlight.copy(alpha = 0f), spec.highlight, spec.highlight.copy(alpha = 0f)),
            start = Offset(band.startX, 0f),
            end = Offset(band.endX, band.skewY),
        )
        drawRect(brush)
    }
}

/**
 * Turns this element into a skeleton block while [visible] is true: the content is
 * not drawn, a [ShimmerSpec.base] shape is drawn instead, and the shimmer sweeps over it.
 * When [visible] becomes false the content is drawn normally with no overhead.
 */
fun Modifier.skeleton(
    visible: Boolean,
    shape: Shape = RectangleShape,
    spec: ShimmerSpec = ShimmerSpec.Light,
): Modifier = if (!visible) this else this
    .clip(shape)
    .drawWithContent { drawRect(spec.base) }
    .shimmer(enabled = true, spec = spec)
