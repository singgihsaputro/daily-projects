package com.dailyprojects.readingtracker

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReadingListState(
    val books: List<Book> = emptyList(),
    val isLoading: Boolean = true,
) {
    val booksFinished: Int get() = books.count { it.status == ReadingStatus.FINISHED }
    val pagesReadTotal: Int get() = books.sumOf { it.pagesRead }
}

/** Platform-agnostic state holder: both the Android and iOS UIs drive their screen from the same [state]. */
class ReadingListModel(
    private val repository: BookRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob()),
) {
    private val _state = MutableStateFlow(ReadingListState())
    val state: StateFlow<ReadingListState> = _state.asStateFlow()

    fun load() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val books = repository.loadBooks()
            _state.value = ReadingListState(books = books, isLoading = false)
        }
    }

    fun addPages(bookId: String, pages: Int) {
        val updated = _state.value.books.map { book ->
            if (book.id == bookId) book.withPagesAdded(pages) else book
        }
        _state.value = _state.value.copy(books = updated)
    }
}
