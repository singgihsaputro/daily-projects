package com.dailyprojects.shoppinglist.server

import com.dailyprojects.shoppinglist.AddItemUseCase
import com.dailyprojects.shoppinglist.ApiError
import com.dailyprojects.shoppinglist.ClearCheckedUseCase
import com.dailyprojects.shoppinglist.ClearedResponse
import com.dailyprojects.shoppinglist.CreateItemRequest
import com.dailyprojects.shoppinglist.ListItemsUseCase
import com.dailyprojects.shoppinglist.RemoveItemUseCase
import com.dailyprojects.shoppinglist.ShopResult
import com.dailyprojects.shoppinglist.ShoppingRepository
import com.dailyprojects.shoppinglist.ToggleCheckedUseCase
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
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::shoppingModule).start(wait = true)
}

fun Application.shoppingModule(repository: ShoppingRepository = InMemoryShoppingRepository.seededFromResource()) {
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
            call.respond(HttpStatusCode.BadRequest, ApiError("Request body must be JSON with name, optional quantity and category"))
        }
    }

    val listItems = ListItemsUseCase(repository)
    val addItem = AddItemUseCase(repository)
    val toggleChecked = ToggleCheckedUseCase(repository)
    val removeItem = RemoveItemUseCase(repository)
    val clearChecked = ClearCheckedUseCase(repository)

    routing {
        get("/health") { call.respond(mapOf("status" to "ok")) }

        get("/items") { call.respond(listItems(call.request.queryParameters["category"].orEmpty())) }

        post("/items") {
            val request = call.receive<CreateItemRequest>()
            when (val result = addItem(request.name, request.quantity, request.category)) {
                is ShopResult.Success -> call.respond(HttpStatusCode.Created, result.value)
                is ShopResult.Failure -> call.respond(HttpStatusCode.BadRequest, ApiError(result.message))
            }
        }

        post("/items/{id}/check") {
            val id = pathId() ?: return@post
            when (val result = toggleChecked(id)) {
                is ShopResult.Success -> call.respond(result.value)
                is ShopResult.Failure -> call.respond(HttpStatusCode.NotFound, ApiError(result.message))
            }
        }

        delete("/items/checked") { call.respond(ClearedResponse(clearChecked())) }

        delete("/items/{id}") {
            val id = pathId() ?: return@delete
            when (val result = removeItem(id)) {
                is ShopResult.Success -> call.respond(HttpStatusCode.NoContent)
                is ShopResult.Failure -> call.respond(HttpStatusCode.NotFound, ApiError(result.message))
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
