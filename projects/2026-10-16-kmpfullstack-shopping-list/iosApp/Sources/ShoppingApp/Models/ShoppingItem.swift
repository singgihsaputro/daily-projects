import Foundation

/// Mirrors `com.dailyprojects.shoppinglist.ShoppingItem` from the KMP `shared` module —
/// the same JSON shape the server sends to every client.
struct ShoppingItem: Codable, Identifiable, Equatable {
    let id: Int
    let name: String
    var quantity: Int = 1
    var category: String = ""
    var checked: Bool = false
    let createdAtEpochSeconds: Int64
}

struct CreateItemRequest: Codable {
    let name: String
    let quantity: Int
    let category: String
}

struct ClearedResponse: Codable {
    let removed: Int
}

struct ApiError: Codable {
    let message: String
}

let maxNameLength = 60
let maxQuantity = 99

/// Mirrors `normalizeCategory` from `shared/src/commonMain`.
func normalizeCategory(_ value: String) -> String {
    value.trimmingCharacters(in: .whitespacesAndNewlines)
        .lowercased()
        .split(whereSeparator: \.isWhitespace)
        .joined(separator: "-")
}

/// Mirrors the ordering in `ListItemsUseCase`: still-to-buy first, then newest first;
/// a blank category means all.
func visibleItems(_ all: [ShoppingItem], category: String) -> [ShoppingItem] {
    let wanted = normalizeCategory(category)
    return all
        .filter { wanted.isEmpty || $0.category == wanted }
        .sorted { lhs, rhs in
            if lhs.checked != rhs.checked { return !lhs.checked }
            return lhs.createdAtEpochSeconds > rhs.createdAtEpochSeconds
        }
}
