package com.dailyprojects.shoppinglist

import kotlinx.serialization.Serializable

@Serializable
data class ShoppingItem(
    val id: Int,
    val name: String,
    val quantity: Int = 1,
    val category: String = "",
    val checked: Boolean = false,
    val createdAtEpochSeconds: Long,
)

@Serializable
data class CreateItemRequest(val name: String, val quantity: Int = 1, val category: String = "")

@Serializable
data class ClearedResponse(val removed: Int)

@Serializable
data class ApiError(val message: String)
