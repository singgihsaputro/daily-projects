package com.dailyprojects.redactlog.sample

import android.content.Context
import org.json.JSONArray

/** Where the raw log lines come from. Swap for a real feed by changing one class. */
interface LineSource {
    fun lines(): List<String>
}

class AssetLineSource(private val context: Context) : LineSource {
    override fun lines(): List<String> {
        val json = context.assets.open("mock/lines.json").bufferedReader().use { it.readText() }
        val array = JSONArray(json)
        return (0 until array.length()).map { array.getString(it) }
    }
}
