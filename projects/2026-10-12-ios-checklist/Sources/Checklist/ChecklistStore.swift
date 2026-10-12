import Foundation
import Observation
import ChecklistCore

@MainActor
@Observable
public final class ChecklistStore {
    public private(set) var checklists: [Checklist] = []
    public private(set) var errorMessage: String?

    private let repository: ChecklistRepository

    public init(repository: ChecklistRepository = MockChecklistRepository()) {
        self.repository = repository
        load()
    }

    private func load() {
        do {
            checklists = try repository.loadChecklists()
            errorMessage = nil
        } catch {
            errorMessage = "Could not load your checklists."
        }
    }

    public func update(_ id: String, _ change: (inout Checklist) -> Void) {
        guard let index = checklists.firstIndex(where: { $0.id == id }) else { return }
        change(&checklists[index])
    }
}
