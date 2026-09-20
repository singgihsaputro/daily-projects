import Foundation

enum MockSleepRepositoryError: Error {
    case fixtureNotFound
}

/// Reads sleep entries from the bundled `sleep.json` fixture. A future
/// networked implementation of `SleepRepository` can replace this one
/// without any changes to the view model or views.
struct MockSleepRepository: SleepRepository {
    func loadEntries() async throws -> [SleepEntry] {
        guard let url = Bundle.module.url(forResource: "sleep", withExtension: "json") else {
            throw MockSleepRepositoryError.fixtureNotFound
        }
        let data = try Data(contentsOf: url)
        return try JSONDecoder().decode([SleepEntry].self, from: data)
    }
}
