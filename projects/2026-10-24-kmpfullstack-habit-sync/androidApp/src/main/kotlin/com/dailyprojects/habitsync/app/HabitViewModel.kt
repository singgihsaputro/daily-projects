package com.dailyprojects.habitsync.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailyprojects.habitsync.AddHabitUseCase
import com.dailyprojects.habitsync.HabitRepository
import com.dailyprojects.habitsync.HabitResult
import com.dailyprojects.habitsync.HabitView
import com.dailyprojects.habitsync.ListHabitsUseCase
import com.dailyprojects.habitsync.RemoveHabitUseCase
import com.dailyprojects.habitsync.ToggleCheckInUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

data class HabitUiState(
    val habits: List<HabitView> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val nameInput: String = "",
    val goalInput: String = "7",
)

class HabitViewModel(repository: HabitRepository = HttpHabitRepository()) : ViewModel() {
    private val listHabits = ListHabitsUseCase(repository)
    private val addHabit = AddHabitUseCase(repository)
    private val toggleCheckIn = ToggleCheckInUseCase(repository)
    private val removeHabit = RemoveHabitUseCase(repository)

    private val _state = MutableStateFlow(HabitUiState())
    val state: StateFlow<HabitUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    private fun today(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

    fun onNameChange(value: String) = _state.update { it.copy(nameInput = value) }
    fun onGoalChange(value: String) = _state.update { it.copy(goalInput = value.filter(Char::isDigit).take(1)) }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching { listHabits(today()) }
                .onSuccess { habits -> _state.update { it.copy(habits = habits, isLoading = false, errorMessage = null) } }
                .onFailure { error -> _state.update { it.copy(isLoading = false, errorMessage = error.message) } }
        }
    }

    fun add() {
        val current = _state.value
        viewModelScope.launch {
            when (val result = addHabit(current.nameInput, current.goalInput.toIntOrNull() ?: 0)) {
                is HabitResult.Success -> {
                    _state.update { it.copy(nameInput = "", goalInput = "7", errorMessage = null) }
                    refresh()
                }
                is HabitResult.Failure -> _state.update { it.copy(errorMessage = result.message) }
            }
        }
    }

    /** Ticks today if it is open, un-ticks it if it is done. */
    fun toggleToday(id: Int) = act { toggleCheckIn(id, today().toString(), today()) }

    fun remove(id: Int) = act { removeHabit(id) }

    private fun act(action: suspend () -> HabitResult<*>) {
        viewModelScope.launch {
            when (val result = runCatching { action() }.getOrElse { HabitResult.Failure(it.message ?: "Request failed") }) {
                is HabitResult.Success -> refresh()
                is HabitResult.Failure -> _state.update { it.copy(errorMessage = result.message) }
            }
        }
    }
}
