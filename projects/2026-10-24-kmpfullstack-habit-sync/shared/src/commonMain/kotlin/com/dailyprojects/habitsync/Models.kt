package com.dailyprojects.habitsync

import kotlinx.serialization.Serializable

/** [checkIns] are ISO `YYYY-MM-DD` dates, kept sorted and unique. */
@Serializable
data class Habit(
    val id: Int,
    val name: String,
    val goalPerWeek: Int = 7,
    val checkIns: List<String> = emptyList(),
)

@Serializable
data class CreateHabitRequest(val name: String, val goalPerWeek: Int = 7)

@Serializable
data class CheckInRequest(val date: String)

@Serializable
data class ApiError(val message: String)

/** What a screen draws for one habit on a given day. */
data class HabitView(
    val habit: Habit,
    val streak: Int,
    val doneToday: Boolean,
    val doneThisWeek: Int,
) {
    val goalMet: Boolean get() = doneThisWeek >= habit.goalPerWeek
}
