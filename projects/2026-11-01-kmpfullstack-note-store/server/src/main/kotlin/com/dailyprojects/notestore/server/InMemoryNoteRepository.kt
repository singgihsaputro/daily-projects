package com.dailyprojects.notestore.server

import com.dailyprojects.notestore.Note
import com.dailyprojects.notestore.NoteRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

/** The server's source of truth: an in-memory list guarded by a mutex. */
class InMemoryNoteRepository(seed: List<Note> = emptyList()) : NoteRepository {
    private val mutex = Mutex()
    private val notes = seed.toMutableList()
    private var nextId = (seed.maxOfOrNull { it.id } ?: 0) + 1

    override suspend fun list(): List<Note> = mutex.withLock { notes.toList() }

    override suspend fun add(title: String, body: String): Note = mutex.withLock {
        Note(id = nextId++, title = title, body = body).also { notes += it }
    }

    override suspend fun togglePin(id: Int): Note? = mutex.withLock {
        val index = notes.indexOfFirst { it.id == id }
        if (index == -1) return@withLock null
        notes[index].copy(pinned = !notes[index].pinned).also { notes[index] = it }
    }

    override suspend fun remove(id: Int): Boolean = mutex.withLock { notes.removeAll { it.id == id } }

    companion object {
        /** Reads `mock/notes.json`, bundled onto the classpath by server/build.gradle.kts. */
        fun seededFromResource(resourcePath: String = "/notes.json"): InMemoryNoteRepository {
            val text = InMemoryNoteRepository::class.java.getResourceAsStream(resourcePath)
                ?.bufferedReader()?.readText()
                ?: error("missing seed resource $resourcePath")
            return InMemoryNoteRepository(Json { ignoreUnknownKeys = true }.decodeFromString(text))
        }
    }
}
