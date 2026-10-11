package com.dailyprojects.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HabitUiState(
    val today: LocalDate = LocalDate.now(),
    val habits: List<Habit> = emptyList(),
    val loading: Boolean = true,
) {
    val doneToday get() = habits.count { it.isDone(today) }
}

class HabitViewModel(private val repository: HabitRepository) : ViewModel() {
    private val _state = MutableStateFlow(HabitUiState())
    val state: StateFlow<HabitUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val habits = repository.habits(_state.value.today)
            _state.update { it.copy(habits = habits, loading = false) }
        }
    }

    fun toggleToday(id: Int) = _state.update { state ->
        state.copy(habits = state.habits.map { if (it.id == id) it.toggled(state.today) else it })
    }
}
