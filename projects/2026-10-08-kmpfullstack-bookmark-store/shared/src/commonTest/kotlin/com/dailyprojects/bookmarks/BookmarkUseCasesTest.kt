package com.dailyprojects.bookmarks

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

private class FakeBookmarkRepository(seed: List<Bookmark> = emptyList()) : BookmarkRepository {
    private val items = seed.toMutableList()
    override suspend fun list(): List<Bookmark> = items.toList()
    override suspend fun add(title: String, url: String, tag: String): Bookmark {
        val bookmark = Bookmark(id = items.size + 1, title = title, url = url, tag = tag, createdAtEpochSeconds = items.size.toLong())
        items += bookmark
        return bookmark
    }
    override suspend fun toggleStar(id: Int): Bookmark? {
        val index = items.indexOfFirst { it.id == id }
        if (index == -1) return null
        items[index] = items[index].copy(starred = !items[index].starred)
        return items[index]
    }
    override suspend fun remove(id: Int): Boolean = items.removeAll { it.id == id }
}

class BookmarkUseCasesTest {
    @Test
    fun addRejectsBlankTitleAndBadUrl() = runTest {
        val add = AddBookmarkUseCase(FakeBookmarkRepository())
        assertIs<BookmarkResult.Failure>(add("  ", "https://example.com"))
        assertIs<BookmarkResult.Failure>(add("Docs", "not-a-url"))
        assertIs<BookmarkResult.Failure>(add("x".repeat(MAX_TITLE_LENGTH + 1), "https://example.com"))
    }

    @Test
    fun addTrimsTitleAndNormalizesTag() = runTest {
        val result = AddBookmarkUseCase(FakeBookmarkRepository())("  Ktor docs ", " https://ktor.io ", "Server Side")
        val saved = assertIs<BookmarkResult.Success<Bookmark>>(result).value
        assertEquals("Ktor docs", saved.title)
        assertEquals("https://ktor.io", saved.url)
        assertEquals("server-side", saved.tag)
    }

    @Test
    fun listPutsStarredFirstThenNewestAndFiltersByTag() = runTest {
        val repo = FakeBookmarkRepository(
            listOf(
                Bookmark(1, "old", "https://a.io", "kotlin", starred = false, createdAtEpochSeconds = 1),
                Bookmark(2, "new", "https://b.io", "kotlin", starred = false, createdAtEpochSeconds = 9),
                Bookmark(3, "starred", "https://c.io", "swift", starred = true, createdAtEpochSeconds = 0),
            ),
        )
        val list = ListBookmarksUseCase(repo)
        assertEquals(listOf(3, 2, 1), list().map { it.id })
        assertEquals(listOf(2, 1), list("Kotlin").map { it.id })
    }

    @Test
    fun toggleStarFlipsAndReportsUnknownIds() = runTest {
        val repo = FakeBookmarkRepository(listOf(Bookmark(1, "a", "https://a.io", createdAtEpochSeconds = 1)))
        val toggle = ToggleStarUseCase(repo)
        assertTrue(assertIs<BookmarkResult.Success<Bookmark>>(toggle(1)).value.starred)
        assertIs<BookmarkResult.Failure>(toggle(99))
    }

    @Test
    fun removeDeletesOnceThenFails() = runTest {
        val remove = RemoveBookmarkUseCase(FakeBookmarkRepository(listOf(Bookmark(1, "a", "https://a.io", createdAtEpochSeconds = 1))))
        assertIs<BookmarkResult.Success<Unit>>(remove(1))
        assertIs<BookmarkResult.Failure>(remove(1))
    }
}
