import Foundation

/// The client-side contract — same shape as the `BookmarkRepository` interface in
/// `shared/src/commonMain`, reimplemented in Swift since Kotlin/Native can't cross-compile
/// an iOS `.xcframework` on the Linux runner that authored this (see the project README).
protocol BookmarkRepository {
    func list() async throws -> [Bookmark]
    func add(title: String, url: String, tag: String) async throws -> Bookmark
    func toggleStar(id: Int) async throws -> Bookmark?
    func remove(id: Int) async throws -> Bool
}

enum BookmarkRepositoryError: LocalizedError {
    case rejected(String)
    case server(String)

    var errorDescription: String? {
        switch self {
        case .rejected(let message), .server(let message):
            return message
        }
    }
}
