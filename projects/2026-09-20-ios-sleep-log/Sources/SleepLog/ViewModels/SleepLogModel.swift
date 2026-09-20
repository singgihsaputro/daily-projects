import Foundation
import Observation

@Observable
final class SleepLogModel {
    private(set) var entries: [SleepEntry] = []
    private(set) var isLoading = false
    private(set) var loadError: String?

    private let repository: SleepRepository

    init(repository: SleepRepository = MockSleepRepository()) {
        self.repository = repository
    }

    func load() async {
        isLoading = true
        loadError = nil
        do {
            entries = try await repository.loadEntries().sorted { $0.date < $1.date }
        } catch {
            loadError = "Couldn't load sleep log: \(error.localizedDescription)"
        }
        isLoading = false
    }

    func addEntry(hours: Double, quality: SleepQuality, on date: Date = Date()) {
        let dateString = SleepLogModel.dateFormatter.string(from: date)
        let entry = SleepEntry(id: UUID().uuidString, date: dateString, hours: hours, quality: quality)
        entries.removeAll { $0.date == dateString }
        entries.append(entry)
        entries.sort { $0.date < $1.date }
    }

    var averageHours: Double {
        entries.isEmpty ? 0 : entries.reduce(0) { $0 + $1.hours } / Double(entries.count)
    }

    var maxHours: Double {
        max(entries.map(\.hours).max() ?? 1, 1)
    }

    static let dateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        formatter.timeZone = TimeZone(identifier: "UTC")
        return formatter
    }()

    static let displayFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateFormat = "EEE, MMM d"
        formatter.timeZone = TimeZone(identifier: "UTC")
        return formatter
    }()

    static func displayDate(for dateString: String) -> String {
        guard let date = dateFormatter.date(from: dateString) else { return dateString }
        return displayFormatter.string(from: date)
    }
}
