import Foundation

/// The client-side contract — same shape as the `ShoppingRepository` interface in
/// `shared/src/commonMain`, reimplemented in Swift since Kotlin/Native can't cross-compile
/// an iOS `.xcframework` on the Linux runner that authored this (see the project README).
protocol ShoppingRepository {
    func list() async throws -> [ShoppingItem]
    func add(name: String, quantity: Int, category: String) async throws -> ShoppingItem
    func toggleChecked(id: Int) async throws -> ShoppingItem?
    func remove(id: Int) async throws -> Bool
    func clearChecked() async throws -> Int
}

enum ShoppingRepositoryError: LocalizedError {
    case rejected(String)
    case server(String)

    var errorDescription: String? {
        switch self {
        case .rejected(let message), .server(let message):
            return message
        }
    }
}
