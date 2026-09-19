package com.dailyprojects.pwgen

import android.content.Context
import org.json.JSONObject

/** Reads the default generator options and generation history from the
 *  bundled `mock/password_history.json` fixture. */
class LocalPasswordHistoryRepository(private val context: Context) : PasswordHistoryRepository {

    override fun loadFixture(): PasswordSettingsFixture {
        val text = context.assets.open("mock/password_history.json")
            .bufferedReader()
            .use { it.readText() }

        val root = JSONObject(text)

        val defaultOptions = PasswordOptions(
            length = root.getInt("defaultLength"),
            useUppercase = root.getBoolean("defaultUseUppercase"),
            useLowercase = root.getBoolean("defaultUseLowercase"),
            useNumbers = root.getBoolean("defaultUseNumbers"),
            useSymbols = root.getBoolean("defaultUseSymbols"),
        )

        val historyArray = root.getJSONArray("history")
        val history = (0 until historyArray.length()).map { index ->
            val entry = historyArray.getJSONObject(index)
            HistoryEntry(
                id = entry.getString("id"),
                password = entry.getString("password"),
                length = entry.getInt("length"),
                strength = entry.getString("strength"),
                time = entry.getString("time"),
            )
        }

        return PasswordSettingsFixture(defaultOptions = defaultOptions, history = history)
    }
}
