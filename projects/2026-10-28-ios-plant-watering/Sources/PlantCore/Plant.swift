import Foundation

public struct Plant: Identifiable, Equatable, Sendable {
    public let id: String
    public let name: String
    public let room: String
    public let everyDays: Int
    public var lastWatered: String

    public init(id: String, name: String, room: String, everyDays: Int, lastWatered: String) {
        self.id = id
        self.name = name
        self.room = room
        self.everyDays = everyDays
        self.lastWatered = lastWatered
    }
}

public enum WaterStatus: Equatable, Sendable {
    case overdue(days: Int)
    case dueToday
    case upcoming(days: Int)

    public var label: String {
        switch self {
        case .overdue(let days): return days == 1 ? "1 day overdue" : "\(days) days overdue"
        case .dueToday: return "Water today"
        case .upcoming(let days): return days == 1 ? "Tomorrow" : "In \(days) days"
        }
    }

    public var needsWater: Bool {
        if case .upcoming = self { return false }
        return true
    }
}

public enum DayMath {
    private static let calendar: Calendar = {
        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = TimeZone(identifier: "UTC")!
        return calendar
    }()

    /// Parses `yyyy-MM-dd` into a UTC date; nil when malformed.
    public static func date(from day: String) -> Date? {
        let parts = day.split(separator: "-").compactMap { Int($0) }
        guard parts.count == 3 else { return nil }
        let components = DateComponents(year: parts[0], month: parts[1], day: parts[2])
        guard let date = calendar.date(from: components),
              calendar.dateComponents([.year, .month, .day], from: date) == components
                .withCalendarFields else { return nil }
        return date
    }

    public static func string(from date: Date) -> String {
        let c = calendar.dateComponents([.year, .month, .day], from: date)
        return String(format: "%04d-%02d-%02d", c.year ?? 0, c.month ?? 0, c.day ?? 0)
    }

    public static func days(from start: String, to end: String) -> Int? {
        guard let a = date(from: start), let b = date(from: end) else { return nil }
        return calendar.dateComponents([.day], from: a, to: b).day
    }

    public static func adding(days: Int, to day: String) -> String? {
        guard let start = date(from: day),
              let result = calendar.date(byAdding: .day, value: days, to: start) else { return nil }
        return string(from: result)
    }
}

private extension DateComponents {
    var withCalendarFields: DateComponents {
        DateComponents(year: year, month: month, day: day)
    }
}

public struct WateringPlan {
    public let today: String

    public init(today: String) {
        self.today = today
    }

    public func status(of plant: Plant) -> WaterStatus {
        let elapsed = DayMath.days(from: plant.lastWatered, to: today) ?? 0
        let remaining = plant.everyDays - elapsed
        if remaining < 0 { return .overdue(days: -remaining) }
        if remaining == 0 { return .dueToday }
        return .upcoming(days: remaining)
    }

    /// Plants that need water first (most overdue first), then by soonest due.
    public func sorted(_ plants: [Plant]) -> [Plant] {
        plants.sorted { a, b in
            let (ra, rb) = (remaining(a), remaining(b))
            return ra == rb ? a.name < b.name : ra < rb
        }
    }

    public func dueCount(in plants: [Plant]) -> Int {
        plants.filter { status(of: $0).needsWater }.count
    }

    private func remaining(_ plant: Plant) -> Int {
        plant.everyDays - (DayMath.days(from: plant.lastWatered, to: today) ?? 0)
    }
}
