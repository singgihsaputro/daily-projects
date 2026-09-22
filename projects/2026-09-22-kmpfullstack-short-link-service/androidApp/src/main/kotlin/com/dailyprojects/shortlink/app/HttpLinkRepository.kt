package com.dailyprojects.shortlink.app

import com.dailyprojects.shortlink.CreateLinkRequest
import com.dailyprojects.shortlink.LinkRepository
import com.dailyprojects.shortlink.ShortLink
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Talks to the real `server/` module over HTTP. 10.0.2.2 is the Android emulator's
 * alias for the host machine's localhost, where `./gradlew :server:run` listens.
 */
class HttpLinkRepository(private val baseUrl: String = "http://10.0.2.2:8080") : LinkRepository {
    private val client = HttpClient(Android) {
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
        // The endpoint answers with a redirect to longUrl; the app only needs the
        // click to be counted, not to actually follow it off-device.
        client.config { followRedirects = false }.get("$baseUrl/r/$code")
        return list().firstOrNull { it.code == code }
    }
}
