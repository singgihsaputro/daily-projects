import Foundation
import Observation

@Observable
final class BookmarkModel {
    private(set) var bookmarks: [Bookmark] = []
    private(set) var tags: [String] = []
    private(set) var isLoading = false
    private(set) var errorMessage: String?
    var selectedTag = ""
    var titleInput = ""
    var urlInput = ""
    var tagInput = ""

    private let repository: BookmarkRepository

    init(repository: BookmarkRepository = HttpBookmarkRepository()) {
        self.repository = repository
    }

    /// Same ordering as `ListBookmarksUseCase`: starred first, then newest first; blank tag means all.
    func refresh() async {
        isLoading = true
        do {
            let all = try await repository.list()
            let wanted = normalizeTag(selectedTag)
            bookmarks = all
                .filter { wanted.isEmpty || $0.tag == wanted }
                .sorted { ($0.starred ? 1 : 0, $0.createdAtEpochSeconds) > ($1.starred ? 1 : 0, $1.createdAtEpochSeconds) }
            tags = Array(Set(all.map(\.tag).filter { !$0.isEmpty })).sorted()
            errorMessage = nil
        } catch {
            errorMessage = "Could not reach the server at http://localhost:8080 — is `./gradlew :server:run` running?"
        }
        isLoading = false
    }

    func select(tag: String) async {
        selectedTag = tag
        await refresh()
    }

    func add() async {
        let title = titleInput.trimmingCharacters(in: .whitespacesAndNewlines)
        if title.isEmpty {
            errorMessage = "Give the bookmark a title"
        } else if title.count > maxTitleLength {
            errorMessage = "Title must be \(maxTitleLength) characters or fewer"
        } else if !isValidHttpUrl(urlInput) {
            errorMessage = "Enter a full http:// or https:// URL"
        } else {
            do {
                _ = try await repository.add(
                    title: title,
                    url: urlInput.trimmingCharacters(in: .whitespacesAndNewlines),
                    tag: normalizeTag(tagInput)
                )
                titleInput = ""
                urlInput = ""
                tagInput = ""
                await refresh()
            } catch {
                errorMessage = error.localizedDescription
            }
        }
    }

    func toggleStar(id: Int) async {
        do {
            if try await repository.toggleStar(id: id) == nil {
                errorMessage = "No bookmark with id \(id)"
            } else {
                await refresh()
            }
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func remove(id: Int) async {
        do {
            if try await repository.remove(id: id) {
                await refresh()
            } else {
                errorMessage = "No bookmark with id \(id)"
            }
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
