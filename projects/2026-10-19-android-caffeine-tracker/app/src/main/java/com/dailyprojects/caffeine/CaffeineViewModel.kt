package com.dailyprojects.caffeine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CaffeineUiState(
    val limitMg: Int = 0,
    val drinks: List<Drink> = emptyList(),
    val log: List<Entry> = emptyList(),
) {
    val totalMg: Int get() = log.sumOf { it.drink.mg }
    val activeMg: Int get() = Pharmacokinetics.activeMg(log).toInt()
    val sleepSafeInMinutes: Int get() = Pharmacokinetics.minutesUntilSleepSafe(log)
    val overLimit: Boolean get() = limitMg > 0 && totalMg > limitMg
}

class CaffeineViewModel(private val repository: CaffeineRepository) : ViewModel() {
    private val _state = MutableStateFlow(CaffeineUiState())
    val state: StateFlow<CaffeineUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val data = repository.load()
            _state.value = CaffeineUiState(data.dailyLimitMg, data.drinks, data.log)
        }
    }

    fun add(drink: Drink) = _state.update { it.copy(log = listOf(Entry(drink, 0)) + it.log) }

    fun remove(entry: Entry) = _state.update { it.copy(log = it.log - entry) }
}
