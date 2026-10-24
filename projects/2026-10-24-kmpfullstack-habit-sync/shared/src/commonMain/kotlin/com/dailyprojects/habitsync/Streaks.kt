package com.dailyprojects.habitsync

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

const val MAX_NAME_LENGTH = 40
const val MAX_GOAL_PER_WEEK = 7

private val DATE_SHAPE = Regex("\\d{4}-\\d{2}-\\d{2}")

fun parseDate(value: String): LocalDate? =
    if (DATE_SHAPE.matches(value)) runCatching { LocalDate.parse(value) }.getOrNull() else null

/**
 * Consecutive days ending today. Today not being ticked yet does not break the streak —
 * it simply counts back from yesterday, so a morning glance at the app still shows the run.
 */
fun currentStreak(checkIns: List<String>, today: LocalDate): Int {
    val days = checkIns.mapNotNull(::parseDate).toSet()
    var cursor = if (today in days) today else today.minus(DatePeriod(days = 1))
    var streak = 0
    while (cursor in days) {
        streak++
        cursor = cursor.minus(DatePeriod(days = 1))
    }
    return streak
}

/** Check-ins from this week's Monday up to and including [today]. */
fun doneThisWeek(checkIns: List<String>, today: LocalDate): Int {
    val monday = today.minus(DatePeriod(days = today.dayOfWeek.ordinal))
    return checkIns.mapNotNull(::parseDate).count { it >= monday && it <= today }
}

fun viewOf(habit: Habit, today: LocalDate): HabitView = HabitView(
    habit = habit,
    streak = currentStreak(habit.checkIns, today),
    doneToday = today.toString() in habit.checkIns,
    doneThisWeek = doneThisWeek(habit.checkIns, today),
)
