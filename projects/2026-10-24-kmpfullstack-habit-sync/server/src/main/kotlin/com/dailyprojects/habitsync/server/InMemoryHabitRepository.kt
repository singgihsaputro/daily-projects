package com.dailyprojects.habitsync.server

import com.dailyprojects.habitsync.Habit
import com.dailyprojects.habitsync.HabitRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** The server's source of truth: an in-memory list guarded by a mutex. */
class InMemoryHabitRepository(seed: List<Habit> = emptyList()) : HabitRepository {
    private val mutex = Mutex()
    private val habits = seed.toMutableList()
    private var nextId = (seed.maxOfOrNull { it.id } ?: 0) + 1

    override suspend fun list(): List<Habit> = mutex.withLock { habits.toList() }

    override suspend fun add(name: String, goalPerWeek: Int): Habit = mutex.withLock {
        Habit(id = nextId++, name = name, goalPerWeek = goalPerWeek).also { habits += it }
    }

    override suspend fun toggleCheckIn(id: Int, date: String): Habit? = mutex.withLock {
        val index = habits.indexOfFirst { it.id == id }
        if (index == -1) return@withLock null
        val days = habits[index].checkIns
        val updated = habits[index].copy(checkIns = if (date in days) days - date else (days + date).sorted())
        habits[index] = updated
        updated
    }

    override suspend fun remove(id: Int): Boolean = mutex.withLock { habits.removeAll { it.id == id } }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        /**
         * Reads `mock/habits.json`. Check-ins there are "days ago" so the seed always has a
         * live streak whatever day the server is started on.
         */
        fun seededFromResource(today: LocalDate, resourcePath: String = "/habits.json"): InMemoryHabitRepository {
            val text = InMemoryHabitRepository::class.java.getResourceAsStream(resourcePath)
                ?.bufferedReader()?.readText()
                ?: error("missing seed resource $resourcePath")
            val seed = json.decodeFromString<List<SeedHabit>>(text).map { entry ->
                Habit(
                    id = entry.id,
                    name = entry.name,
                    goalPerWeek = entry.goalPerWeek,
                    checkIns = entry.checkInDaysAgo.map { today.minus(DatePeriod(days = it)).toString() }.sorted(),
                )
            }
            return InMemoryHabitRepository(seed)
        }
    }
}

@Serializable
private data class SeedHabit(val id: Int, val name: String, val goalPerWeek: Int, val checkInDaysAgo: List<Int>)
