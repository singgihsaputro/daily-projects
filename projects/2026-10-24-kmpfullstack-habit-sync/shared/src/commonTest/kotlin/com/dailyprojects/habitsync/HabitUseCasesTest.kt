package com.dailyprojects.habitsync

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

private val TODAY = LocalDate.parse("2026-10-24") // a Saturday

private class FakeHabitRepository(seed: List<Habit> = emptyList()) : HabitRepository {
    private val habits = seed.toMutableList()
    override suspend fun list(): List<Habit> = habits.toList()
    override suspend fun add(name: String, goalPerWeek: Int): Habit =
        Habit(habits.size + 1, name, goalPerWeek).also { habits += it }
    override suspend fun toggleCheckIn(id: Int, date: String): Habit? {
        val index = habits.indexOfFirst { it.id == id }
        if (index == -1) return null
        val days = habits[index].checkIns
        habits[index] = habits[index].copy(checkIns = if (date in days) days - date else (days + date).sorted())
        return habits[index]
    }
    override suspend fun remove(id: Int): Boolean = habits.removeAll { it.id == id }
}

class HabitUseCasesTest {
    @Test
    fun addRejectsBlankNameLongNameAndBadGoal() = runTest {
        val add = AddHabitUseCase(FakeHabitRepository())
        assertIs<HabitResult.Failure>(add("  ", 3))
        assertIs<HabitResult.Failure>(add("x".repeat(MAX_NAME_LENGTH + 1), 3))
        assertIs<HabitResult.Failure>(add("Read", 0))
        assertIs<HabitResult.Failure>(add("Read", MAX_GOAL_PER_WEEK + 1))
    }

    @Test
    fun addTrimsName() = runTest {
        val habit = assertIs<HabitResult.Success<Habit>>(AddHabitUseCase(FakeHabitRepository())("  Read  ", 5)).value
        assertEquals("Read", habit.name)
        assertEquals(5, habit.goalPerWeek)
    }

    @Test
    fun streakCountsBackFromTodayOrYesterday() {
        assertEquals(3, currentStreak(listOf("2026-10-22", "2026-10-23", "2026-10-24"), TODAY))
        assertEquals(2, currentStreak(listOf("2026-10-22", "2026-10-23"), TODAY))
        assertEquals(0, currentStreak(listOf("2026-10-21"), TODAY))
        assertEquals(1, currentStreak(listOf("2026-10-20", "2026-10-22", "2026-10-24"), TODAY))
    }

    @Test
    fun weekCountStartsOnMonday() {
        // 2026-10-19 is the Monday; the 18th belongs to last week.
        val days = listOf("2026-10-18", "2026-10-19", "2026-10-21", "2026-10-24")
        assertEquals(3, doneThisWeek(days, TODAY))
    }

    @Test
    fun listPutsLongestStreakFirst() = runTest {
        val repo = FakeHabitRepository(
            listOf(
                Habit(1, "Stretch", 7, listOf("2026-10-24")),
                Habit(2, "Read", 7, listOf("2026-10-22", "2026-10-23", "2026-10-24")),
                Habit(3, "Alpha", 7),
            ),
        )
        assertEquals(listOf("Read", "Stretch", "Alpha"), ListHabitsUseCase(repo)(TODAY).map { it.habit.name })
    }

    @Test
    fun toggleValidatesDateAndId() = runTest {
        val repo = FakeHabitRepository(listOf(Habit(1, "Read")))
        val toggle = ToggleCheckInUseCase(repo)
        assertIs<HabitResult.Failure>(toggle(1, "yesterday", TODAY))
        assertIs<HabitResult.Failure>(toggle(1, "2026-02-30", TODAY))
        assertIs<HabitResult.Failure>(toggle(1, "2026-10-25", TODAY))
        assertIs<HabitResult.Failure>(toggle(9, "2026-10-24", TODAY))
        val ticked = assertIs<HabitResult.Success<Habit>>(toggle(1, "2026-10-24", TODAY)).value
        assertEquals(listOf("2026-10-24"), ticked.checkIns)
        val unticked = assertIs<HabitResult.Success<Habit>>(toggle(1, "2026-10-24", TODAY)).value
        assertEquals(emptyList(), unticked.checkIns)
    }

    @Test
    fun removeReportsMissingIds() = runTest {
        val repo = FakeHabitRepository(listOf(Habit(1, "Read")))
        assertIs<HabitResult.Success<Unit>>(RemoveHabitUseCase(repo)(1))
        assertIs<HabitResult.Failure>(RemoveHabitUseCase(repo)(1))
    }

    @Test
    fun goalMetWhenWeekCountReachesGoal() {
        val view = viewOf(Habit(1, "Run", 2, listOf("2026-10-20", "2026-10-24")), TODAY)
        assertEquals(true, view.goalMet)
        assertEquals(true, view.doneToday)
    }
}
