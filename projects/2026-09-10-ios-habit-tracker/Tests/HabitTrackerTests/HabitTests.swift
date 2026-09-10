import Testing
@testable import HabitTracker

struct HabitTests {
    @Test func toggleTodayIncrementsStreakWhenCompleting() {
        var habit = Habit(
            id: "test",
            name: "Test Habit",
            emoji: "✅",
            streak: 2,
            completedToday: false,
            last7Days: [false, false, false, false, false, false, false]
        )

        habit.toggleToday()

        #expect(habit.completedToday == true)
        #expect(habit.streak == 3)
        #expect(habit.last7Days.last == true)
    }

    @Test func toggleTodayDecrementsStreakWhenUndoing() {
        var habit = Habit(
            id: "test",
            name: "Test Habit",
            emoji: "✅",
            streak: 3,
            completedToday: true,
            last7Days: [false, false, false, false, false, false, true]
        )

        habit.toggleToday()

        #expect(habit.completedToday == false)
        #expect(habit.streak == 2)
        #expect(habit.last7Days.last == false)
    }

    @Test func streakNeverGoesNegative() {
        var habit = Habit(
            id: "test",
            name: "Test Habit",
            emoji: "✅",
            streak: 0,
            completedToday: true,
            last7Days: [false, false, false, false, false, false, true]
        )

        habit.toggleToday()

        #expect(habit.streak == 0)
    }
}
