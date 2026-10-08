package com.dailyprojects.bookmarks.server

import com.dailyprojects.bookmarks.Bookmark
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ApplicationTest {

    private fun seedRepository() = InMemoryBookmarkRepository(
        seed = listOf(
            Bookmark(1, "First", "https://example.com/one", "kotlin", starred = false, createdAtEpochSeconds = 1),
            Bookmark(2, "Second", "https://example.com/two", "swift", starred = false, createdAtEpochSeconds = 2),
        ),
    )

    @Test
    fun listsAndFiltersByTag() = testApplication {
        application { bookmarkModule(seedRepository()) }
        val all = client.get("/bookmarks")
        assertEquals(HttpStatusCode.OK, all.status)
        assertTrue(all.bodyAsText().contains("First") && all.bodyAsText().contains("Second"))

        val swiftOnly = client.get("/bookmarks?tag=Swift").bodyAsText()
        assertTrue(swiftOnly.contains("Second"))
        assertFalse(swiftOnly.contains("First"))
    }

    @Test
    fun createsValidAndRejectsInvalid() = testApplication {
        application { bookmarkModule(seedRepository()) }

        val created = client.post("/bookmarks") {
            contentType(ContentType.Application.Json)
            setBody("""{"title":"Docs","url":"https://kotlinlang.org/","tag":"Kotlin"}""")
        }
        assertEquals(HttpStatusCode.Created, created.status)
        assertTrue(created.bodyAsText().contains("\"id\": 3"))

        val badUrl = client.post("/bookmarks") {
            contentType(ContentType.Application.Json)
            setBody("""{"title":"Docs","url":"nope"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, badUrl.status)

        val malformed = client.post("/bookmarks") {
            contentType(ContentType.Application.Json)
            setBody("""{"title":""")
        }
        assertEquals(HttpStatusCode.BadRequest, malformed.status)
    }

    @Test
    fun starTogglesAndUnknownIdIs404() = testApplication {
        application { bookmarkModule(seedRepository()) }
        val first = client.post("/bookmarks/1/star")
        assertEquals(HttpStatusCode.OK, first.status)
        assertTrue(first.bodyAsText().contains("\"starred\": true"))
        assertTrue(client.post("/bookmarks/1/star").bodyAsText().contains("\"starred\": false"))
        assertEquals(HttpStatusCode.NotFound, client.post("/bookmarks/99/star").status)
        assertEquals(HttpStatusCode.BadRequest, client.post("/bookmarks/abc/star").status)
    }

    @Test
    fun deleteRemovesOnce() = testApplication {
        application { bookmarkModule(seedRepository()) }
        assertEquals(HttpStatusCode.NoContent, client.delete("/bookmarks/1").status)
        assertEquals(HttpStatusCode.NotFound, client.delete("/bookmarks/1").status)
        assertFalse(client.get("/bookmarks").bodyAsText().contains("First"))
    }
}
