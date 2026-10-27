package com.dailyprojects.envelopes

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/** Reads assets/mock/budget.json. Swap this class for a network client to go live. */
class LocalBudgetRepository(private val context: Context) : BudgetRepository {
    override suspend fun load(): BudgetData = withContext(Dispatchers.IO) {
        val text = context.assets.open("mock/budget.json").bufferedReader().use { it.readText() }
        val root = JSONObject(text)
        val array = root.getJSONArray("envelopes")
        val envelopes = List(array.length()) { i ->
            val o = array.getJSONObject(i)
            Envelope(o.getString("id"), o.getString("name"), o.getInt("budget"), o.getInt("spent"))
        }
        BudgetData(root.getString("month"), root.getInt("daysLeft"), envelopes)
    }
}
