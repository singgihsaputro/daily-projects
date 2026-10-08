package com.dailyprojects.bookmarks.web

import com.dailyprojects.bookmarks.Bookmark
import com.dailyprojects.bookmarks.BookmarkRepository
import com.dailyprojects.bookmarks.CreateBookmarkRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** Talks to the real `server/` module over HTTP, from the browser. */
class HttpBookmarkRepository(private val baseUrl: String = "http://localhost:8080") : BookmarkRepository {
    private val client = HttpClient(Js) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    override suspend fun list(): List<Bookmark> = client.get("$baseUrl/bookmarks").body()

    override suspend fun add(title: String, url: String, tag: String): Bookmark =
        client.post("$baseUrl/bookmarks") {
            contentType(ContentType.Application.Json)
            setBody(CreateBookmarkRequest(title, url, tag))
        }.body()

    override suspend fun toggleStar(id: Int): Bookmark? {
        val response = client.post("$baseUrl/bookmarks/$id/star")
        return if (response.status == HttpStatusCode.OK) response.body() else null
    }

    override suspend fun remove(id: Int): Boolean =
        client.delete("$baseUrl/bookmarks/$id").status == HttpStatusCode.NoContent
}
