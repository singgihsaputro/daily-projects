package com.dailyprojects.countdown

import java.time.Duration
import java.time.LocalDateTime

data class CountdownEvent(
    val id: Int,
    val title: String,
    val emoji: String,
    val date: LocalDateTime,
)

data class Remaining(val days: Long, val hours: Long, val minutes: Long, val seconds: Long)

sealed interface EventStatus {
    data class Upcoming(val remaining: Remaining) : EventStatus
    data class Passed(val daysAgo: Long) : EventStatus
}

fun CountdownEvent.statusAt(now: LocalDateTime): EventStatus {
    val diff = Duration.between(now, date)
    if (diff.isNegative) return EventStatus.Passed(daysAgo = -diff.toDays())
    return EventStatus.Upcoming(
        Remaining(
            days = diff.toDays(),
            hours = diff.toHours() % 24,
            minutes = diff.toMinutes() % 60,
            seconds = diff.seconds % 60,
        )
    )
}

interface EventRepository {
    suspend fun events(): List<CountdownEvent>
}
