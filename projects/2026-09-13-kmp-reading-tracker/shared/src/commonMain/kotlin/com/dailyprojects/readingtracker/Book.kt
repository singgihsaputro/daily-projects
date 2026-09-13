package com.dailyprojects.readingtracker

enum class ReadingStatus {
    WANT_TO_READ,
    READING,
    FINISHED,
}

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val totalPages: Int,
    val pagesRead: Int,
    val status: ReadingStatus,
) {
    val progress: Float
        get() = if (totalPages == 0) 0f else (pagesRead.toFloat() / totalPages).coerceIn(0f, 1f)

    /** Advances progress by [pages], flipping status to READING/FINISHED as the page count crosses those lines. */
    fun withPagesAdded(pages: Int): Book {
        val updatedPages = (pagesRead + pages).coerceIn(0, totalPages)
        val updatedStatus = when {
            updatedPages >= totalPages -> ReadingStatus.FINISHED
            updatedPages > 0 -> ReadingStatus.READING
            else -> ReadingStatus.WANT_TO_READ
        }
        return copy(pagesRead = updatedPages, status = updatedStatus)
    }
}
