package com.dailyprojects.notestore.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailyprojects.notestore.AddNoteUseCase
import com.dailyprojects.notestore.ListNotesUseCase
import com.dailyprojects.notestore.NoteRepository
import com.dailyprojects.notestore.NoteResult
import com.dailyprojects.notestore.NoteView
import com.dailyprojects.notestore.RemoveNoteUseCase
import com.dailyprojects.notestore.TogglePinUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NoteUiState(
    val notes: List<NoteView> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val query: String = "",
    val titleInput: String = "",
    val bodyInput: String = "",
)

class NoteViewModel(repository: NoteRepository = HttpNoteRepository()) : ViewModel() {
    private val listNotes = ListNotesUseCase(repository)
    private val addNote = AddNoteUseCase(repository)
    private val togglePin = TogglePinUseCase(repository)
    private val removeNote = RemoveNoteUseCase(repository)

    private val _state = MutableStateFlow(NoteUiState())
    val state: StateFlow<NoteUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun onQueryChange(value: String) {
        _state.update { it.copy(query = value) }
        refresh()
    }

    fun onTitleChange(value: String) = _state.update { it.copy(titleInput = value) }
    fun onBodyChange(value: String) = _state.update { it.copy(bodyInput = value) }

    fun refresh() {
        viewModelScope.launch {
            runCatching { listNotes(_state.value.query) }
                .onSuccess { notes -> _state.update { it.copy(notes = notes, isLoading = false, errorMessage = null) } }
                .onFailure { error -> _state.update { it.copy(isLoading = false, errorMessage = error.message ?: "Could not reach the server") } }
        }
    }

    fun add() {
        val current = _state.value
        viewModelScope.launch {
            val result = runCatching { addNote(current.titleInput, current.bodyInput) }
                .getOrElse { NoteResult.Failure(it.message ?: "Request failed") }
            when (result) {
                is NoteResult.Success -> {
                    _state.update { it.copy(titleInput = "", bodyInput = "", errorMessage = null) }
                    refresh()
                }
                is NoteResult.Failure -> _state.update { it.copy(errorMessage = result.message) }
            }
        }
    }

    fun pin(id: Int) = act { togglePin(id) }

    fun remove(id: Int) = act { removeNote(id) }

    private fun act(action: suspend () -> NoteResult<*>) {
        viewModelScope.launch {
            when (val result = runCatching { action() }.getOrElse { NoteResult.Failure(it.message ?: "Request failed") }) {
                is NoteResult.Success -> refresh()
                is NoteResult.Failure -> _state.update { it.copy(errorMessage = result.message) }
            }
        }
    }
}
