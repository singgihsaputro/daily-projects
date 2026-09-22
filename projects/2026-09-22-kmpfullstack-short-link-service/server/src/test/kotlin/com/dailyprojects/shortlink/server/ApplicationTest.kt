package com.dailyprojects.shortlink.server

import com.dailyprojects.shortlink.ShortLink
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplicationTest {

    private fun seedRepository() = InMemoryLinkRepository(
        seed = listOf(ShortLink(code = "abc123", longUrl = "https://example.com/original", createdAtEpochSeconds = 1, clicks = 0)),
    )

    @Test
    fun listsSeededLinks() = testApplication {
        application { shortLinkModule(seedRepository()) }
        val response = client.get("/links")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("abc123"))
    }

    @Test
    fun createsAValidLinkAndRejectsAnInvalidOne() = testApplication {
        application { shortLinkModule(seedRepository()) }
        val client = createClient { install(ClientContentNegotiation) { json() } }

        val created = client.post("/links") {
            contentType(ContentType.Application.Json)
            setBody("""{"longUrl":"https://kotlinlang.org/"}""")
        }
        assertEquals(HttpStatusCode.Created, created.status)
        assertTrue(created.bodyAsText().contains("kotlinlang.org"))

        val rejected = client.post("/links") {
            contentType(ContentType.Application.Json)
            setBody("""{"longUrl":"not-a-url"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, rejected.status)
    }

    @Test
    fun visitingAKnownCodeRedirectsAndCountsAClick() = testApplication {
        application { shortLinkModule(seedRepository()) }
        val client = createClient { followRedirects = false }
        val response = client.get("/r/abc123")
        assertEquals(HttpStatusCode.Found, response.status)
        assertEquals("https://example.com/original", response.headers["Location"])

        val links = client.get("/links").bodyAsText().replace(" ", "").replace("\n", "")
        assertTrue(links.contains("\"clicks\":1"))
    }

    @Test
    fun visitingAnUnknownCodeReturns404() = testApplication {
        application { shortLinkModule(seedRepository()) }
        val response = client.get("/r/does-not-exist")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}
