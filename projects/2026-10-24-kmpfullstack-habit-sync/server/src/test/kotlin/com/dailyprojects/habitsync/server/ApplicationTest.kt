package com.dailyprojects.habitsync.server

import com.dailyprojects.habitsync.Habit
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ApplicationTest {
    private val today = LocalDate.parse("2026-10-24")

    private fun ApplicationTestBuilder.start() = application {
        habitModule(
            today = { today },
            repository = InMemoryHabitRepository(listOf(Habit(1, "Read", 7, listOf("2026-10-23")), Habit(2, "Run", 3))),
        )
    }

    private suspend fun ApplicationTestBuilder.postJson(path: String, body: String) =
        client.post(path) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }

    @Test
    fun listsSeededHabits() = testApplication {
        start()
        val response = client.get("/habits")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("\"Read\"") && response.bodyAsText().contains("2026-10-23"))
    }

    @Test
    fun createsValidAndRejectsInvalid() = testApplication {
        start()
        val created = postJson("/habits", """{"name":" Journal ","goalPerWeek":4}""")
        assertEquals(HttpStatusCode.Created, created.status)
        assertTrue(created.bodyAsText().contains("\"id\": 3") && created.bodyAsText().contains("\"Journal\""))

        assertEquals(HttpStatusCode.BadRequest, postJson("/habits", """{"name":"  "}""").status)
        assertEquals(HttpStatusCode.BadRequest, postJson("/habits", """{"name":"x","goalPerWeek":8}""").status)
        assertEquals(HttpStatusCode.BadRequest, postJson("/habits", """{"name":""").status)
    }

    @Test
    fun checkInTogglesAndValidates() = testApplication {
        start()
        val ticked = postJson("/habits/2/checkins", """{"date":"2026-10-24"}""")
        assertEquals(HttpStatusCode.OK, ticked.status)
        assertTrue(ticked.bodyAsText().contains("2026-10-24"))
        assertFalse(postJson("/habits/2/checkins", """{"date":"2026-10-24"}""").bodyAsText().contains("2026-10-24"))

        assertEquals(HttpStatusCode.BadRequest, postJson("/habits/2/checkins", """{"date":"2026-10-25"}""").status)
        assertEquals(HttpStatusCode.BadRequest, postJson("/habits/2/checkins", """{"date":"tomorrow"}""").status)
        assertEquals(HttpStatusCode.NotFound, postJson("/habits/99/checkins", """{"date":"2026-10-24"}""").status)
        assertEquals(HttpStatusCode.BadRequest, postJson("/habits/abc/checkins", """{"date":"2026-10-24"}""").status)
    }

    @Test
    fun deleteRemovesOnce() = testApplication {
        start()
        assertEquals(HttpStatusCode.NoContent, client.delete("/habits/1").status)
        assertEquals(HttpStatusCode.NotFound, client.delete("/habits/1").status)
        assertFalse(client.get("/habits").bodyAsText().contains("\"Read\""))
    }

    @Test
    fun seedFileProducesLiveStreaks() {
        val repo = InMemoryHabitRepository.seededFromResource(today)
        val read = kotlinx.coroutines.runBlocking { repo.list() }.first { it.id == 1 }
        assertTrue("2026-10-24" in read.checkIns && "2026-10-20" in read.checkIns)
    }
}
