package com.dailyprojects.grocerylist

/** Pure grouping helper: buckets items by category, in category-declaration order, dropping empty buckets. */
fun List<GroceryItem>.groupedByCategory(): Map<GroceryCategory, List<GroceryItem>> {
    val byCategory = groupBy { it.category }
    return GroceryCategory.entries
        .mapNotNull { category -> byCategory[category]?.let { category to it } }
        .toMap()
}
