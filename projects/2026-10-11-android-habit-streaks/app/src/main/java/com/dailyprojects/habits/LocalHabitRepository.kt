package com.dailyprojects.habits

import android.content.Context
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

/** Reads assets/mock/habits.json. Swap this class for a network client to go live. */
class LocalHabitRepository(private val context: Context) : HabitRepository {
    override suspend fun habits(today: LocalDate): List<Habit> = withContext(Dispatchers.IO) {
        val text = context.assets.open("mock/habits.json").bufferedReader().use { it.readText() }
        val array = JSONArray(text)
        List(array.length()) { i ->
            val o = array.getJSONObject(i)
            val ago = o.getJSONArray("doneDaysAgo")
            Habit(
                id = o.getInt("id"),
                name = o.getString("name"),
                emoji = o.getString("emoji"),
                doneDays = List(ago.length()) { today.minusDays(ago.getLong(it)) }.toSet(),
            )
        }
    }
}
