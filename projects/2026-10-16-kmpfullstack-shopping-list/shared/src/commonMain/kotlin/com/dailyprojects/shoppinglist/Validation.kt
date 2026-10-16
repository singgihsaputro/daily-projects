package com.dailyprojects.shoppinglist

const val MAX_NAME_LENGTH = 60
const val MAX_QUANTITY = 99

/** Categories are single lowercase words so "Dairy " and "dairy" land in the same aisle. */
fun normalizeCategory(value: String): String = value.trim().lowercase().replace(Regex("\\s+"), "-")
