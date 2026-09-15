import Testing
@testable import PackingList

struct PackingItemTests {
    @Test func togglePackedMarksItemPacked() {
        var item = PackingItem(id: "test", name: "Test Item", category: .clothing, packed: false)

        item.togglePacked()

        #expect(item.packed == true)
    }

    @Test func togglePackedTwiceReturnsToUnpacked() {
        var item = PackingItem(id: "test", name: "Test Item", category: .documents, packed: false)

        item.togglePacked()
        item.togglePacked()

        #expect(item.packed == false)
    }

    @Test func decodesFromFixtureJSON() throws {
        let json = """
        [{ "id": "passport", "name": "Passport", "category": "Documents", "packed": true }]
        """
        let items = try JSONDecoder().decode([PackingItem].self, from: Data(json.utf8))

        #expect(items.count == 1)
        #expect(items[0].category == .documents)
        #expect(items[0].packed == true)
    }
}
