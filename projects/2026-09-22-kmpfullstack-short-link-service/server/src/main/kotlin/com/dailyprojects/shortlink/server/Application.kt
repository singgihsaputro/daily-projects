package com.dailyprojects.shortlink.server

import com.dailyprojects.shortlink.ApiError
import com.dailyprojects.shortlink.CreateLinkRequest
import com.dailyprojects.shortlink.CreateShortLinkUseCase
import com.dailyprojects.shortlink.LinkRepository
import com.dailyprojects.shortlink.LinkResult
import com.dailyprojects.shortlink.ListLinksUseCase
import com.dailyprojects.shortlink.VisitLinkUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::shortLinkModule).start(wait = true)
}

fun Application.shortLinkModule(repository: LinkRepository = InMemoryLinkRepository.seededFromResource()) {
    install(ContentNegotiation) {
        json(Json { prettyPrint = true; ignoreUnknownKeys = true })
    }
    install(CORS) {
        anyHost()
        allowHeader("Content-Type")
        allowMethod(io.ktor.http.HttpMethod.Post)
    }

    val listLinks = ListLinksUseCase(repository)
    val createLink = CreateShortLinkUseCase(repository)
    val visitLink = VisitLinkUseCase(repository)

    routing {
        get("/health") { call.respond(mapOf("status" to "ok")) }

        get("/links") { call.respond(listLinks()) }

        post("/links") {
            val request = call.receive<CreateLinkRequest>()
            when (val result = createLink(request.longUrl)) {
                is LinkResult.Success -> call.respond(HttpStatusCode.Created, result.value)
                is LinkResult.Failure -> call.respond(HttpStatusCode.BadRequest, ApiError(result.message))
            }
        }

        get("/r/{code}") {
            val code = call.parameters["code"] ?: return@get call.respond(HttpStatusCode.BadRequest, ApiError("missing code"))
            when (val result = visitLink(code)) {
                is LinkResult.Success -> {
                    call.response.header("Location", result.value.longUrl)
                    call.respond(HttpStatusCode.Found)
                }
                is LinkResult.Failure -> call.respond(HttpStatusCode.NotFound, ApiError(result.message))
            }
        }
    }
}
