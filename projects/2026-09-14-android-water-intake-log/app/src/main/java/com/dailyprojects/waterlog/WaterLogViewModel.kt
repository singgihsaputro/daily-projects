package com.dailyprojects.waterlog

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class WaterLogUiState(
    val goalMl: Int = 0,
    val presets: List<DrinkPreset> = emptyList(),
    val entries: List<WaterEntry> = emptyList(),
) {
    val totalMl: Int get() = entries.sumOf { it.amountMl }
    val progress: Float get() = if (goalMl == 0) 0f else (totalMl.toFloat() / goalMl).coerceIn(0f, 1f)
}

class WaterLogViewModel(private val repository: WaterLogRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(WaterLogUiState())
    val uiState: StateFlow<WaterLogUiState> = _uiState.asStateFlow()

    private var nextId: Int

    init {
        val fixture = repository.loadFixture()
        _uiState.value = WaterLogUiState(
            goalMl = fixture.dailyGoalMl,
            presets = fixture.presets,
            entries = fixture.entries,
        )
        nextId = (fixture.entries.mapNotNull { it.id.toIntOrNull() }.maxOrNull() ?: 0) + 1
    }

    fun addEntry(preset: DrinkPreset) {
        val entry = WaterEntry(
            id = (nextId++).toString(),
            label = preset.label,
            amountMl = preset.amountMl,
            time = currentTimeLabel(),
        )
        _uiState.value = _uiState.value.copy(entries = _uiState.value.entries + entry)
    }

    fun removeEntry(id: String) {
        _uiState.value = _uiState.value.copy(
            entries = _uiState.value.entries.filterNot { it.id == id },
        )
    }

    private fun currentTimeLabel(): String =
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
}
