package com.dailyprojects.notestore.app

import com.dailyprojects.notestore.CreateNoteRequest
import com.dailyprojects.notestore.Note
import com.dailyprojects.notestore.NoteRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
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

/**
 * Talks to the real `server/` module over HTTP. 10.0.2.2 is the Android emulator's
 * alias for the host machine's localhost, where `./gradlew :server:run` listens.
 */
class HttpNoteRepository(private val baseUrl: String = "http://10.0.2.2:8080") : NoteRepository {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    override suspend fun list(): List<Note> = client.get("$baseUrl/notes").body()

    override suspend fun add(title: String, body: String): Note =
        client.post("$baseUrl/notes") {
            contentType(ContentType.Application.Json)
            setBody(CreateNoteRequest(title, body))
        }.body()

    override suspend fun togglePin(id: Int): Note? {
        val response = client.post("$baseUrl/notes/$id/pin")
        return if (response.status == HttpStatusCode.OK) response.body() else null
    }

    override suspend fun remove(id: Int): Boolean =
        client.delete("$baseUrl/notes/$id").status == HttpStatusCode.NoContent
}
