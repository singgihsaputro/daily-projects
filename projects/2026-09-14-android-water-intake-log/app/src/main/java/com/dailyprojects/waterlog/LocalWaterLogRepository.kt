package com.dailyprojects.waterlog

import android.content.Context
import org.json.JSONObject

/** Reads today's goal, presets and log entries from the bundled
 *  `mock/water_log.json` fixture. */
class LocalWaterLogRepository(private val context: Context) : WaterLogRepository {

    override fun loadFixture(): WaterLogFixture {
        val text = context.assets.open("mock/water_log.json")
            .bufferedReader()
            .use { it.readText() }

        val root = JSONObject(text)

        val presetsArray = root.getJSONArray("presets")
        val presets = (0 until presetsArray.length()).map { index ->
            val entry = presetsArray.getJSONObject(index)
            DrinkPreset(
                id = entry.getString("id"),
                label = entry.getString("label"),
                amountMl = entry.getInt("amountMl"),
            )
        }

        val entriesArray = root.getJSONArray("entries")
        val entries = (0 until entriesArray.length()).map { index ->
            val entry = entriesArray.getJSONObject(index)
            WaterEntry(
                id = entry.getString("id"),
                label = entry.getString("label"),
                amountMl = entry.getInt("amountMl"),
                time = entry.getString("time"),
            )
        }

        return WaterLogFixture(
            dailyGoalMl = root.getInt("dailyGoalMl"),
            presets = presets,
            entries = entries,
        )
    }
}
