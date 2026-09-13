package com.dailyprojects.readingtracker

import android.content.Context

/** Reads the fixture bundled as an Android asset at `assets/mock/books.json`. */
class AndroidBookJsonSource(private val context: Context) : BookJsonSource {
    override fun readJson(): String =
        context.assets.open("mock/books.json").bufferedReader().use { it.readText() }
}
