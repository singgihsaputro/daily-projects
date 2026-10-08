package com.dailyprojects.bookmarks.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailyprojects.bookmarks.AddBookmarkUseCase
import com.dailyprojects.bookmarks.Bookmark
import com.dailyprojects.bookmarks.BookmarkRepository
import com.dailyprojects.bookmarks.BookmarkResult
import com.dailyprojects.bookmarks.ListBookmarksUseCase
import com.dailyprojects.bookmarks.RemoveBookmarkUseCase
import com.dailyprojects.bookmarks.ToggleStarUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BookmarkUiState(
    val bookmarks: List<Bookmark> = emptyList(),
    val tags: List<String> = emptyList(),
    val selectedTag: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val titleInput: String = "",
    val urlInput: String = "",
    val tagInput: String = "",
)

class BookmarkViewModel(repository: BookmarkRepository = HttpBookmarkRepository()) : ViewModel() {
    private val listBookmarks = ListBookmarksUseCase(repository)
    private val addBookmark = AddBookmarkUseCase(repository)
    private val toggleStar = ToggleStarUseCase(repository)
    private val removeBookmark = RemoveBookmarkUseCase(repository)

    private val _state = MutableStateFlow(BookmarkUiState())
    val state: StateFlow<BookmarkUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun onTitleChange(value: String) = _state.update { it.copy(titleInput = value) }
    fun onUrlChange(value: String) = _state.update { it.copy(urlInput = value) }
    fun onTagChange(value: String) = _state.update { it.copy(tagInput = value) }

    fun selectTag(tag: String) {
        _state.update { it.copy(selectedTag = tag) }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching { listBookmarks(_state.value.selectedTag) to listBookmarks().map { it.tag }.filter { it.isNotEmpty() }.distinct().sorted() }
                .onSuccess { (visible, tags) ->
                    _state.update { it.copy(bookmarks = visible, tags = tags, isLoading = false, errorMessage = null) }
                }
                .onFailure { error -> _state.update { it.copy(isLoading = false, errorMessage = error.message) } }
        }
    }

    fun add() {
        val current = _state.value
        viewModelScope.launch {
            when (val result = addBookmark(current.titleInput, current.urlInput, current.tagInput)) {
                is BookmarkResult.Success -> {
                    _state.update { it.copy(titleInput = "", urlInput = "", tagInput = "", errorMessage = null) }
                    refresh()
                }
                is BookmarkResult.Failure -> _state.update { it.copy(errorMessage = result.message) }
            }
        }
    }

    fun star(id: Int) = act { toggleStar(id) }

    fun remove(id: Int) = act { removeBookmark(id) }

    private fun act(action: suspend () -> BookmarkResult<*>) {
        viewModelScope.launch {
            when (val result = action()) {
                is BookmarkResult.Success -> refresh()
                is BookmarkResult.Failure -> _state.update { it.copy(errorMessage = result.message) }
            }
        }
    }
}
