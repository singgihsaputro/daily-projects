package com.dailyprojects.notestore

/** The one contract every target codes against — an in-memory store on the server, HTTP on every client. */
interface NoteRepository {
    suspend fun list(): List<Note>
    suspend fun add(title: String, body: String): Note

    /** Flips the pinned flag. Null when the id is unknown. */
    suspend fun togglePin(id: Int): Note?
    suspend fun remove(id: Int): Boolean
}
