import Foundation
import Observation

@Observable
final class ShortLinkModel {
    private(set) var links: [ShortLink] = []
    private(set) var isLoading = false
    private(set) var errorMessage: String?
    var newUrlInput = ""

    private let repository: LinkRepository

    init(repository: LinkRepository = HttpLinkRepository()) {
        self.repository = repository
    }

    func refresh() async {
        isLoading = true
        do {
            links = try await repository.list().sorted { $0.createdAtEpochSeconds > $1.createdAtEpochSeconds }
            errorMessage = nil
        } catch {
            errorMessage = "Could not reach the server at http://localhost:8080 — is `swift run` / the Ktor server running?"
        }
        isLoading = false
    }

    func addLink() async {
        guard isValidHttpUrl(newUrlInput) else {
            errorMessage = "Enter a full http:// or https:// URL"
            return
        }
        do {
            _ = try await repository.create(longUrl: newUrlInput.trimmingCharacters(in: .whitespacesAndNewlines))
            newUrlInput = ""
            errorMessage = nil
            await refresh()
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func visit(code: String) async {
        do {
            _ = try await repository.visit(code: code)
            await refresh()
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
