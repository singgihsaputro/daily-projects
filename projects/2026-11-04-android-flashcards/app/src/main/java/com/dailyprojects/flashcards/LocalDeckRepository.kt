package com.dailyprojects.flashcards

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/** Reads assets/mock/flashcards.json. Swap this class for a network client to go live. */
class LocalDeckRepository(private val context: Context) : DeckRepository {
    override suspend fun load(): Deck = withContext(Dispatchers.IO) {
        val text = context.assets.open("mock/flashcards.json").bufferedReader().use { it.readText() }
        val root = JSONObject(text)
        val array = root.getJSONArray("cards")
        val cards = List(array.length()) { i ->
            val o = array.getJSONObject(i)
            Card(o.getString("id"), o.getString("front"), o.getString("back"))
        }
        Deck(root.getString("deck"), cards)
    }
}
