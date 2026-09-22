package com.dailyprojects.shortlink

import kotlinx.serialization.Serializable

@Serializable
data class ShortLink(
    val code: String,
    val longUrl: String,
    val createdAtEpochSeconds: Long,
    val clicks: Int = 0,
)

@Serializable
data class CreateLinkRequest(val longUrl: String)

@Serializable
data class ApiError(val message: String)
