package com.dailyprojects.shoppinglist.server

import com.dailyprojects.shoppinglist.ShoppingItem
import com.dailyprojects.shoppinglist.ShoppingRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import java.time.Instant

/** The server's source of truth: an in-memory list seeded from `mock/items.json`, guarded by a mutex. */
class InMemoryShoppingRepository(seed: List<ShoppingItem>) : ShoppingRepository {
    private val mutex = Mutex()
    private val items = seed.toMutableList()
    private var nextId = (seed.maxOfOrNull { it.id } ?: 0) + 1

    override suspend fun list(): List<ShoppingItem> = mutex.withLock { items.toList() }

    override suspend fun add(name: String, quantity: Int, category: String): ShoppingItem = mutex.withLock {
        val item = ShoppingItem(
            id = nextId++,
            name = name,
            quantity = quantity,
            category = category,
            createdAtEpochSeconds = Instant.now().epochSecond,
        )
        items += item
        item
    }

    override suspend fun toggleChecked(id: Int): ShoppingItem? = mutex.withLock {
        val index = items.indexOfFirst { it.id == id }
        if (index == -1) return@withLock null
        val updated = items[index].copy(checked = !items[index].checked)
        items[index] = updated
        updated
    }

    override suspend fun remove(id: Int): Boolean = mutex.withLock { items.removeAll { it.id == id } }

    override suspend fun clearChecked(): Int = mutex.withLock {
        val before = items.size
        items.removeAll { it.checked }
        before - items.size
    }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun seededFromResource(resourcePath: String = "/items.json"): InMemoryShoppingRepository {
            val text = InMemoryShoppingRepository::class.java.getResourceAsStream(resourcePath)
                ?.bufferedReader()?.readText()
                ?: error("missing seed resource $resourcePath")
            return InMemoryShoppingRepository(json.decodeFromString<List<ShoppingItem>>(text))
        }
    }
}
