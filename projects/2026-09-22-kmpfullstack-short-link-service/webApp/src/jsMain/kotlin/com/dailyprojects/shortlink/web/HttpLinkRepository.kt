package com.dailyprojects.shortlink.web

import com.dailyprojects.shortlink.CreateLinkRequest
import com.dailyprojects.shortlink.LinkRepository
import com.dailyprojects.shortlink.ShortLink
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** Talks to the real `server/` module over HTTP, from the browser. */
class HttpLinkRepository(private val baseUrl: String = "http://localhost:8080") : LinkRepository {
    private val client = HttpClient(Js) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    override suspend fun list(): List<ShortLink> = client.get("$baseUrl/links").body()

    override suspend fun create(longUrl: String): ShortLink =
        client.post("$baseUrl/links") {
            contentType(ContentType.Application.Json)
            setBody(CreateLinkRequest(longUrl))
        }.body()

    override suspend fun visit(code: String): ShortLink? {
        client.config { followRedirects = false }.get("$baseUrl/r/$code")
        return list().firstOrNull { it.code == code }
    }
}
