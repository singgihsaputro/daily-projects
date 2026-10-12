import Foundation

public struct ChecklistItem: Identifiable, Equatable, Sendable {
    public let id: UUID
    public var title: String
    public var isDone: Bool

    public init(id: UUID = UUID(), title: String, isDone: Bool = false) {
        self.id = id
        self.title = title
        self.isDone = isDone
    }
}

public struct Checklist: Identifiable, Equatable, Sendable {
    public let id: String
    public var title: String
    public var items: [ChecklistItem]

    public init(id: String, title: String, items: [ChecklistItem]) {
        self.id = id
        self.title = title
        self.items = items
    }

    public var doneCount: Int { items.filter(\.isDone).count }

    /// Fraction complete in 0...1; an empty list counts as 0.
    public var progress: Double {
        items.isEmpty ? 0 : Double(doneCount) / Double(items.count)
    }

    public var isComplete: Bool { !items.isEmpty && doneCount == items.count }

    /// Adds an item from user input. Returns false when the text is blank
    /// or duplicates an existing item (ignoring case).
    @discardableResult
    public mutating func add(_ text: String) -> Bool {
        let title = text.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !title.isEmpty,
              !items.contains(where: { $0.title.caseInsensitiveCompare(title) == .orderedSame })
        else { return false }
        items.append(ChecklistItem(title: title))
        return true
    }

    public mutating func toggle(_ itemID: UUID) {
        guard let index = items.firstIndex(where: { $0.id == itemID }) else { return }
        items[index].isDone.toggle()
    }

    public mutating func remove(at offsets: IndexSet) {
        items = items.enumerated().filter { !offsets.contains($0.offset) }.map(\.element)
    }

    public mutating func resetAll() {
        for index in items.indices { items[index].isDone = false }
    }
}
