package com.dailyprojects.shimmer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ShimmerMathTest {
    private val spec = ShimmerSpec(bandWidthFraction = 0.5f, angleDegrees = 0f)

    @Test
    fun bandStartsFullyLeftOfElement() {
        val band = ShimmerMath.band(0f, 200f, 100f, spec)
        assertEquals(0f, band.endX, 0.001f)
        assertEquals(-100f, band.startX, 0.001f)
    }

    @Test
    fun bandEndsFullyRightOfElement() {
        val band = ShimmerMath.band(1f, 200f, 100f, spec)
        assertEquals(200f, band.startX, 0.001f)
    }

    @Test
    fun progressIsClamped() {
        assertEquals(ShimmerMath.band(1f, 200f, 100f, spec), ShimmerMath.band(5f, 200f, 100f, spec))
        assertEquals(ShimmerMath.band(0f, 200f, 100f, spec), ShimmerMath.band(-1f, 200f, 100f, spec))
    }

    @Test
    fun tiltProducesVerticalDrift() {
        val band = ShimmerMath.band(0.5f, 200f, 100f, ShimmerSpec(angleDegrees = 45f))
        assertEquals(100f, band.skewY, 0.01f)
    }

    @Test
    fun invalidSpecIsRejected() {
        assertThrows(IllegalArgumentException::class.java) { ShimmerSpec(durationMillis = 0) }
        assertThrows(IllegalArgumentException::class.java) { ShimmerSpec(angleDegrees = 90f) }
    }
}
