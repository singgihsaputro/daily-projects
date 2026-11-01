import Foundation

/// The client-side contract — same shape as the `NoteRepository` interface in
/// `shared/src/commonMain`, reimplemented in Swift since Kotlin/Native can't cross-compile
/// an iOS `.xcframework` on the Linux runner that authored this (see the project README).
protocol NoteRepository {
    func list() async throws -> [Note]
    func add(title: String, body: String) async throws -> Note
    func togglePin(id: Int) async throws -> Note?
    func remove(id: Int) async throws -> Bool
}

enum NoteRepositoryError: LocalizedError {
    case rejected(String)
    case server(String)

    var errorDescription: String? {
        switch self {
        case .rejected(let message), .server(let message):
            return message
        }
    }
}
