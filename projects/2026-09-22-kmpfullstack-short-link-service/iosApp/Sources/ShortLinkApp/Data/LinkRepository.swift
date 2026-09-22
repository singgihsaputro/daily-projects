import Foundation

/// The client-side contract — same shape as the `LinkRepository` interface in
/// `shared/src/commonMain`, reimplemented in Swift since Kotlin/Native can't cross-compile
/// an iOS `.xcframework` on this Linux runner (see the project README for details).
protocol LinkRepository {
    func list() async throws -> [ShortLink]
    func create(longUrl: String) async throws -> ShortLink
    func visit(code: String) async throws -> ShortLink?
}

enum LinkRepositoryError: LocalizedError {
    case invalidUrl(String)
    case server(String)
    case notFound(String)

    var errorDescription: String? {
        switch self {
        case .invalidUrl(let message), .server(let message), .notFound(let message):
            return message
        }
    }
}
