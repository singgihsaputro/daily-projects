package com.dailyprojects.countdown

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.LocalDateTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CountdownUiState(
    val now: LocalDateTime = LocalDateTime.now(),
    val events: List<CountdownEvent> = emptyList(),
    val loading: Boolean = true,
) {
    val upcoming get() = events.filter { it.date >= now }.sortedBy { it.date }
    val passed get() = events.filter { it.date < now }.sortedByDescending { it.date }
}

class CountdownViewModel(private val repository: EventRepository) : ViewModel() {
    private val _state = MutableStateFlow(CountdownUiState())
    val state: StateFlow<CountdownUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val events = repository.events()
            _state.update { it.copy(events = events, loading = false) }
        }
        viewModelScope.launch {
            while (true) {
                delay(1000)
                _state.update { it.copy(now = LocalDateTime.now()) }
            }
        }
    }
}
