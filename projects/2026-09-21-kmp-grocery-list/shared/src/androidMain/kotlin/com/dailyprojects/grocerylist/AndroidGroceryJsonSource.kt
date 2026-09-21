package com.dailyprojects.grocerylist

import android.content.Context

/** Reads the fixture bundled as an Android asset at `assets/mock/groceries.json`. */
class AndroidGroceryJsonSource(private val context: Context) : GroceryJsonSource {
    override fun readJson(): String =
        context.assets.open("mock/groceries.json").bufferedReader().use { it.readText() }
}
