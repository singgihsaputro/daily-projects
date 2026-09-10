import Foundation

struct Habit: Identifiable, Codable, Equatable {
    let id: String
    var name: String
    var emoji: String
    var streak: Int
    var completedToday: Bool
    var last7Days: [Bool]

    mutating func toggleToday() {
        completedToday.toggle()
        streak = max(0, streak + (completedToday ? 1 : -1))
        if !last7Days.isEmpty {
            last7Days[last7Days.count - 1] = completedToday
        }
    }
}
