import Foundation

/// The client-side contract — same shape as the `HabitRepository` interface in
/// `shared/src/commonMain`, reimplemented in Swift since Kotlin/Native can't cross-compile
/// an iOS `.xcframework` on the Linux runner that authored this (see the project README).
protocol HabitRepository {
    func list() async throws -> [Habit]
    func add(name: String, goalPerWeek: Int) async throws -> Habit
    func toggleCheckIn(id: Int, date: String) async throws -> Habit?
    func remove(id: Int) async throws -> Bool
}

enum HabitRepositoryError: LocalizedError {
    case rejected(String)
    case server(String)

    var errorDescription: String? {
        switch self {
        case .rejected(let message), .server(let message):
            return message
        }
    }
}
