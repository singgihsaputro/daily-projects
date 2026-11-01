package com.dailyprojects.notestore.web

import com.dailyprojects.notestore.AddNoteUseCase
import com.dailyprojects.notestore.ListNotesUseCase
import com.dailyprojects.notestore.NoteResult
import com.dailyprojects.notestore.NoteView
import com.dailyprojects.notestore.RemoveNoteUseCase
import com.dailyprojects.notestore.TogglePinUseCase
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLFormElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.events.Event

private const val SERVER = "http://localhost:8080"

private val repository = HttpNoteRepository()
private val listNotes = ListNotesUseCase(repository)
private val addNote = AddNoteUseCase(repository)
private val togglePin = TogglePinUseCase(repository)
private val removeNote = RemoveNoteUseCase(repository)
private val scope = MainScope()

private fun element(id: String) = document.getElementById(id) as HTMLElement
private fun input(id: String) = document.getElementById(id) as HTMLInputElement

fun main() {
    (document.getElementById("create-form") as HTMLFormElement).addEventListener("submit", { event: Event ->
        event.preventDefault()
        val body = document.getElementById("body-input") as HTMLTextAreaElement
        scope.launch {
            when (val result = addNote(input("title-input").value, body.value)) {
                is NoteResult.Success -> {
                    input("title-input").value = ""
                    body.value = ""
                    render()
                }
                is NoteResult.Failure -> element("error").textContent = result.message
            }
        }
    })
    input("search-input").addEventListener("input", { scope.launch { render() } })
    scope.launch { render() }
}

private suspend fun render() {
    val notes = try {
        listNotes(input("search-input").value)
    } catch (t: Throwable) {
        element("error").textContent = "Could not reach the server at $SERVER — is ./gradlew :server:run running?"
        return
    }
    element("error").textContent = ""
    val list = element("note-list")
    while (list.firstChild != null) list.removeChild(list.firstChild!!)
    if (notes.isEmpty()) list.appendChild(node("li", "empty", "No notes match."))
    notes.forEach { list.appendChild(noteCard(it)) }
}

private fun noteCard(view: NoteView): HTMLElement {
    val note = view.note
    val card = node("li", if (note.pinned) "card pinned" else "card")
    card.appendChild(node("div", "title", (if (note.pinned) "📌 " else "") + note.title))
    card.appendChild(node("div", "preview", view.preview))

    val actions = node("div", "actions")
    val pin = node("button", "link", if (note.pinned) "Unpin" else "Pin")
    pin.addEventListener("click", { scope.launch { report(togglePin(note.id)) } })
    val delete = node("button", "link danger", "Delete")
    delete.addEventListener("click", { scope.launch { report(removeNote(note.id)) } })
    actions.appendChild(pin)
    actions.appendChild(delete)
    card.appendChild(actions)
    return card
}

private suspend fun report(result: NoteResult<*>) {
    when (result) {
        is NoteResult.Success -> render()
        is NoteResult.Failure -> element("error").textContent = result.message
    }
}

private fun node(tag: String, className: String, text: String = ""): HTMLElement {
    val node = document.createElement(tag) as HTMLElement
    node.className = className
    node.textContent = text
    return node
}
