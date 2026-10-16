package com.dailyprojects.shoppinglist.web

import com.dailyprojects.shoppinglist.ClearedResponse
import com.dailyprojects.shoppinglist.CreateItemRequest
import com.dailyprojects.shoppinglist.ShoppingItem
import com.dailyprojects.shoppinglist.ShoppingRepository
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
class HttpShoppingRepository(private val baseUrl: String = "http://localhost:8080") : ShoppingRepository {
    private val client = HttpClient(Js) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    override suspend fun list(): List<ShoppingItem> = client.get("$baseUrl/items").body()

    override suspend fun add(name: String, quantity: Int, category: String): ShoppingItem =
        client.post("$baseUrl/items") {
            contentType(ContentType.Application.Json)
            setBody(CreateItemRequest(name, quantity, category))
        }.body()

    override suspend fun toggleChecked(id: Int): ShoppingItem? {
        val response = client.post("$baseUrl/items/$id/check")
        return if (response.status == HttpStatusCode.OK) response.body() else null
    }

    override suspend fun remove(id: Int): Boolean =
        client.delete("$baseUrl/items/$id").status == HttpStatusCode.NoContent

    override suspend fun clearChecked(): Int = client.delete("$baseUrl/items/checked").body<ClearedResponse>().removed
}
