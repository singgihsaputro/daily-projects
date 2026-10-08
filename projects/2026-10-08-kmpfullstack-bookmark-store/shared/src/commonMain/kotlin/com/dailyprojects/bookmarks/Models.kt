package com.dailyprojects.bookmarks

import kotlinx.serialization.Serializable

@Serializable
data class Bookmark(
    val id: Int,
    val title: String,
    val url: String,
    val tag: String = "",
    val starred: Boolean = false,
    val createdAtEpochSeconds: Long,
)

@Serializable
data class CreateBookmarkRequest(val title: String, val url: String, val tag: String = "")

@Serializable
data class ApiError(val message: String)
