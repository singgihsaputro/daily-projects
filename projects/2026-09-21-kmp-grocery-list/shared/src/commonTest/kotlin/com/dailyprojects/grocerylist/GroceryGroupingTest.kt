package com.dailyprojects.grocerylist

import kotlin.test.Test
import kotlin.test.assertEquals

class GroceryGroupingTest {

    private fun item(id: String, category: GroceryCategory) =
        GroceryItem(id = id, name = id, quantity = 1, category = category)

    @Test
    fun groupsItemsInCategoryDeclarationOrder() {
        val items = listOf(
            item("bread", GroceryCategory.BAKERY),
            item("milk", GroceryCategory.DAIRY),
            item("apple", GroceryCategory.PRODUCE),
        )

        val grouped = items.groupedByCategory()

        assertEquals(
            listOf(GroceryCategory.PRODUCE, GroceryCategory.DAIRY, GroceryCategory.BAKERY),
            grouped.keys.toList(),
        )
    }

    @Test
    fun dropsCategoriesWithNoItems() {
        val items = listOf(item("apple", GroceryCategory.PRODUCE))

        val grouped = items.groupedByCategory()

        assertEquals(setOf(GroceryCategory.PRODUCE), grouped.keys)
        assertEquals(1, grouped.getValue(GroceryCategory.PRODUCE).size)
    }
}
