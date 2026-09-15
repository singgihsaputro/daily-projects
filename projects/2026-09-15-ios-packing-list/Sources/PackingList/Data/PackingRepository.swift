import Foundation

protocol PackingRepository {
    func loadItems() async throws -> [PackingItem]
}
