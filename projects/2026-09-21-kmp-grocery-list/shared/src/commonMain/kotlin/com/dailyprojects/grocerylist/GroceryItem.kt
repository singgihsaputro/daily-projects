package com.dailyprojects.grocerylist

enum class GroceryCategory {
    PRODUCE,
    DAIRY,
    BAKERY,
    MEAT,
    PANTRY,
    HOUSEHOLD,
}

data class GroceryItem(
    val id: String,
    val name: String,
    val quantity: Int,
    val category: GroceryCategory,
    val checked: Boolean = false,
)
