package com.dailyprojects.bookmarks

sealed class BookmarkResult<out T> {
    data class Success<T>(val value: T) : BookmarkResult<T>()
    data class Failure(val message: String) : BookmarkResult<Nothing>()
}

/** Starred bookmarks first, then newest first. A blank [tag] means "all". */
class ListBookmarksUseCase(private val repository: BookmarkRepository) {
    suspend operator fun invoke(tag: String = ""): List<Bookmark> {
        val wanted = normalizeTag(tag)
        return repository.list()
            .filter { wanted.isEmpty() || it.tag == wanted }
            .sortedWith(compareByDescending<Bookmark> { it.starred }.thenByDescending { it.createdAtEpochSeconds })
    }
}

class AddBookmarkUseCase(private val repository: BookmarkRepository) {
    suspend operator fun invoke(title: String, url: String, tag: String = ""): BookmarkResult<Bookmark> {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) return BookmarkResult.Failure("Give the bookmark a title")
        if (cleanTitle.length > MAX_TITLE_LENGTH) {
            return BookmarkResult.Failure("Title must be $MAX_TITLE_LENGTH characters or fewer")
        }
        if (!isValidHttpUrl(url)) return BookmarkResult.Failure("Enter a full http:// or https:// URL")
        return BookmarkResult.Success(repository.add(cleanTitle, url.trim(), normalizeTag(tag)))
    }
}

class ToggleStarUseCase(private val repository: BookmarkRepository) {
    suspend operator fun invoke(id: Int): BookmarkResult<Bookmark> {
        val updated = repository.toggleStar(id) ?: return BookmarkResult.Failure("No bookmark with id $id")
        return BookmarkResult.Success(updated)
    }
}

class RemoveBookmarkUseCase(private val repository: BookmarkRepository) {
    suspend operator fun invoke(id: Int): BookmarkResult<Unit> =
        if (repository.remove(id)) BookmarkResult.Success(Unit) else BookmarkResult.Failure("No bookmark with id $id")
}
