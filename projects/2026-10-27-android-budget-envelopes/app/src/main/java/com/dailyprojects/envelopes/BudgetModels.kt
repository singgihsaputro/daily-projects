package com.dailyprojects.envelopes

enum class Status { OK, NEAR_LIMIT, EMPTY, OVER }

data class Envelope(val id: String, val name: String, val budget: Int, val spent: Int) {
    val left: Int get() = budget - spent

    val status: Status
        get() = when {
            spent > budget -> Status.OVER
            spent == budget -> Status.EMPTY
            spent * 100 >= budget * 80 -> Status.NEAR_LIMIT
            else -> Status.OK
        }

    /** Fill ratio for the progress bar, clamped to 0..1. */
    val fraction: Float get() = if (budget == 0) 1f else (spent.toFloat() / budget).coerceIn(0f, 1f)
}

data class BudgetData(val month: String, val daysLeft: Int, val envelopes: List<Envelope>)

interface BudgetRepository {
    suspend fun load(): BudgetData
}
