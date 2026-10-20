import Testing
import Foundation
@testable import MoodCore

@Suite struct MoodTests {
    private func entry(_ day: String, _ mood: Mood) -> MoodEntry {
        MoodEntry(day: day, mood: mood)
    }

    @Test func streakCountsConsecutiveDaysEndingToday() {
        let entries = [entry("2026-10-20", .good), entry("2026-10-19", .low),
                       entry("2026-10-18", .okay), entry("2026-10-16", .great)]
        #expect(MoodLog.streak(entries, today: "2026-10-20") == 3)
    }

    @Test func streakSurvivesUnloggedToday() {
        let entries = [entry("2026-10-19", .good), entry("2026-10-18", .good)]
        #expect(MoodLog.streak(entries, today: "2026-10-20") == 2)
        #expect(MoodLog.streak(entries, today: "2026-10-22") == 0)
    }

    @Test func streakCrossesMonthBoundary() {
        let entries = [entry("2026-11-01", .good), entry("2026-10-31", .good)]
        #expect(MoodLog.streak(entries, today: "2026-11-01") == 2)
    }

    @Test func upsertReplacesSameDayAndSortsNewestFirst() {
        let start = [entry("2026-10-18", .low), entry("2026-10-19", .okay)]
        let result = MoodLog.upserting(MoodEntry(day: "2026-10-18", mood: .great, note: "x"), into: start)
        #expect(result.map(\.day) == ["2026-10-19", "2026-10-18"])
        #expect(result[1].mood == .great)
    }

    @Test func summaryAverageAndBest() {
        let entries = [entry("2026-10-18", .great), entry("2026-10-19", .low)]
        let summary = MoodLog.summary(entries, today: "2026-10-19")
        #expect(summary.average == 3.5)
        #expect(summary.best?.day == "2026-10-18")
        #expect(MoodLog.summary([], today: "2026-10-19").average == 0)
    }

    @Test func mockRepositoryLoadsFixture() throws {
        let entries = try MockMoodRepository().loadEntries()
        #expect(entries.count == 6)
        #expect(entries.first?.day == "2026-10-19")
    }

    @Test func invalidMoodIsRejected() {
        let json = #"{"entries":[{"day":"2026-10-19","mood":9,"note":""}]}"#
        #expect(throws: MoodRepositoryError.invalidData) {
            try MockMoodRepository(data: Data(json.utf8)).loadEntries()
        }
    }
}
