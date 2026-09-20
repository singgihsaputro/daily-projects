import Foundation

protocol SleepRepository {
    func loadEntries() async throws -> [SleepEntry]
}
