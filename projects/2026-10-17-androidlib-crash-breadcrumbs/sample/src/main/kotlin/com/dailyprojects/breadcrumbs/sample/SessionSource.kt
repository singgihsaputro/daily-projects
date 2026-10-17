package com.dailyprojects.breadcrumbs.sample

import android.content.Context
import com.dailyprojects.breadcrumbs.Level
import org.json.JSONArray

data class SessionStep(val category: String, val message: String, val level: Level)

/** Where the scripted "user session" comes from. Swap for a real feed by changing one class. */
interface SessionSource {
    fun steps(): List<SessionStep>
}

class AssetSessionSource(private val context: Context) : SessionSource {
    override fun steps(): List<SessionStep> {
        val json = context.assets.open("mock/session.json").bufferedReader().use { it.readText() }
        val array = JSONArray(json)
        return (0 until array.length()).map {
            val o = array.getJSONObject(it)
            SessionStep(o.getString("category"), o.getString("message"), Level.valueOf(o.getString("level")))
        }
    }
}
