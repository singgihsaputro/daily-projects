import Foundation
import Observation

@Observable
final class NoteModel {
    private(set) var notes: [NoteView] = []
    private(set) var isLoading = false
    private(set) var errorMessage: String?
    var query = ""
    var titleInput = ""
    var bodyInput = ""

    private let repository: NoteRepository

    init(repository: NoteRepository = HttpNoteRepository()) {
        self.repository = repository
    }

    /// Fetches everything and filters locally with the same rules as the shared `arrange`.
    func refresh() async {
        isLoading = true
        do {
            notes = arrange(try await repository.list(), query: query)
            errorMessage = nil
        } catch {
            errorMessage = "Could not reach the server at http://localhost:8080 — is `./gradlew :server:run` running?"
        }
        isLoading = false
    }

    func add() async {
        if let problem = validateNote(title: titleInput, body: bodyInput) {
            errorMessage = problem
            return
        }
        do {
            _ = try await repository.add(
                title: titleInput.trimmingCharacters(in: .whitespacesAndNewlines),
                body: bodyInput.trimmingCharacters(in: .whitespacesAndNewlines)
            )
            titleInput = ""
            bodyInput = ""
            await refresh()
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func togglePin(id: Int) async {
        do {
            if try await repository.togglePin(id: id) == nil {
                errorMessage = "No note with id \(id)"
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
                errorMessage = "No note with id \(id)"
            }
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
