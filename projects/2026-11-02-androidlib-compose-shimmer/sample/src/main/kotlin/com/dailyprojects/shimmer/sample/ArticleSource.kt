package com.dailyprojects.shimmer.sample

import android.content.Context
import kotlinx.coroutines.delay
import org.json.JSONArray

data class Article(val title: String, val author: String, val summary: String)

/** Where articles come from. A real HTTP client would replace the one class below. */
interface ArticleSource {
    suspend fun load(): List<Article>
}

/** Reads assets/mock/articles.json after a short delay so the skeleton is visible. */
class AssetArticleSource(private val context: Context) : ArticleSource {
    override suspend fun load(): List<Article> {
        delay(2500)
        val json = context.assets.open("mock/articles.json").bufferedReader().use { it.readText() }
        val array = JSONArray(json)
        return (0 until array.length()).map {
            val o = array.getJSONObject(it)
            Article(o.getString("title"), o.getString("author"), o.getString("summary"))
        }
    }
}
