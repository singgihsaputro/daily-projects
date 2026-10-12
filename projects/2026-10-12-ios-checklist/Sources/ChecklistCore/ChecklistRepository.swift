import Foundation

public protocol ChecklistRepository: Sendable {
    func loadChecklists() throws -> [Checklist]
}

public enum ChecklistRepositoryError: Error, Equatable {
    case missingResource
    case invalidData
}

/// Reads starter checklists from the bundled `mock/checklists.json` fixture.
/// A real client (server or iCloud) replaces this one type.
public struct MockChecklistRepository: ChecklistRepository {
    private struct Payload: Decodable {
        struct Entry: Decodable {
            let id: String
            let title: String
            let items: [String]
        }
        let checklists: [Entry]
    }

    private let data: Data?

    public init() {
        data = Bundle.module.url(forResource: "checklists", withExtension: "json")
            .flatMap { try? Data(contentsOf: $0) }
    }

    public init(data: Data) {
        self.data = data
    }

    public func loadChecklists() throws -> [Checklist] {
        guard let data else { throw ChecklistRepositoryError.missingResource }
        guard let payload = try? JSONDecoder().decode(Payload.self, from: data) else {
            throw ChecklistRepositoryError.invalidData
        }
        return payload.checklists.map { entry in
            Checklist(id: entry.id, title: entry.title,
                      items: entry.items.map { ChecklistItem(title: $0) })
        }
    }
}
