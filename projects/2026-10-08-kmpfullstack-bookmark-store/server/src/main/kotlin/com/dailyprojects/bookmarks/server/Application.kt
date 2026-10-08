package com.dailyprojects.bookmarks.server

import com.dailyprojects.bookmarks.AddBookmarkUseCase
import com.dailyprojects.bookmarks.ApiError
import com.dailyprojects.bookmarks.BookmarkRepository
import com.dailyprojects.bookmarks.BookmarkResult
import com.dailyprojects.bookmarks.CreateBookmarkRequest
import com.dailyprojects.bookmarks.ListBookmarksUseCase
import com.dailyprojects.bookmarks.RemoveBookmarkUseCase
import com.dailyprojects.bookmarks.ToggleStarUseCase
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.util.pipeline.PipelineContext
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::bookmarkModule).start(wait = true)
}

fun Application.bookmarkModule(repository: BookmarkRepository = InMemoryBookmarkRepository.seededFromResource()) {
    install(ContentNegotiation) {
        json(Json { prettyPrint = true; ignoreUnknownKeys = true; encodeDefaults = true })
    }
    install(CORS) {
        anyHost()
        allowHeader("Content-Type")
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Delete)
    }
    install(StatusPages) {
        exception<BadRequestException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, ApiError("Request body must be JSON with title, url and optional tag"))
        }
    }

    val listBookmarks = ListBookmarksUseCase(repository)
    val addBookmark = AddBookmarkUseCase(repository)
    val toggleStar = ToggleStarUseCase(repository)
    val removeBookmark = RemoveBookmarkUseCase(repository)

    routing {
        get("/health") { call.respond(mapOf("status" to "ok")) }

        get("/bookmarks") { call.respond(listBookmarks(call.request.queryParameters["tag"].orEmpty())) }

        post("/bookmarks") {
            val request = call.receive<CreateBookmarkRequest>()
            when (val result = addBookmark(request.title, request.url, request.tag)) {
                is BookmarkResult.Success -> call.respond(HttpStatusCode.Created, result.value)
                is BookmarkResult.Failure -> call.respond(HttpStatusCode.BadRequest, ApiError(result.message))
            }
        }

        post("/bookmarks/{id}/star") {
            val id = pathId() ?: return@post
            when (val result = toggleStar(id)) {
                is BookmarkResult.Success -> call.respond(result.value)
                is BookmarkResult.Failure -> call.respond(HttpStatusCode.NotFound, ApiError(result.message))
            }
        }

        delete("/bookmarks/{id}") {
            val id = pathId() ?: return@delete
            when (val result = removeBookmark(id)) {
                is BookmarkResult.Success -> call.respond(HttpStatusCode.NoContent)
                is BookmarkResult.Failure -> call.respond(HttpStatusCode.NotFound, ApiError(result.message))
            }
        }
    }
}

/** Reads `{id}` as an Int, answering 400 itself (and returning null) when it isn't one. */
private suspend fun PipelineContext<Unit, ApplicationCall>.pathId(): Int? {
    val id = call.parameters["id"]?.toIntOrNull()
    if (id == null) call.respond(HttpStatusCode.BadRequest, ApiError("id must be a number"))
    return id
}
