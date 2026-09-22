package com.dailyprojects.shortlink

/** The one contract every target codes against — an in-memory store on the server, HTTP on every client. */
interface LinkRepository {
    suspend fun list(): List<ShortLink>
    suspend fun create(longUrl: String): ShortLink
    suspend fun visit(code: String): ShortLink?
}
