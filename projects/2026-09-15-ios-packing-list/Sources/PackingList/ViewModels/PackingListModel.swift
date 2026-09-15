import Foundation
import Observation

@Observable
final class PackingListModel {
    private(set) var items: [PackingItem] = []
    private(set) var isLoading = false
    private(set) var loadError: String?

    private let repository: PackingRepository

    init(repository: PackingRepository = MockPackingRepository()) {
        self.repository = repository
    }

    func load() async {
        isLoading = true
        loadError = nil
        do {
            items = try await repository.loadItems()
        } catch {
            loadError = "Couldn't load packing list: \(error.localizedDescription)"
        }
        isLoading = false
    }

    func toggle(_ item: PackingItem) {
        guard let index = items.firstIndex(where: { $0.id == item.id }) else { return }
        items[index].togglePacked()
    }

    var categories: [PackingCategory] {
        PackingCategory.allCases.filter { category in items.contains { $0.category == category } }
    }

    func items(in category: PackingCategory) -> [PackingItem] {
        items.filter { $0.category == category }
    }

    var packedCount: Int {
        items.count { $0.packed }
    }

    var progress: Double {
        items.isEmpty ? 0 : Double(packedCount) / Double(items.count)
    }
}
