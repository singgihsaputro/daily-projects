package com.dailyprojects.habitsync.web

import com.dailyprojects.habitsync.CheckInRequest
import com.dailyprojects.habitsync.CreateHabitRequest
import com.dailyprojects.habitsync.Habit
import com.dailyprojects.habitsync.HabitRepository
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
class HttpHabitRepository(private val baseUrl: String = "http://localhost:8080") : HabitRepository {
    private val client = HttpClient(Js) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    override suspend fun list(): List<Habit> = client.get("$baseUrl/habits").body()

    override suspend fun add(name: String, goalPerWeek: Int): Habit =
        client.post("$baseUrl/habits") {
            contentType(ContentType.Application.Json)
            setBody(CreateHabitRequest(name, goalPerWeek))
        }.body()

    override suspend fun toggleCheckIn(id: Int, date: String): Habit? {
        val response = client.post("$baseUrl/habits/$id/checkins") {
            contentType(ContentType.Application.Json)
            setBody(CheckInRequest(date))
        }
        return if (response.status == HttpStatusCode.OK) response.body() else null
    }

    override suspend fun remove(id: Int): Boolean =
        client.delete("$baseUrl/habits/$id").status == HttpStatusCode.NoContent
}
