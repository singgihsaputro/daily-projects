import Foundation
import Observation
import MoodCore

@MainActor
@Observable
public final class MoodStore {
    public private(set) var entries: [MoodEntry] = []
    public private(set) var errorMessage: String?
    public let today: String

    private let repository: MoodRepository

    public init(repository: MoodRepository = MockMoodRepository(), now: Date = Date()) {
        self.repository = repository
        self.today = MoodLog.dayString(now)
        do {
            entries = try repository.loadEntries()
        } catch {
            errorMessage = "Could not load your mood log."
        }
    }

    public var summary: MoodSummary { MoodLog.summary(entries, today: today) }

    public var todayEntry: MoodEntry? { entries.first { $0.day == today } }

    public func log(_ mood: Mood, note: String) {
        let trimmed = note.trimmingCharacters(in: .whitespacesAndNewlines)
        entries = MoodLog.upserting(MoodEntry(day: today, mood: mood, note: trimmed), into: entries)
    }
}
