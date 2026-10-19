package com.dailyprojects.caffeine

import kotlin.math.pow

data class Drink(val id: String, val name: String, val mg: Int)

/** One logged drink; [minutesAgo] is measured against the moment the data was loaded. */
data class Entry(val drink: Drink, val minutesAgo: Int)

data class CaffeineData(val dailyLimitMg: Int, val drinks: List<Drink>, val log: List<Entry>)

interface CaffeineRepository {
    suspend fun load(): CaffeineData
}

object Pharmacokinetics {
    const val HALF_LIFE_MINUTES = 300.0
    const val SLEEP_SAFE_MG = 50.0

    /** Milligrams of a single dose still active after [minutes], assuming first-order decay. */
    fun remaining(mg: Int, minutes: Int): Double = mg * 0.5.pow(minutes / HALF_LIFE_MINUTES)

    fun activeMg(log: List<Entry>): Double = log.sumOf { remaining(it.drink.mg, it.minutesAgo) }

    /** Minutes from now until [activeMg] decays below [SLEEP_SAFE_MG]; 0 if already below. */
    fun minutesUntilSleepSafe(log: List<Entry>): Int {
        val now = activeMg(log)
        if (now <= SLEEP_SAFE_MG) return 0
        return (HALF_LIFE_MINUTES * kotlin.math.log2(now / SLEEP_SAFE_MG)).toInt() + 1
    }
}
