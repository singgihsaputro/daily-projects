package com.dailyprojects.unitconverter

/** Pure conversion math: every unit's [UnitDefinition.factorToBase] is relative to its category's base unit. */
object UnitConverter {
    fun convert(value: Double, from: UnitDefinition, to: UnitDefinition): Double =
        value * from.factorToBase / to.factorToBase
}
