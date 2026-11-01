import Foundation

/// Mirrors `com.dailyprojects.notestore.Note` from the KMP `shared` module —
/// the same JSON shape the server sends to every client.
struct Note: Codable, Identifiable, Equatable {
    let id: Int
    let title: String
    var body: String = ""
    var pinned: Bool = false
}

struct CreateNoteRequest: Codable {
    let title: String
    let body: String
}

struct ApiError: Codable {
    let message: String
}

let maxTitleLength = 60
let maxBodyLength = 500
let previewLength = 70

/// What a row draws for one note. Mirrors `NoteView` in `shared`.
struct NoteView: Identifiable, Equatable {
    let note: Note
    let preview: String
    var id: Int { note.id }
}

/// Mirrors `validateNote`: an error message, or nil when acceptable.
func validateNote(title: String, body: String) -> String? {
    let trimmedTitle = title.trimmingCharacters(in: .whitespacesAndNewlines)
    if trimmedTitle.isEmpty { return "Give the note a title" }
    if trimmedTitle.count > maxTitleLength { return "Title must be \(maxTitleLength) characters or fewer" }
    if body.trimmingCharacters(in: .whitespacesAndNewlines).count > maxBodyLength {
        return "Body must be \(maxBodyLength) characters or fewer"
    }
    return nil
}

/// Mirrors `previewOf`: whitespace collapsed to single spaces, cut with an ellipsis.
func previewOf(_ body: String) -> String {
    let flat = body.split(whereSeparator: { $0.isWhitespace }).joined(separator: " ")
    guard flat.count > previewLength else { return flat }
    let cut = String(flat.prefix(previewLength - 1)).trimmingCharacters(in: .whitespaces)
    return cut + "…"
}

/// Mirrors `matches`: case-insensitive on title or body; blank matches everything.
func matches(_ note: Note, query: String) -> Bool {
    let needle = query.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
    return needle.isEmpty || note.title.lowercased().contains(needle) || note.body.lowercased().contains(needle)
}

/// Mirrors `arrange`: pinned first, then alphabetical by title.
func arrange(_ notes: [Note], query: String = "") -> [NoteView] {
    notes
        .filter { matches($0, query: query) }
        .sorted { lhs, rhs in
            if lhs.pinned != rhs.pinned { return lhs.pinned }
            return lhs.title.lowercased() < rhs.title.lowercased()
        }
        .map { NoteView(note: $0, preview: previewOf($0.body)) }
}
