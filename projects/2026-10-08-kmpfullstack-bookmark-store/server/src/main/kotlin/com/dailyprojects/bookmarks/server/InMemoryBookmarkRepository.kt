package com.dailyprojects.bookmarks.server

import com.dailyprojects.bookmarks.Bookmark
import com.dailyprojects.bookmarks.BookmarkRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import java.time.Instant

/** The server's source of truth: an in-memory list seeded from `mock/bookmarks.json`, guarded by a mutex. */
class InMemoryBookmarkRepository(seed: List<Bookmark>) : BookmarkRepository {
    private val mutex = Mutex()
    private val items = seed.toMutableList()
    private var nextId = (seed.maxOfOrNull { it.id } ?: 0) + 1

    override suspend fun list(): List<Bookmark> = mutex.withLock { items.toList() }

    override suspend fun add(title: String, url: String, tag: String): Bookmark = mutex.withLock {
        val bookmark = Bookmark(
            id = nextId++,
            title = title,
            url = url,
            tag = tag,
            createdAtEpochSeconds = Instant.now().epochSecond,
        )
        items += bookmark
        bookmark
    }

    override suspend fun toggleStar(id: Int): Bookmark? = mutex.withLock {
        val index = items.indexOfFirst { it.id == id }
        if (index == -1) return@withLock null
        val updated = items[index].copy(starred = !items[index].starred)
        items[index] = updated
        updated
    }

    override suspend fun remove(id: Int): Boolean = mutex.withLock { items.removeAll { it.id == id } }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun seededFromResource(resourcePath: String = "/bookmarks.json"): InMemoryBookmarkRepository {
            val text = InMemoryBookmarkRepository::class.java.getResourceAsStream(resourcePath)
                ?.bufferedReader()?.readText()
                ?: error("missing seed resource $resourcePath")
            return InMemoryBookmarkRepository(json.decodeFromString<List<Bookmark>>(text))
        }
    }
}
