import Foundation

/// Mirrors `com.dailyprojects.habitsync.Habit` from the KMP `shared` module —
/// the same JSON shape the server sends to every client.
struct Habit: Codable, Identifiable, Equatable {
    let id: Int
    let name: String
    var goalPerWeek: Int = 7
    var checkIns: [String] = []
}

struct CreateHabitRequest: Codable {
    let name: String
    let goalPerWeek: Int
}

struct CheckInRequest: Codable {
    let date: String
}

struct ApiError: Codable {
    let message: String
}

let maxNameLength = 40
let maxGoalPerWeek = 7

/// What a row draws for one habit on a given day. Mirrors `HabitView` in `shared`.
struct HabitView: Identifiable, Equatable {
    let habit: Habit
    let streak: Int
    let doneToday: Bool
    let doneThisWeek: Int

    var id: Int { habit.id }
    var goalMet: Bool { doneThisWeek >= habit.goalPerWeek }
}

private func calendar() -> Calendar {
    var calendar = Calendar(identifier: .iso8601)
    calendar.timeZone = TimeZone.current
    return calendar
}

func isoString(_ date: Date) -> String {
    let parts = calendar().dateComponents([.year, .month, .day], from: date)
    return String(format: "%04d-%02d-%02d", parts.year ?? 0, parts.month ?? 0, parts.day ?? 0)
}

/// Mirrors `parseDate`: strict `YYYY-MM-DD`, rejecting impossible dates such as 2026-02-30.
func parseDate(_ value: String) -> Date? {
    let pieces = value.split(separator: "-", omittingEmptySubsequences: false)
    guard pieces.count == 3, pieces[0].count == 4, pieces[1].count == 2, pieces[2].count == 2,
          let year = Int(pieces[0]), let month = Int(pieces[1]), let day = Int(pieces[2]),
          let date = calendar().date(from: DateComponents(year: year, month: month, day: day)),
          isoString(date) == value
    else { return nil }
    return date
}

/// Mirrors `currentStreak`: consecutive days ending today, or yesterday if today is still open.
func currentStreak(_ checkIns: [String], today: Date) -> Int {
    let calendar = calendar()
    let days = Set(checkIns)
    var cursor = days.contains(isoString(today)) ? today : calendar.date(byAdding: .day, value: -1, to: today)!
    var streak = 0
    while days.contains(isoString(cursor)) {
        streak += 1
        cursor = calendar.date(byAdding: .day, value: -1, to: cursor)!
    }
    return streak
}

/// Mirrors `doneThisWeek`: check-ins from this week's Monday through today.
func doneThisWeek(_ checkIns: [String], today: Date) -> Int {
    let calendar = calendar()
    let sinceMonday = (calendar.component(.weekday, from: today) + 5) % 7
    let monday = calendar.date(byAdding: .day, value: -sinceMonday, to: today)!
    let first = isoString(monday)
    let last = isoString(today)
    return checkIns.filter { $0 >= first && $0 <= last }.count
}

/// Mirrors `ListHabitsUseCase`: longest streak first, then name.
func habitViews(_ habits: [Habit], today: Date) -> [HabitView] {
    habits
        .map { habit in
            HabitView(
                habit: habit,
                streak: currentStreak(habit.checkIns, today: today),
                doneToday: habit.checkIns.contains(isoString(today)),
                doneThisWeek: doneThisWeek(habit.checkIns, today: today)
            )
        }
        .sorted { lhs, rhs in
            if lhs.streak != rhs.streak { return lhs.streak > rhs.streak }
            return lhs.habit.name.lowercased() < rhs.habit.name.lowercased()
        }
}
