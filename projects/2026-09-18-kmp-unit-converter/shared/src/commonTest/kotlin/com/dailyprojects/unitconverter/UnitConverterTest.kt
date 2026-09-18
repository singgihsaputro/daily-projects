package com.dailyprojects.unitconverter

import kotlin.test.Test
import kotlin.test.assertEquals

class UnitConverterTest {

    private fun unit(id: String, factorToBase: Double, category: UnitCategory = UnitCategory.LENGTH) =
        UnitDefinition(id = id, label = id, category = category, factorToBase = factorToBase)

    @Test
    fun convertingBetweenTheSameUnitReturnsTheSameValue() {
        val meter = unit("meter", 1.0)
        assertEquals(5.0, UnitConverter.convert(5.0, meter, meter))
    }

    @Test
    fun convertsKilometersToMeters() {
        val kilometer = unit("km", 1000.0)
        val meter = unit("m", 1.0)
        assertEquals(3000.0, UnitConverter.convert(3.0, kilometer, meter))
    }

    @Test
    fun convertsPoundsToGrams() {
        val pound = unit("lb", 453.592, UnitCategory.WEIGHT)
        val gram = unit("g", 1.0, UnitCategory.WEIGHT)
        assertEquals(453.592, UnitConverter.convert(1.0, pound, gram))
    }
}
