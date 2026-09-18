package com.dailyprojects.unitconverter

enum class UnitCategory {
    LENGTH,
    WEIGHT,
    VOLUME,
}

data class UnitDefinition(
    val id: String,
    val label: String,
    val category: UnitCategory,
    val factorToBase: Double,
)
