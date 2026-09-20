import Foundation
import Testing
@testable import SleepLog

struct SleepEntryTests {
    @Test func decodesFromFixtureJSON() throws {
        let json = """
        [{ "id": "n1", "date": "2026-09-14", "hours": 6.5, "quality": "fair" }]
        """
        let entries = try JSONDecoder().decode([SleepEntry].self, from: Data(json.utf8))

        #expect(entries.count == 1)
        #expect(entries[0].quality == .fair)
        #expect(entries[0].hours == 6.5)
    }

    @Test func addEntryReplacesSameDayEntry() {
        let model = SleepLogModel(repository: MockSleepRepository())
        let date = SleepLogModel.dateFormatter.date(from: "2026-09-21")!

        model.addEntry(hours: 6, quality: .poor, on: date)
        model.addEntry(hours: 8, quality: .great, on: date)

        let matching = model.entries.filter { $0.date == "2026-09-21" }
        #expect(matching.count == 1)
        #expect(matching[0].hours == 8)
        #expect(matching[0].quality == .great)
    }

    @Test func averageHoursIsZeroWhenEmpty() {
        let model = SleepLogModel(repository: MockSleepRepository())
        #expect(model.averageHours == 0)
    }

    @Test func displayDateFormatsFixtureString() {
        let display = SleepLogModel.displayDate(for: "2026-09-20")
        #expect(display == "Sun, Sep 20")
    }
}
