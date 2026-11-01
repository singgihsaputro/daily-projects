package com.dailyprojects.notestore.server

import com.dailyprojects.notestore.AddNoteUseCase
import com.dailyprojects.notestore.ApiError
import com.dailyprojects.notestore.CreateNoteRequest
import com.dailyprojects.notestore.NoteRepository
import com.dailyprojects.notestore.NoteResult
import com.dailyprojects.notestore.RemoveNoteUseCase
import com.dailyprojects.notestore.TogglePinUseCase
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.util.pipeline.PipelineContext
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") { noteModule() }.start(wait = true)
}

fun Application.noteModule(repository: NoteRepository = InMemoryNoteRepository.seededFromResource()) {
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
            call.respond(HttpStatusCode.BadRequest, ApiError("Request body must be valid JSON for this endpoint"))
        }
    }

    val addNote = AddNoteUseCase(repository)
    val togglePin = TogglePinUseCase(repository)
    val removeNote = RemoveNoteUseCase(repository)

    routing {
        get("/health") { call.respond(mapOf("status" to "ok")) }

        get("/notes") { call.respond(repository.list()) }

        post("/notes") {
            val request = call.receive<CreateNoteRequest>()
            when (val result = addNote(request.title, request.body)) {
                is NoteResult.Success -> call.respond(HttpStatusCode.Created, result.value)
                is NoteResult.Failure -> call.respond(HttpStatusCode.BadRequest, ApiError(result.message))
            }
        }

        post("/notes/{id}/pin") {
            val id = pathId() ?: return@post
            when (val result = togglePin(id)) {
                is NoteResult.Success -> call.respond(result.value)
                is NoteResult.Failure -> call.respond(HttpStatusCode.NotFound, ApiError(result.message))
            }
        }

        delete("/notes/{id}") {
            val id = pathId() ?: return@delete
            when (val result = removeNote(id)) {
                is NoteResult.Success -> call.respond(HttpStatusCode.NoContent)
                is NoteResult.Failure -> call.respond(HttpStatusCode.NotFound, ApiError(result.message))
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
