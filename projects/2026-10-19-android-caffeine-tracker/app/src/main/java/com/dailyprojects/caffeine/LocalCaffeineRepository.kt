package com.dailyprojects.caffeine

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/** Reads assets/mock/caffeine.json. Swap this class for a network client to go live. */
class LocalCaffeineRepository(private val context: Context) : CaffeineRepository {
    override suspend fun load(): CaffeineData = withContext(Dispatchers.IO) {
        val text = context.assets.open("mock/caffeine.json").bufferedReader().use { it.readText() }
        val root = JSONObject(text)
        val drinkArray = root.getJSONArray("drinks")
        val drinks = List(drinkArray.length()) { i ->
            val o = drinkArray.getJSONObject(i)
            Drink(o.getString("id"), o.getString("name"), o.getInt("mg"))
        }
        val byId = drinks.associateBy { it.id }
        val logArray = root.getJSONArray("log")
        val log = List(logArray.length()) { i ->
            val o = logArray.getJSONObject(i)
            Entry(byId.getValue(o.getString("drinkId")), o.getInt("minutesAgo"))
        }
        CaffeineData(root.getInt("dailyLimitMg"), drinks, log)
    }
}
