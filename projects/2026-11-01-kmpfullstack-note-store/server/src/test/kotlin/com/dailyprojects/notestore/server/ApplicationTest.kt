package com.dailyprojects.notestore.server

import com.dailyprojects.notestore.Note
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
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ApplicationTest {
    private fun ApplicationTestBuilder.start() = application {
        noteModule(InMemoryNoteRepository(listOf(Note(1, "Alpha", "first"), Note(2, "Beta", "second", pinned = true))))
    }

    private suspend fun ApplicationTestBuilder.postJson(path: String, body: String) =
        client.post(path) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }

    @Test
    fun listsSeededNotes() = testApplication {
        start()
        val response = client.get("/notes")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("\"Alpha\"") && response.bodyAsText().contains("\"Beta\""))
    }

    @Test
    fun createsValidAndRejectsInvalid() = testApplication {
        start()
        val created = postJson("/notes", """{"title":"  Soup  ","body":"leek"}""")
        assertEquals(HttpStatusCode.Created, created.status)
        assertTrue(created.bodyAsText().contains("\"id\": 3") && created.bodyAsText().contains("\"Soup\""))

        assertEquals(HttpStatusCode.BadRequest, postJson("/notes", """{"title":"  "}""").status)
        assertEquals(HttpStatusCode.BadRequest, postJson("/notes", """{"title":"${"x".repeat(61)}"}""").status)
        assertEquals(HttpStatusCode.BadRequest, postJson("/notes", """{"title":""").status)
    }

    @Test
    fun pinTogglesAndValidatesId() = testApplication {
        start()
        assertTrue(postJson("/notes/1/pin", "").bodyAsText().contains("\"pinned\": true"))
        assertTrue(postJson("/notes/1/pin", "").bodyAsText().contains("\"pinned\": false"))
        assertEquals(HttpStatusCode.NotFound, postJson("/notes/99/pin", "").status)
        assertEquals(HttpStatusCode.BadRequest, postJson("/notes/abc/pin", "").status)
    }

    @Test
    fun deleteRemovesOnce() = testApplication {
        start()
        assertEquals(HttpStatusCode.NoContent, client.delete("/notes/1").status)
        assertEquals(HttpStatusCode.NotFound, client.delete("/notes/1").status)
        assertFalse(client.get("/notes").bodyAsText().contains("\"Alpha\""))
    }

    @Test
    fun seedFileLoads() {
        val notes = runBlocking { InMemoryNoteRepository.seededFromResource().list() }
        assertEquals(5, notes.size)
        assertEquals(listOf(1, 4), notes.filter { it.pinned }.map { it.id })
    }
}
