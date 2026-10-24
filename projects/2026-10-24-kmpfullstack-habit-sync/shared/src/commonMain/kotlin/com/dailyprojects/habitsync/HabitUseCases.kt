package com.dailyprojects.habitsync

import kotlinx.datetime.LocalDate

sealed class HabitResult<out T> {
    data class Success<T>(val value: T) : HabitResult<T>()
    data class Failure(val message: String) : HabitResult<Nothing>()
}

/** Longest streak first, then name — the habits worth protecting float to the top. */
class ListHabitsUseCase(private val repository: HabitRepository) {
    suspend operator fun invoke(today: LocalDate): List<HabitView> =
        repository.list()
            .map { viewOf(it, today) }
            .sortedWith(compareByDescending<HabitView> { it.streak }.thenBy { it.habit.name.lowercase() })
}

class AddHabitUseCase(private val repository: HabitRepository) {
    suspend operator fun invoke(name: String, goalPerWeek: Int = 7): HabitResult<Habit> {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return HabitResult.Failure("Give the habit a name")
        if (cleanName.length > MAX_NAME_LENGTH) {
            return HabitResult.Failure("Name must be $MAX_NAME_LENGTH characters or fewer")
        }
        if (goalPerWeek !in 1..MAX_GOAL_PER_WEEK) {
            return HabitResult.Failure("Goal must be between 1 and $MAX_GOAL_PER_WEEK days a week")
        }
        return HabitResult.Success(repository.add(cleanName, goalPerWeek))
    }
}

/** Ticks or un-ticks [date]. Future dates are refused so a streak cannot be pre-filled. */
class ToggleCheckInUseCase(private val repository: HabitRepository) {
    suspend operator fun invoke(id: Int, date: String, today: LocalDate): HabitResult<Habit> {
        val parsed = parseDate(date) ?: return HabitResult.Failure("Date must look like YYYY-MM-DD")
        if (parsed > today) return HabitResult.Failure("You cannot check in on a future date")
        val updated = repository.toggleCheckIn(id, parsed.toString()) ?: return HabitResult.Failure("No habit with id $id")
        return HabitResult.Success(updated)
    }
}

class RemoveHabitUseCase(private val repository: HabitRepository) {
    suspend operator fun invoke(id: Int): HabitResult<Unit> =
        if (repository.remove(id)) HabitResult.Success(Unit) else HabitResult.Failure("No habit with id $id")
}
