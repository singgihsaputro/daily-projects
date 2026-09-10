import Foundation
import Observation

@Observable
final class HabitListModel {
    private(set) var habits: [Habit] = []
    private(set) var isLoading = false
    private(set) var loadError: String?

    private let repository: HabitRepository

    init(repository: HabitRepository = MockHabitRepository()) {
        self.repository = repository
    }

    func load() async {
        isLoading = true
        loadError = nil
        do {
            habits = try await repository.loadHabits()
        } catch {
            loadError = "Couldn't load habits: \(error.localizedDescription)"
        }
        isLoading = false
    }

    func toggle(_ habit: Habit) {
        guard let index = habits.firstIndex(where: { $0.id == habit.id }) else { return }
        habits[index].toggleToday()
    }

    var completedCount: Int {
        habits.count { $0.completedToday }
    }
}
