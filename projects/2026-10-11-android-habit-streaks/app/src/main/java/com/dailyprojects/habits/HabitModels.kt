package com.dailyprojects.habits

import java.time.LocalDate

data class Habit(
    val id: Int,
    val name: String,
    val emoji: String,
    val doneDays: Set<LocalDate>,
) {
    fun isDone(day: LocalDate) = day in doneDays

    /**
     * Consecutive completed days ending today. If today is not done yet the streak
     * is still alive and counts back from yesterday.
     */
    fun streak(today: LocalDate): Int {
        var day = if (isDone(today)) today else today.minusDays(1)
        var count = 0
        while (isDone(day)) {
            count++
            day = day.minusDays(1)
        }
        return count
    }

    /** Longest run of consecutive completed days in the whole history. */
    fun bestStreak(): Int {
        var best = 0
        var run = 0
        var previous: LocalDate? = null
        for (day in doneDays.sorted()) {
            run = if (previous != null && previous.plusDays(1) == day) run + 1 else 1
            best = maxOf(best, run)
            previous = day
        }
        return best
    }

    fun toggled(day: LocalDate) =
        copy(doneDays = if (isDone(day)) doneDays - day else doneDays + day)
}

interface HabitRepository {
    suspend fun habits(today: LocalDate): List<Habit>
}
