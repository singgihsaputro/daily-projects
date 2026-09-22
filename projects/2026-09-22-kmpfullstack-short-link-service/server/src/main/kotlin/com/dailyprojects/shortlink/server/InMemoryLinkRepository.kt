package com.dailyprojects.shortlink.server

import com.dailyprojects.shortlink.LinkRepository
import com.dailyprojects.shortlink.ShortCodeGenerator
import com.dailyprojects.shortlink.ShortLink
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import java.time.Instant

/** The server's source of truth: an in-memory list seeded from `mock/links.json`, guarded by a mutex. */
class InMemoryLinkRepository(seed: List<ShortLink>) : LinkRepository {
    private val mutex = Mutex()
    private val links = seed.toMutableList()

    override suspend fun list(): List<ShortLink> = mutex.withLock { links.toList() }

    override suspend fun create(longUrl: String): ShortLink = mutex.withLock {
        val link = ShortLink(
            code = ShortCodeGenerator.generate(links.map { it.code }.toSet()),
            longUrl = longUrl,
            createdAtEpochSeconds = Instant.now().epochSecond,
        )
        links += link
        link
    }

    override suspend fun visit(code: String): ShortLink? = mutex.withLock {
        val index = links.indexOfFirst { it.code == code }
        if (index == -1) return@withLock null
        val updated = links[index].copy(clicks = links[index].clicks + 1)
        links[index] = updated
        updated
    }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun seededFromResource(resourcePath: String = "/mock/links.json"): InMemoryLinkRepository {
            val text = InMemoryLinkRepository::class.java.getResourceAsStream(resourcePath)
                ?.bufferedReader()?.readText()
                ?: error("missing seed resource $resourcePath")
            return InMemoryLinkRepository(json.decodeFromString<List<ShortLink>>(text))
        }
    }
}
