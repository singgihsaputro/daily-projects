import XCTest
@testable import ShoppingApp

private final class FakeRepository: ShoppingRepository {
    var items: [ShoppingItem]

    init(items: [ShoppingItem] = []) {
        self.items = items
    }

    func list() async throws -> [ShoppingItem] { items }

    func add(name: String, quantity: Int, category: String) async throws -> ShoppingItem {
        let item = ShoppingItem(
            id: items.count + 1,
            name: name,
            quantity: quantity,
            category: category,
            createdAtEpochSeconds: Int64(items.count)
        )
        items.append(item)
        return item
    }

    func toggleChecked(id: Int) async throws -> ShoppingItem? {
        guard let index = items.firstIndex(where: { $0.id == id }) else { return nil }
        items[index].checked.toggle()
        return items[index]
    }

    func remove(id: Int) async throws -> Bool {
        let before = items.count
        items.removeAll { $0.id == id }
        return items.count < before
    }

    func clearChecked() async throws -> Int {
        let before = items.count
        items.removeAll { $0.checked }
        return before - items.count
    }
}

final class ShoppingAppTests: XCTestCase {
    func testNormalizeCategoryLowercasesAndHyphenates() {
        XCTAssertEqual(normalizeCategory("  Frozen   Food "), "frozen-food")
        XCTAssertEqual(normalizeCategory("   "), "")
    }

    func testVisibleItemsPutUncheckedFirstThenNewest() {
        let all = [
            ShoppingItem(id: 1, name: "old", createdAtEpochSeconds: 1),
            ShoppingItem(id: 2, name: "bought", checked: true, createdAtEpochSeconds: 9),
            ShoppingItem(id: 3, name: "new", createdAtEpochSeconds: 5),
        ]
        XCTAssertEqual(visibleItems(all, category: "").map(\.name), ["new", "old", "bought"])
    }

    func testVisibleItemsFilterByCategory() {
        let all = [
            ShoppingItem(id: 1, name: "apples", category: "produce", createdAtEpochSeconds: 1),
            ShoppingItem(id: 2, name: "soap", category: "household", createdAtEpochSeconds: 2),
        ]
        XCTAssertEqual(visibleItems(all, category: "Produce").map(\.name), ["apples"])
    }

    func testAddRejectsBlankNameAndOutOfRangeQuantity() async {
        let model = ShoppingModel(repository: FakeRepository())
        model.nameInput = "   "
        await model.add()
        XCTAssertEqual(model.errorMessage, "Give the item a name")

        model.nameInput = "Milk"
        model.quantityInput = 0
        await model.add()
        XCTAssertEqual(model.errorMessage, "Quantity must be between 1 and 99")
    }

    func testAddStoresNormalizedItemAndResetsForm() async {
        let model = ShoppingModel(repository: FakeRepository())
        model.nameInput = " Milk "
        model.quantityInput = 2
        model.categoryInput = " Dairy Aisle "
        await model.add()
        XCTAssertEqual(model.items.map(\.name), ["Milk"])
        XCTAssertEqual(model.items.first?.category, "dairy-aisle")
        XCTAssertEqual(model.nameInput, "")
        XCTAssertEqual(model.quantityInput, 1)
    }

    func testClearCheckedDropsTickedItems() async {
        let repo = FakeRepository(items: [
            ShoppingItem(id: 1, name: "a", checked: true, createdAtEpochSeconds: 1),
            ShoppingItem(id: 2, name: "b", createdAtEpochSeconds: 2),
        ])
        let model = ShoppingModel(repository: repo)
        await model.refresh()
        XCTAssertTrue(model.hasCheckedItems)
        await model.clearChecked()
        XCTAssertEqual(model.items.map(\.name), ["b"])
        XCTAssertFalse(model.hasCheckedItems)
    }
}
