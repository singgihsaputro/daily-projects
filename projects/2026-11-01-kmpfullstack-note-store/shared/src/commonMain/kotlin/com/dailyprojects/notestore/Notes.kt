package com.dailyprojects.notestore

const val MAX_TITLE_LENGTH = 60
const val MAX_BODY_LENGTH = 500
const val PREVIEW_LENGTH = 70

/** Returns an error message, or null when the title and body are acceptable. */
fun validateNote(title: String, body: String): String? = when {
    title.isBlank() -> "Give the note a title"
    title.trim().length > MAX_TITLE_LENGTH -> "Title must be $MAX_TITLE_LENGTH characters or fewer"
    body.trim().length > MAX_BODY_LENGTH -> "Body must be $MAX_BODY_LENGTH characters or fewer"
    else -> null
}

/** First line-ish of the body, collapsed to single spaces and cut with an ellipsis. */
fun previewOf(body: String): String {
    val flat = body.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.joinToString(" ")
    return if (flat.length <= PREVIEW_LENGTH) flat else flat.take(PREVIEW_LENGTH - 1).trimEnd() + "…"
}

/** Case-insensitive match on title or body; a blank query matches everything. */
fun matches(note: Note, query: String): Boolean {
    val needle = query.trim().lowercase()
    return needle.isEmpty() || needle in note.title.lowercase() || needle in note.body.lowercase()
}

/** Pinned notes first, then alphabetical by title. */
fun arrange(notes: List<Note>, query: String = ""): List<NoteView> =
    notes.filter { matches(it, query) }
        .sortedWith(compareByDescending<Note> { it.pinned }.thenBy { it.title.lowercase() })
        .map { NoteView(it, previewOf(it.body)) }
