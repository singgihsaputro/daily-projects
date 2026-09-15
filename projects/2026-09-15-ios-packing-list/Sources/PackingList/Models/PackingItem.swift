import Foundation

enum PackingCategory: String, Codable, CaseIterable, Identifiable {
    case clothing = "Clothing"
    case toiletries = "Toiletries"
    case electronics = "Electronics"
    case documents = "Documents"

    var id: String { rawValue }

    var symbol: String {
        switch self {
        case .clothing: return "tshirt"
        case .toiletries: return "drop"
        case .electronics: return "bolt"
        case .documents: return "doc.text"
        }
    }
}

struct PackingItem: Identifiable, Codable, Equatable {
    let id: String
    var name: String
    var category: PackingCategory
    var packed: Bool

    mutating func togglePacked() {
        packed.toggle()
    }
}
