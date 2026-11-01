package com.dailyprojects.notestore

import kotlinx.serialization.Serializable

@Serializable
data class Note(
    val id: Int,
    val title: String,
    val body: String = "",
    val pinned: Boolean = false,
)

@Serializable
data class CreateNoteRequest(val title: String, val body: String = "")

@Serializable
data class ApiError(val message: String)

/** What a screen draws for one note: the note plus a one-line preview of its body. */
data class NoteView(val note: Note, val preview: String)
