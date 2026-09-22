package com.dailyprojects.shortlink.web

import com.dailyprojects.shortlink.CreateShortLinkUseCase
import com.dailyprojects.shortlink.LinkResult
import com.dailyprojects.shortlink.ListLinksUseCase
import com.dailyprojects.shortlink.ShortLink
import com.dailyprojects.shortlink.VisitLinkUseCase
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLFormElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLUListElement
import org.w3c.dom.events.Event

private val repository = HttpLinkRepository()
private val listLinks = ListLinksUseCase(repository)
private val createLink = CreateShortLinkUseCase(repository)
private val visitLink = VisitLinkUseCase(repository)
private val scope = MainScope()

fun main() {
    val form = document.getElementById("create-form") as HTMLFormElement
    val input = document.getElementById("url-input") as HTMLInputElement
    val errorBox = document.getElementById("error") as HTMLElement
    val list = document.getElementById("link-list") as HTMLUListElement

    form.addEventListener("submit", { event: Event ->
        event.preventDefault()
        scope.launch {
            when (val result = createLink(input.value)) {
                is LinkResult.Success -> {
                    input.value = ""
                    errorBox.textContent = ""
                    renderLinks(list, errorBox)
                }
                is LinkResult.Failure -> errorBox.textContent = result.message
            }
        }
    })

    scope.launch { renderLinks(list, errorBox) }
}

private suspend fun renderLinks(list: HTMLUListElement, errorBox: HTMLElement) {
    val links = try {
        listLinks()
    } catch (t: Throwable) {
        errorBox.textContent = "Could not reach the server at http://localhost:8080 — is ./gradlew :server:run running?"
        return
    }
    while (list.firstChild != null) list.removeChild(list.firstChild!!)
    links.forEach { link -> list.appendChild(buildLinkRow(link, list, errorBox)) }
}

private fun buildLinkRow(link: ShortLink, list: HTMLUListElement, errorBox: HTMLElement): HTMLElement {
    val li = document.createElement("li") as HTMLElement

    val codeDiv = document.createElement("div") as HTMLElement
    codeDiv.className = "code"
    codeDiv.textContent = "/r/${link.code}"

    val urlDiv = document.createElement("div") as HTMLElement
    urlDiv.className = "url"
    urlDiv.textContent = link.longUrl

    val clicksDiv = document.createElement("div") as HTMLElement
    clicksDiv.className = "clicks"
    clicksDiv.textContent = "${link.clicks} clicks"

    val visitButton = document.createElement("button") as HTMLButtonElement
    visitButton.className = "visit-btn"
    visitButton.textContent = "Visit"
    visitButton.addEventListener("click", {
        scope.launch {
            when (val result = visitLink(link.code)) {
                is LinkResult.Success -> renderLinks(list, errorBox)
                is LinkResult.Failure -> errorBox.textContent = result.message
            }
        }
    })

    li.appendChild(codeDiv)
    li.appendChild(urlDiv)
    li.appendChild(clicksDiv)
    li.appendChild(visitButton)
    return li
}
