import Foundation

protocol HabitRepository {
    func loadHabits() async throws -> [Habit]
}
