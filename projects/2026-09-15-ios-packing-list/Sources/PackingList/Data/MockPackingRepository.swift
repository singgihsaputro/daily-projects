import Foundation

enum MockPackingRepositoryError: Error {
    case fixtureNotFound
}

/// Reads packing items from the bundled `packing.json` fixture. A future
/// networked implementation of `PackingRepository` can replace this one
/// without any changes to the view model or views.
struct MockPackingRepository: PackingRepository {
    func loadItems() async throws -> [PackingItem] {
        guard let url = Bundle.module.url(forResource: "packing", withExtension: "json") else {
            throw MockPackingRepositoryError.fixtureNotFound
        }
        let data = try Data(contentsOf: url)
        return try JSONDecoder().decode([PackingItem].self, from: data)
    }
}
