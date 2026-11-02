package com.dailyprojects.shimmer

import androidx.compose.ui.graphics.Color

/**
 * Look and timing of a shimmer sweep.
 *
 * @param base colour of the placeholder surface.
 * @param highlight colour of the moving band.
 * @param durationMillis time for the band to cross the element once.
 * @param bandWidthFraction width of the band as a fraction of the element width.
 * @param angleDegrees tilt of the band; 0 is a vertical band moving left to right.
 */
data class ShimmerSpec(
    val base: Color = Color(0xFFE3E3E8),
    val highlight: Color = Color(0xFFF6F6F9),
    val durationMillis: Int = 1200,
    val bandWidthFraction: Float = 0.6f,
    val angleDegrees: Float = 15f,
) {
    init {
        require(durationMillis > 0) { "durationMillis must be positive" }
        require(bandWidthFraction > 0f) { "bandWidthFraction must be positive" }
        require(angleDegrees > -90f && angleDegrees < 90f) { "angleDegrees must be within (-90, 90)" }
    }

    companion object {
        val Light = ShimmerSpec()
        val Dark = ShimmerSpec(base = Color(0xFF2B2B31), highlight = Color(0xFF3F3F47))
    }
}
