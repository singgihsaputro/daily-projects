package com.dailyprojects.shortlink

sealed class LinkResult<out T> {
    data class Success<T>(val value: T) : LinkResult<T>()
    data class Failure(val message: String) : LinkResult<Nothing>()
}

class ListLinksUseCase(private val repository: LinkRepository) {
    suspend operator fun invoke(): List<ShortLink> =
        repository.list().sortedByDescending { it.createdAtEpochSeconds }
}

class CreateShortLinkUseCase(private val repository: LinkRepository) {
    suspend operator fun invoke(longUrl: String): LinkResult<ShortLink> {
        if (!isValidHttpUrl(longUrl)) {
            return LinkResult.Failure("Enter a full http:// or https:// URL")
        }
        return LinkResult.Success(repository.create(longUrl.trim()))
    }
}

class VisitLinkUseCase(private val repository: LinkRepository) {
    suspend operator fun invoke(code: String): LinkResult<ShortLink> {
        val updated = repository.visit(code) ?: return LinkResult.Failure("No link with code '$code'")
        return LinkResult.Success(updated)
    }
}
