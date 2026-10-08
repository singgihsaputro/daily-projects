import Foundation

/// Mirrors `com.dailyprojects.bookmarks.Bookmark` from the KMP `shared` module —
/// the same JSON shape the server sends to every client.
struct Bookmark: Codable, Identifiable, Equatable {
    let id: Int
    let title: String
    let url: String
    var tag: String = ""
    var starred: Bool = false
    let createdAtEpochSeconds: Int64
}

struct CreateBookmarkRequest: Codable {
    let title: String
    let url: String
    let tag: String
}

struct ApiError: Codable {
    let message: String
}

let maxTitleLength = 80

/// Mirrors `isValidHttpUrl` from `shared/src/commonMain`.
func isValidHttpUrl(_ value: String) -> Bool {
    let trimmed = value.trimmingCharacters(in: .whitespacesAndNewlines)
    guard trimmed.hasPrefix("http://") || trimmed.hasPrefix("https://") else { return false }
    let afterScheme = trimmed.components(separatedBy: "://").last ?? ""
    let host = afterScheme.components(separatedBy: "/").first?.components(separatedBy: "?").first ?? ""
    return !host.isEmpty && host.contains(".") && !host.contains(" ")
}

/// Mirrors `normalizeTag` from `shared/src/commonMain`.
func normalizeTag(_ value: String) -> String {
    value.trimmingCharacters(in: .whitespacesAndNewlines)
        .lowercased()
        .split(whereSeparator: \.isWhitespace)
        .joined(separator: "-")
}
