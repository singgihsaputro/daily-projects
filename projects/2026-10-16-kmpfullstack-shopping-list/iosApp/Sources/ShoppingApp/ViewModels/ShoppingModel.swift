import Foundation
import Observation

@Observable
final class ShoppingModel {
    private(set) var items: [ShoppingItem] = []
    private(set) var categories: [String] = []
    private(set) var isLoading = false
    private(set) var errorMessage: String?
    var selectedCategory = ""
    var nameInput = ""
    var quantityInput = 1
    var categoryInput = ""

    private let repository: ShoppingRepository

    init(repository: ShoppingRepository = HttpShoppingRepository()) {
        self.repository = repository
    }

    var hasCheckedItems: Bool { items.contains { $0.checked } }

    func refresh() async {
        isLoading = true
        do {
            let all = try await repository.list()
            items = visibleItems(all, category: selectedCategory)
            categories = Array(Set(all.map(\.category).filter { !$0.isEmpty })).sorted()
            errorMessage = nil
        } catch {
            errorMessage = "Could not reach the server at http://localhost:8080 — is `./gradlew :server:run` running?"
        }
        isLoading = false
    }

    func select(category: String) async {
        selectedCategory = category
        await refresh()
    }

    func add() async {
        let name = nameInput.trimmingCharacters(in: .whitespacesAndNewlines)
        if name.isEmpty {
            errorMessage = "Give the item a name"
        } else if name.count > maxNameLength {
            errorMessage = "Name must be \(maxNameLength) characters or fewer"
        } else if !(1...maxQuantity).contains(quantityInput) {
            errorMessage = "Quantity must be between 1 and \(maxQuantity)"
        } else {
            do {
                _ = try await repository.add(
                    name: name,
                    quantity: quantityInput,
                    category: normalizeCategory(categoryInput)
                )
                nameInput = ""
                quantityInput = 1
                categoryInput = ""
                await refresh()
            } catch {
                errorMessage = error.localizedDescription
            }
        }
    }

    func toggleChecked(id: Int) async {
        do {
            if try await repository.toggleChecked(id: id) == nil {
                errorMessage = "No item with id \(id)"
            } else {
                await refresh()
            }
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func remove(id: Int) async {
        do {
            if try await repository.remove(id: id) {
                await refresh()
            } else {
                errorMessage = "No item with id \(id)"
            }
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func clearChecked() async {
        do {
            _ = try await repository.clearChecked()
            await refresh()
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
