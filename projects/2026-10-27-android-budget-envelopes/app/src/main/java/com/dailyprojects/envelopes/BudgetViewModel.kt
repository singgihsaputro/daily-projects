package com.dailyprojects.envelopes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BudgetUiState(
    val month: String = "",
    val daysLeft: Int = 0,
    val envelopes: List<Envelope> = emptyList(),
) {
    val totalBudget: Int get() = envelopes.sumOf { it.budget }
    val totalSpent: Int get() = envelopes.sumOf { it.spent }
    val totalLeft: Int get() = totalBudget - totalSpent

    /** What can still be spent per day without going over, counting only envelopes with money left. */
    val safePerDay: Int
        get() = if (daysLeft <= 0) 0 else envelopes.sumOf { it.left.coerceAtLeast(0) } / daysLeft
}

class BudgetViewModel(private val repository: BudgetRepository) : ViewModel() {
    private val _state = MutableStateFlow(BudgetUiState())
    val state: StateFlow<BudgetUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val data = repository.load()
            _state.value = BudgetUiState(data.month, data.daysLeft, data.envelopes)
        }
    }

    fun spend(id: String, amount: Int) = adjust(id, amount)

    /** Undo a spend; an envelope never goes below zero spent. */
    fun refund(id: String, amount: Int) = adjust(id, -amount)

    private fun adjust(id: String, delta: Int) = _state.update { s ->
        s.copy(envelopes = s.envelopes.map {
            if (it.id == id) it.copy(spent = (it.spent + delta).coerceAtLeast(0)) else it
        })
    }
}
