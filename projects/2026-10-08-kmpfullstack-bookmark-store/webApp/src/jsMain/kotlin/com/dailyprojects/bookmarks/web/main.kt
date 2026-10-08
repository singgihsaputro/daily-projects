package com.dailyprojects.bookmarks.web

import com.dailyprojects.bookmarks.AddBookmarkUseCase
import com.dailyprojects.bookmarks.Bookmark
import com.dailyprojects.bookmarks.BookmarkResult
import com.dailyprojects.bookmarks.ListBookmarksUseCase
import com.dailyprojects.bookmarks.RemoveBookmarkUseCase
import com.dailyprojects.bookmarks.ToggleStarUseCase
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLFormElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event

private val repository = HttpBookmarkRepository()
private val listBookmarks = ListBookmarksUseCase(repository)
private val addBookmark = AddBookmarkUseCase(repository)
private val toggleStar = ToggleStarUseCase(repository)
private val removeBookmark = RemoveBookmarkUseCase(repository)
private val scope = MainScope()

private var selectedTag = ""

private fun element(id: String) = document.getElementById(id) as HTMLElement
private fun input(id: String) = document.getElementById(id) as HTMLInputElement

fun main() {
    (document.getElementById("create-form") as HTMLFormElement).addEventListener("submit", { event: Event ->
        event.preventDefault()
        scope.launch {
            when (val result = addBookmark(input("title-input").value, input("url-input").value, input("tag-input").value)) {
                is BookmarkResult.Success -> {
                    listOf("title-input", "url-input", "tag-input").forEach { input(it).value = "" }
                    render()
                }
                is BookmarkResult.Failure -> element("error").textContent = result.message
            }
        }
    })
    scope.launch { render() }
}

private suspend fun render() {
    val visible: List<Bookmark>
    val tags: List<String>
    try {
        visible = listBookmarks(selectedTag)
        tags = listBookmarks().map { it.tag }.filter { it.isNotEmpty() }.distinct().sorted()
    } catch (t: Throwable) {
        element("error").textContent = "Could not reach the server at http://localhost:8080 — is ./gradlew :server:run running?"
        return
    }
    element("error").textContent = ""
    fill(element("tag-filter"), (listOf("") + tags).map { tag -> tagChip(tag) })
    fill(element("bookmark-list"), visible.map { bookmarkRow(it) })
}

private fun fill(parent: HTMLElement, children: List<HTMLElement>) {
    while (parent.firstChild != null) parent.removeChild(parent.firstChild!!)
    children.forEach { parent.appendChild(it) }
}

private fun tagChip(tag: String): HTMLElement {
    val chip = button(if (tag.isEmpty()) "All" else "#$tag", if (tag == selectedTag) "chip active" else "chip") {
        selectedTag = tag
        render()
    }
    return chip
}

private fun bookmarkRow(bookmark: Bookmark): HTMLElement {
    val li = document.createElement("li") as HTMLElement
    li.appendChild(div("title", bookmark.title))
    li.appendChild(div("url", bookmark.url))
    li.appendChild(div("tag", if (bookmark.tag.isEmpty()) "untagged" else "#${bookmark.tag}"))
    val actions = div("actions", "")
    actions.appendChild(button(if (bookmark.starred) "★ Starred" else "☆ Star", "star-btn") {
        report(toggleStar(bookmark.id))
    })
    actions.appendChild(button("Delete", "delete-btn") { report(removeBookmark(bookmark.id)) })
    li.appendChild(actions)
    return li
}

private suspend fun report(result: BookmarkResult<*>) {
    when (result) {
        is BookmarkResult.Success -> render()
        is BookmarkResult.Failure -> element("error").textContent = result.message
    }
}

private fun div(className: String, text: String): HTMLElement {
    val node = document.createElement("div") as HTMLElement
    node.className = className
    node.textContent = text
    return node
}

private fun button(label: String, className: String, onClick: suspend () -> Unit): HTMLElement {
    val node = document.createElement("button") as HTMLElement
    node.className = className
    node.textContent = label
    node.addEventListener("click", { scope.launch { onClick() } })
    return node
}
