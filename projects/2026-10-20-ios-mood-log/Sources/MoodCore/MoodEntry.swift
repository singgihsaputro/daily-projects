import Foundation

public enum Mood: Int, CaseIterable, Sendable, Codable {
    case awful = 1, low, okay, good, great

    public var emoji: String {
        switch self {
        case .awful: "😞"
        case .low: "🙁"
        case .okay: "😐"
        case .good: "🙂"
        case .great: "😄"
        }
    }

    public var label: String {
        switch self {
        case .awful: "Awful"
        case .low: "Low"
        case .okay: "Okay"
        case .good: "Good"
        case .great: "Great"
        }
    }
}

public struct MoodEntry: Identifiable, Equatable, Sendable {
    /// Calendar day as `yyyy-MM-dd`; also the identity, so one entry per day.
    public let day: String
    public var mood: Mood
    public var note: String

    public var id: String { day }

    public init(day: String, mood: Mood, note: String = "") {
        self.day = day
        self.mood = mood
        self.note = note
    }
}

public struct MoodSummary: Equatable, Sendable {
    public let average: Double
    public let streak: Int
    public let best: MoodEntry?
}

public enum MoodLog {
    private static let calendar: Calendar = {
        var c = Calendar(identifier: .gregorian)
        c.timeZone = TimeZone(identifier: "UTC")!
        return c
    }()

    private static let formatter: DateFormatter = {
        let f = DateFormatter()
        f.calendar = calendar
        f.timeZone = calendar.timeZone
        f.locale = Locale(identifier: "en_US_POSIX")
        f.dateFormat = "yyyy-MM-dd"
        return f
    }()

    public static func dayString(_ date: Date) -> String {
        formatter.string(from: date)
    }

    /// Inserts or replaces the entry for its day and keeps newest first.
    public static func upserting(_ entry: MoodEntry, into entries: [MoodEntry]) -> [MoodEntry] {
        var result = entries.filter { $0.day != entry.day }
        result.append(entry)
        return result.sorted { $0.day > $1.day }
    }

    /// Consecutive logged days ending at `today` (or yesterday, if today is not logged yet).
    public static func streak(_ entries: [MoodEntry], today: String) -> Int {
        let days = Set(entries.map(\.day))
        guard var cursor = formatter.date(from: today) else { return 0 }
        if !days.contains(today) {
            guard let yesterday = calendar.date(byAdding: .day, value: -1, to: cursor) else { return 0 }
            cursor = yesterday
        }
        var count = 0
        while days.contains(formatter.string(from: cursor)) {
            count += 1
            guard let previous = calendar.date(byAdding: .day, value: -1, to: cursor) else { break }
            cursor = previous
        }
        return count
    }

    public static func summary(_ entries: [MoodEntry], today: String) -> MoodSummary {
        let average = entries.isEmpty
            ? 0
            : Double(entries.map(\.mood.rawValue).reduce(0, +)) / Double(entries.count)
        let best = entries.max { ($0.mood.rawValue, $0.day) < ($1.mood.rawValue, $1.day) }
        return MoodSummary(average: average, streak: streak(entries, today: today), best: best)
    }
}
