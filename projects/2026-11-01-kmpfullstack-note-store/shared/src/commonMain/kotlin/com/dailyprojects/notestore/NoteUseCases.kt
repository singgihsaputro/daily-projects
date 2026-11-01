package com.dailyprojects.notestore

sealed class NoteResult<out T> {
    data class Success<T>(val value: T) : NoteResult<T>()
    data class Failure(val message: String) : NoteResult<Nothing>()
}

class ListNotesUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(query: String = ""): List<NoteView> = arrange(repository.list(), query)
}

class AddNoteUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(title: String, body: String = ""): NoteResult<Note> {
        validateNote(title, body)?.let { return NoteResult.Failure(it) }
        return NoteResult.Success(repository.add(title.trim(), body.trim()))
    }
}

class TogglePinUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(id: Int): NoteResult<Note> =
        repository.togglePin(id)?.let { NoteResult.Success(it) } ?: NoteResult.Failure("No note with id $id")
}

class RemoveNoteUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(id: Int): NoteResult<Unit> =
        if (repository.remove(id)) NoteResult.Success(Unit) else NoteResult.Failure("No note with id $id")
}
