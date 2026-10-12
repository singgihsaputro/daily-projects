import Testing
import Foundation
@testable import ChecklistCore

@Suite struct ChecklistTests {
    private func sample() -> Checklist {
        Checklist(id: "t", title: "T", items: [
            ChecklistItem(title: "A"), ChecklistItem(title: "B"),
            ChecklistItem(title: "C"), ChecklistItem(title: "D")
        ])
    }

    @Test func progressTracksToggles() {
        var list = sample()
        #expect(list.progress == 0)
        list.toggle(list.items[0].id)
        list.toggle(list.items[1].id)
        #expect(list.doneCount == 2)
        #expect(list.progress == 0.5)
        list.toggle(list.items[1].id)
        #expect(list.doneCount == 1)
    }

    @Test func completeOnlyWhenEverythingDone() {
        var list = sample()
        for item in list.items { list.toggle(item.id) }
        #expect(list.isComplete)
        #expect(!Checklist(id: "e", title: "E", items: []).isComplete)
    }

    @Test func addRejectsBlankAndDuplicates() {
        var list = sample()
        let blank = list.add("   ")
        let duplicate = list.add("a")
        let fresh = list.add("  E  ")
        #expect(!blank)
        #expect(!duplicate)
        #expect(fresh)
        #expect(list.items.last?.title == "E")
        #expect(list.items.count == 5)
    }

    @Test func removeAndReset() {
        var list = sample()
        list.toggle(list.items[0].id)
        list.remove(at: IndexSet([0, 2]))
        #expect(list.items.map(\.title) == ["B", "D"])
        list.toggle(list.items[0].id)
        list.resetAll()
        #expect(list.doneCount == 0)
    }

    @Test func mockRepositoryLoadsFixture() throws {
        let lists = try MockChecklistRepository().loadChecklists()
        #expect(lists.count == 3)
        #expect(lists[0].id == "weekend-trip")
        #expect(lists.allSatisfy { !$0.items.isEmpty && $0.doneCount == 0 })
    }

    @Test func invalidJSONThrows() {
        let repo = MockChecklistRepository(data: Data("{}".utf8))
        #expect(throws: ChecklistRepositoryError.invalidData) { try repo.loadChecklists() }
    }
}
