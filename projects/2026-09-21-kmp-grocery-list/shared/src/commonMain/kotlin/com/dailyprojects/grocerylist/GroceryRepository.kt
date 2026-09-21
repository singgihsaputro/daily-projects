package com.dailyprojects.grocerylist

/** Source of the starting list. Swap [MockGroceryRepository] for a networked implementation without touching callers. */
interface GroceryRepository {
    suspend fun loadItems(): List<GroceryItem>
}

/** Reads the raw `mock/groceries.json` text. Each platform bundles and opens that fixture differently. */
interface GroceryJsonSource {
    fun readJson(): String
}
