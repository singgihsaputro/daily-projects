import Foundation

/// Mirrors `com.dailyprojects.shortlink.ShortLink` from the KMP `shared` module —
/// the same JSON shape the server sends to every client.
struct ShortLink: Codable, Identifiable, Equatable {
    let code: String
    let longUrl: String
    let createdAtEpochSeconds: Int64
    var clicks: Int = 0

    var id: String { code }
}

struct CreateLinkRequest: Codable {
    let longUrl: String
}

struct ApiError: Codable {
    let message: String
}

/// Mirrors `isValidHttpUrl` from `shared/src/commonMain`.
func isValidHttpUrl(_ value: String) -> Bool {
    let trimmed = value.trimmingCharacters(in: .whitespacesAndNewlines)
    guard trimmed.hasPrefix("http://") || trimmed.hasPrefix("https://") else { return false }
    let afterScheme = trimmed.components(separatedBy: "://").last ?? ""
    let host = afterScheme.components(separatedBy: "/").first?.components(separatedBy: "?").first ?? ""
    return !host.isEmpty && host.contains(".") && !host.contains(" ")
}
