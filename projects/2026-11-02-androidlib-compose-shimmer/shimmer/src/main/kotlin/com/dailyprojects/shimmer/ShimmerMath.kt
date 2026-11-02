package com.dailyprojects.shimmer

import kotlin.math.tan

/** Start and end x-coordinates of the gradient band, plus its vertical drift. */
data class BandGeometry(val startX: Float, val endX: Float, val skewY: Float)

/** Pure geometry for the shimmer band, kept free of Compose so it is unit-testable. */
object ShimmerMath {
    /**
     * Where the band sits for [progress] in 0..1.
     * At 0 the band is fully left of the element; at 1 it is fully right of it.
     */
    fun band(progress: Float, width: Float, height: Float, spec: ShimmerSpec): BandGeometry {
        val p = progress.coerceIn(0f, 1f)
        val bandWidth = width * spec.bandWidthFraction
        val startX = -bandWidth + (width + bandWidth) * p
        val skew = (height * tan(Math.toRadians(spec.angleDegrees.toDouble()))).toFloat()
        return BandGeometry(startX = startX, endX = startX + bandWidth, skewY = skew)
    }
}
