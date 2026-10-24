import XCTest
@testable import HabitApp

private final class FakeRepository: HabitRepository {
    var habits: [Habit]

    init(habits: [Habit] = []) {
        self.habits = habits
    }

    func list() async throws -> [Habit] { habits }

    func add(name: String, goalPerWeek: Int) async throws -> Habit {
        let habit = Habit(id: habits.count + 1, name: name, goalPerWeek: goalPerWeek)
        habits.append(habit)
        return habit
    }

    func toggleCheckIn(id: Int, date: String) async throws -> Habit? {
        guard let index = habits.firstIndex(where: { $0.id == id }) else { return nil }
        if let existing = habits[index].checkIns.firstIndex(of: date) {
            habits[index].checkIns.remove(at: existing)
        } else {
            habits[index].checkIns = (habits[index].checkIns + [date]).sorted()
        }
        return habits[index]
    }

    func remove(id: Int) async throws -> Bool {
        let before = habits.count
        habits.removeAll { $0.id == id }
        return habits.count < before
    }
}

final class HabitAppTests: XCTestCase {
    /// 2026-10-24, a Saturday — the same fixed "today" the shared Kotlin tests use.
    private let today = parseDate("2026-10-24")!

    func testParseDateRejectsMalformedAndImpossibleDates() {
        XCTAssertNotNil(parseDate("2026-10-24"))
        XCTAssertNil(parseDate("2026-02-30"))
        XCTAssertNil(parseDate("tomorrow"))
        XCTAssertNil(parseDate("2026-1-5"))
    }

    func testStreakCountsBackFromTodayOrYesterday() {
        XCTAssertEqual(currentStreak(["2026-10-22", "2026-10-23", "2026-10-24"], today: today), 3)
        XCTAssertEqual(currentStreak(["2026-10-22", "2026-10-23"], today: today), 2)
        XCTAssertEqual(currentStreak(["2026-10-21"], today: today), 0)
        XCTAssertEqual(currentStreak(["2026-10-20", "2026-10-22", "2026-10-24"], today: today), 1)
    }

    func testWeekCountStartsOnMonday() {
        let days = ["2026-10-18", "2026-10-19", "2026-10-21", "2026-10-24"]
        XCTAssertEqual(doneThisWeek(days, today: today), 3)
    }

    func testViewsPutLongestStreakFirst() {
        let habits = [
            Habit(id: 1, name: "Stretch", checkIns: ["2026-10-24"]),
            Habit(id: 2, name: "Read", checkIns: ["2026-10-22", "2026-10-23", "2026-10-24"]),
            Habit(id: 3, name: "Alpha"),
        ]
        XCTAssertEqual(habitViews(habits, today: today).map(\.habit.name), ["Read", "Stretch", "Alpha"])
    }

    func testAddRejectsBlankNameAndOutOfRangeGoal() async {
        let model = HabitModel(repository: FakeRepository(), today: { self.today })
        model.nameInput = "   "
        await model.add()
        XCTAssertEqual(model.errorMessage, "Give the habit a name")

        model.nameInput = "Read"
        model.goalInput = 0
        await model.add()
        XCTAssertEqual(model.errorMessage, "Goal must be between 1 and 7 days a week")
    }

    func testToggleTodayTicksAndUnticks() async {
        let repo = FakeRepository(habits: [Habit(id: 1, name: "Read")])
        let model = HabitModel(repository: repo, today: { self.today })
        await model.refresh()
        await model.toggleToday(id: 1)
        XCTAssertEqual(model.habits.first?.doneToday, true)
        XCTAssertEqual(model.habits.first?.streak, 1)
        await model.toggleToday(id: 1)
        XCTAssertEqual(model.habits.first?.doneToday, false)
    }

    func testRemoveDropsHabit() async {
        let repo = FakeRepository(habits: [Habit(id: 1, name: "Read"), Habit(id: 2, name: "Run")])
        let model = HabitModel(repository: repo, today: { self.today })
        await model.remove(id: 1)
        XCTAssertEqual(model.habits.map(\.habit.name), ["Run"])
    }
}
