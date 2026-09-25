package com.dailyprojects.qrgen

import android.content.Context
import org.json.JSONObject

/** Reads starter presets and generation history from the bundled
 *  `mock/qr_history.json` fixture. */
class LocalQrHistoryRepository(private val context: Context) : QrHistoryRepository {

    override fun loadFixture(): QrFixture {
        val text = context.assets.open("mock/qr_history.json")
            .bufferedReader()
            .use { it.readText() }

        val root = JSONObject(text)

        val presetsArray = root.getJSONArray("presets")
        val presets = (0 until presetsArray.length()).map { index ->
            val entry = presetsArray.getJSONObject(index)
            QrPreset(label = entry.getString("label"), content = entry.getString("content"))
        }

        val historyArray = root.getJSONArray("history")
        val history = (0 until historyArray.length()).map { index ->
            val entry = historyArray.getJSONObject(index)
            QrHistoryEntry(
                id = entry.getString("id"),
                content = entry.getString("content"),
                time = entry.getString("time"),
            )
        }

        return QrFixture(presets = presets, history = history)
    }
}
