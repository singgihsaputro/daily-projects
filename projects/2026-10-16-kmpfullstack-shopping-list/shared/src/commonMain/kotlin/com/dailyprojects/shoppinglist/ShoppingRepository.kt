package com.dailyprojects.shoppinglist

/** The one contract every target codes against — an in-memory store on the server, HTTP on every client. */
interface ShoppingRepository {
    suspend fun list(): List<ShoppingItem>
    suspend fun add(name: String, quantity: Int, category: String): ShoppingItem
    suspend fun toggleChecked(id: Int): ShoppingItem?
    suspend fun remove(id: Int): Boolean
    suspend fun clearChecked(): Int
}
