package com.dailyprojects.pomodoro

import android.content.Context
import org.json.JSONArray

/** Reads timer presets from the bundled `mock/presets.json` fixture. */
class LocalPresetRepository(private val context: Context) : PresetRepository {

    override fun loadPresets(): List<TimerPreset> {
        val text = context.assets.open("mock/presets.json")
            .bufferedReader()
            .use { it.readText() }

        val array = JSONArray(text)
        return (0 until array.length()).map { index ->
            val entry = array.getJSONObject(index)
            TimerPreset(
                id = entry.getString("id"),
                label = entry.getString("label"),
                focusMinutes = entry.getInt("focusMinutes"),
                breakMinutes = entry.getInt("breakMinutes"),
            )
        }
    }
}
