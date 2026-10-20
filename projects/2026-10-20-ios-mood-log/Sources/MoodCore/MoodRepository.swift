import Foundation

public protocol MoodRepository: Sendable {
    func loadEntries() throws -> [MoodEntry]
}

public enum MoodRepositoryError: Error, Equatable {
    case missingResource
    case invalidData
}

/// Reads past entries from the bundled `mock/entries.json` fixture.
/// A real client (server or iCloud) replaces this one type.
public struct MockMoodRepository: MoodRepository {
    private struct Payload: Decodable {
        struct Entry: Decodable {
            let day: String
            let mood: Int
            let note: String
        }
        let entries: [Entry]
    }

    private let data: Data?

    public init() {
        data = Bundle.module.url(forResource: "entries", withExtension: "json")
            .flatMap { try? Data(contentsOf: $0) }
    }

    public init(data: Data) {
        self.data = data
    }

    public func loadEntries() throws -> [MoodEntry] {
        guard let data else { throw MoodRepositoryError.missingResource }
        guard let payload = try? JSONDecoder().decode(Payload.self, from: data) else {
            throw MoodRepositoryError.invalidData
        }
        let entries = try payload.entries.map { raw in
            guard let mood = Mood(rawValue: raw.mood) else { throw MoodRepositoryError.invalidData }
            return MoodEntry(day: raw.day, mood: mood, note: raw.note)
        }
        return entries.sorted { $0.day > $1.day }
    }
}
