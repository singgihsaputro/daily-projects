package com.dailyprojects.unitconverter

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class UnitDto(
    val id: String,
    val label: String,
    val category: String,
    val factorToBase: Double,
)

/** Parses the bundled fixture from [source] into domain [UnitDefinition]s. */
class MockUnitRepository(private val source: UnitJsonSource) : UnitRepository {

    override suspend fun loadUnits(): List<UnitDefinition> {
        val dtos = Json.decodeFromString<List<UnitDto>>(source.readJson())
        return dtos.map { dto ->
            UnitDefinition(
                id = dto.id,
                label = dto.label,
                category = UnitCategory.valueOf(dto.category),
                factorToBase = dto.factorToBase,
            )
        }
    }
}
