package com.dailyprojects.notestore

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

private class FakeRepository(var notes: List<Note> = emptyList()) : NoteRepository {
    override suspend fun list() = notes
    override suspend fun add(title: String, body: String) =
        Note((notes.maxOfOrNull { it.id } ?: 0) + 1, title, body).also { notes = notes + it }
    override suspend fun togglePin(id: Int): Note? {
        val found = notes.firstOrNull { it.id == id } ?: return null
        return found.copy(pinned = !found.pinned).also { updated -> notes = notes.map { if (it.id == id) updated else it } }
    }
    override suspend fun remove(id: Int): Boolean {
        val before = notes.size
        notes = notes.filterNot { it.id == id }
        return notes.size < before
    }
}

class NoteUseCasesTest {
    private val notes = listOf(
        Note(1, "banana bread", "ripe bananas, flour"),
        Note(2, "Apple pie", "granny smith", pinned = true),
        Note(3, "Zebra facts", "stripes. Also eats APPLE sometimes"),
    )

    @Test
    fun pinnedFirstThenAlphabetical() = runTest {
        val titles = ListNotesUseCase(FakeRepository(notes))().map { it.note.title }
        assertEquals(listOf("Apple pie", "banana bread", "Zebra facts"), titles)
    }

    @Test
    fun searchIsCaseInsensitiveOverTitleAndBody() = runTest {
        val titles = ListNotesUseCase(FakeRepository(notes))("apple").map { it.note.id }
        assertEquals(listOf(2, 3), titles)
        assertEquals(emptyList(), ListNotesUseCase(FakeRepository(notes))("kiwi"))
    }

    @Test
    fun addValidatesAndTrims() = runTest {
        val repo = FakeRepository()
        val add = AddNoteUseCase(repo)
        assertIs<NoteResult.Failure>(add("   "))
        assertIs<NoteResult.Failure>(add("x".repeat(MAX_TITLE_LENGTH + 1)))
        assertIs<NoteResult.Failure>(add("ok", "y".repeat(MAX_BODY_LENGTH + 1)))
        val ok = add("  Tea  ", " oolong ")
        assertEquals(Note(1, "Tea", "oolong"), (ok as NoteResult.Success).value)
    }

    @Test
    fun pinTogglesAndReportsUnknownIds() = runTest {
        val repo = FakeRepository(notes)
        val pin = TogglePinUseCase(repo)
        assertEquals(true, (pin(1) as NoteResult.Success).value.pinned)
        assertEquals(false, (pin(1) as NoteResult.Success).value.pinned)
        assertEquals("No note with id 9", (pin(9) as NoteResult.Failure).message)
    }

    @Test
    fun removeOnlyOnce() = runTest {
        val remove = RemoveNoteUseCase(FakeRepository(notes))
        assertIs<NoteResult.Success<Unit>>(remove(1))
        assertIs<NoteResult.Failure>(remove(1))
    }

    @Test
    fun previewCollapsesWhitespaceAndTruncates() {
        assertEquals("a b c", previewOf("  a\n b\t c "))
        val long = previewOf("word ".repeat(40))
        assertTrue(long.length == PREVIEW_LENGTH && long.endsWith("…"))
    }
}
