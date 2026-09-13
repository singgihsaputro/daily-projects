package com.dailyprojects.readingtracker

/** Source of the reading list. Swap [MockBookRepository] for a networked implementation without touching callers. */
interface BookRepository {
    suspend fun loadBooks(): List<Book>
}

/** Reads the raw `mock/books.json` text. Each platform bundles and opens that fixture differently. */
interface BookJsonSource {
    fun readJson(): String
}
