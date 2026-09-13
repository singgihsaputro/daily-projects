package com.dailyprojects.readingtracker

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class BookDto(
    val id: String,
    val title: String,
    val author: String,
    val totalPages: Int,
    val pagesRead: Int,
    val status: String,
)

/** Parses the bundled fixture from [source] into domain [Book]s. */
class MockBookRepository(private val source: BookJsonSource) : BookRepository {

    override suspend fun loadBooks(): List<Book> {
        val dtos = Json.decodeFromString<List<BookDto>>(source.readJson())
        return dtos.map { dto ->
            Book(
                id = dto.id,
                title = dto.title,
                author = dto.author,
                totalPages = dto.totalPages,
                pagesRead = dto.pagesRead,
                status = ReadingStatus.valueOf(dto.status),
            )
        }
    }
}
