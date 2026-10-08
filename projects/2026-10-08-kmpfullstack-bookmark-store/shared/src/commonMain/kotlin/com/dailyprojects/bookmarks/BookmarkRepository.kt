package com.dailyprojects.bookmarks

/** The one contract every target codes against — an in-memory store on the server, HTTP on every client. */
interface BookmarkRepository {
    suspend fun list(): List<Bookmark>
    suspend fun add(title: String, url: String, tag: String): Bookmark
    suspend fun toggleStar(id: Int): Bookmark?
    suspend fun remove(id: Int): Boolean
}
