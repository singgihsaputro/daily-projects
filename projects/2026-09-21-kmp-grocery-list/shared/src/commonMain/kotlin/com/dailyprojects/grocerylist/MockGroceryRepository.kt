package com.dailyprojects.grocerylist

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class GroceryItemDto(
    val id: String,
    val name: String,
    val quantity: Int,
    val category: String,
    val checked: Boolean = false,
)

/** Parses the bundled fixture from [source] into domain [GroceryItem]s. */
class MockGroceryRepository(private val source: GroceryJsonSource) : GroceryRepository {

    override suspend fun loadItems(): List<GroceryItem> {
        val dtos = Json.decodeFromString<List<GroceryItemDto>>(source.readJson())
        return dtos.map { dto ->
            GroceryItem(
                id = dto.id,
                name = dto.name,
                quantity = dto.quantity,
                category = GroceryCategory.valueOf(dto.category),
                checked = dto.checked,
            )
        }
    }
}
