package com.dailyprojects.countdown

import android.content.Context
import java.time.LocalDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

/** Reads assets/mock/events.json. Swap this class for a network client to go live. */
class LocalEventRepository(private val context: Context) : EventRepository {
    override suspend fun events(): List<CountdownEvent> = withContext(Dispatchers.IO) {
        val text = context.assets.open("mock/events.json").bufferedReader().use { it.readText() }
        val array = JSONArray(text)
        List(array.length()) { i ->
            val o = array.getJSONObject(i)
            CountdownEvent(
                id = o.getInt("id"),
                title = o.getString("title"),
                emoji = o.getString("emoji"),
                date = LocalDateTime.parse(o.getString("date")),
            )
        }
    }
}
