package com.dailyprojects.habitsync

/** The one contract every target codes against — an in-memory store on the server, HTTP on every client. */
interface HabitRepository {
    suspend fun list(): List<Habit>
    suspend fun add(name: String, goalPerWeek: Int): Habit

    /** Adds [date] to the habit's check-ins, or removes it if already there. Null when the id is unknown. */
    suspend fun toggleCheckIn(id: Int, date: String): Habit?
    suspend fun remove(id: Int): Boolean
}
