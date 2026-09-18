package com.dailyprojects.unitconverter

/** Source of the unit catalog. Swap [MockUnitRepository] for a networked implementation without touching callers. */
interface UnitRepository {
    suspend fun loadUnits(): List<UnitDefinition>
}

/** Reads the raw `mock/units.json` text. Each platform bundles and opens that fixture differently. */
interface UnitJsonSource {
    fun readJson(): String
}
