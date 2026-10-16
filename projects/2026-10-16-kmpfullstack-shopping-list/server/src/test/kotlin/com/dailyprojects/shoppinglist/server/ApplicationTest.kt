package com.dailyprojects.shoppinglist.server

import com.dailyprojects.shoppinglist.ShoppingItem
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

    private fun seedRepository() = InMemoryShoppingRepository(
        seed = listOf(
            ShoppingItem(1, "First", 1, "dairy", checked = false, createdAtEpochSeconds = 1),
            ShoppingItem(2, "Second", 3, "produce", checked = true, createdAtEpochSeconds = 2),
        ),
    )

    @Test
    fun listsAndFiltersByCategory() = testApplication {
        application { shoppingModule(seedRepository()) }
        val all = client.get("/items")
        assertEquals(HttpStatusCode.OK, all.status)
        assertTrue(all.bodyAsText().contains("\"First\"") && all.bodyAsText().contains("\"Second\""))

        val produceOnly = client.get("/items?category=Produce").bodyAsText()
        assertTrue(produceOnly.contains("\"Second\""))
        assertFalse(produceOnly.contains("\"First\""))
    }

    @Test
    fun createsValidAndRejectsInvalid() = testApplication {
        application { shoppingModule(seedRepository()) }

        val created = client.post("/items") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"Eggs","quantity":12,"category":"Dairy"}""")
        }
        assertEquals(HttpStatusCode.Created, created.status)
        assertTrue(created.bodyAsText().contains("\"id\": 3"))

        val badQuantity = client.post("/items") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"Eggs","quantity":0}""")
        }
        assertEquals(HttpStatusCode.BadRequest, badQuantity.status)

        val blankName = client.post("/items") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"  "}""")
        }
        assertEquals(HttpStatusCode.BadRequest, blankName.status)

        val malformed = client.post("/items") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":""")
        }
        assertEquals(HttpStatusCode.BadRequest, malformed.status)
    }

    @Test
    fun checkTogglesAndUnknownIdIs404() = testApplication {
        application { shoppingModule(seedRepository()) }
        val first = client.post("/items/1/check")
        assertEquals(HttpStatusCode.OK, first.status)
        assertTrue(first.bodyAsText().contains("\"checked\": true"))
        assertTrue(client.post("/items/1/check").bodyAsText().contains("\"checked\": false"))
        assertEquals(HttpStatusCode.NotFound, client.post("/items/99/check").status)
        assertEquals(HttpStatusCode.BadRequest, client.post("/items/abc/check").status)
    }

    @Test
    fun deleteRemovesOnce() = testApplication {
        application { shoppingModule(seedRepository()) }
        assertEquals(HttpStatusCode.NoContent, client.delete("/items/1").status)
        assertEquals(HttpStatusCode.NotFound, client.delete("/items/1").status)
        assertFalse(client.get("/items").bodyAsText().contains("\"First\""))
    }

    @Test
    fun clearCheckedRemovesOnlyTickedItems() = testApplication {
        application { shoppingModule(seedRepository()) }
        val cleared = client.delete("/items/checked")
        assertEquals(HttpStatusCode.OK, cleared.status)
        assertTrue(cleared.bodyAsText().contains("\"removed\": 1"))
        val remaining = client.get("/items").bodyAsText()
        assertTrue(remaining.contains("\"First\""))
        assertFalse(remaining.contains("\"Second\""))
    }
}
