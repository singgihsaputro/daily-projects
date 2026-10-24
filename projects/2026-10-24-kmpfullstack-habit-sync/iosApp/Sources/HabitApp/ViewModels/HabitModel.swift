import Foundation
import Observation

@Observable
final class HabitModel {
    private(set) var habits: [HabitView] = []
    private(set) var isLoading = false
    private(set) var errorMessage: String?
    var nameInput = ""
    var goalInput = 7

    private let repository: HabitRepository
    private let today: () -> Date

    init(repository: HabitRepository = HttpHabitRepository(), today: @escaping () -> Date = { Date() }) {
        self.repository = repository
        self.today = today
    }

    func refresh() async {
        isLoading = true
        do {
            habits = habitViews(try await repository.list(), today: today())
            errorMessage = nil
        } catch {
            errorMessage = "Could not reach the server at http://localhost:8080 — is `./gradlew :server:run` running?"
        }
        isLoading = false
    }

    func add() async {
        let name = nameInput.trimmingCharacters(in: .whitespacesAndNewlines)
        if name.isEmpty {
            errorMessage = "Give the habit a name"
        } else if name.count > maxNameLength {
            errorMessage = "Name must be \(maxNameLength) characters or fewer"
        } else if !(1...maxGoalPerWeek).contains(goalInput) {
            errorMessage = "Goal must be between 1 and \(maxGoalPerWeek) days a week"
        } else {
            do {
                _ = try await repository.add(name: name, goalPerWeek: goalInput)
                nameInput = ""
                goalInput = 7
                await refresh()
            } catch {
                errorMessage = error.localizedDescription
            }
        }
    }

    /// Ticks today if it is open, un-ticks it if it is done.
    func toggleToday(id: Int) async {
        do {
            if try await repository.toggleCheckIn(id: id, date: isoString(today())) == nil {
                errorMessage = "No habit with id \(id)"
            } else {
                await refresh()
            }
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func remove(id: Int) async {
        do {
            if try await repository.remove(id: id) {
                await refresh()
            } else {
                errorMessage = "No habit with id \(id)"
            }
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
