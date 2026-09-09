package com.dailyprojects.pomodoro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class Phase { FOCUS, BREAK }

data class PomodoroUiState(
    val presets: List<TimerPreset> = emptyList(),
    val selectedPreset: TimerPreset? = null,
    val phase: Phase = Phase.FOCUS,
    val secondsRemaining: Int = 0,
    val isRunning: Boolean = false,
    val completedFocusSessions: Int = 0,
)

class PomodoroViewModel(private val repository: PresetRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PomodoroUiState())
    val uiState: StateFlow<PomodoroUiState> = _uiState.asStateFlow()

    private var tickJob: Job? = null

    init {
        val presets = repository.loadPresets()
        val first = presets.firstOrNull()
        _uiState.value = PomodoroUiState(
            presets = presets,
            selectedPreset = first,
            secondsRemaining = (first?.focusMinutes ?: 0) * 60,
        )
    }

    fun selectPreset(preset: TimerPreset) {
        stopTicking()
        _uiState.value = _uiState.value.copy(
            selectedPreset = preset,
            phase = Phase.FOCUS,
            secondsRemaining = preset.focusMinutes * 60,
            isRunning = false,
        )
    }

    fun start() {
        if (_uiState.value.isRunning) return
        _uiState.value = _uiState.value.copy(isRunning = true)
        tickJob = viewModelScope.launch {
            while (_uiState.value.isRunning) {
                delay(1000)
                tick()
            }
        }
    }

    fun pause() {
        stopTicking()
        _uiState.value = _uiState.value.copy(isRunning = false)
    }

    fun reset() {
        stopTicking()
        val preset = _uiState.value.selectedPreset ?: return
        _uiState.value = _uiState.value.copy(
            phase = Phase.FOCUS,
            secondsRemaining = preset.focusMinutes * 60,
            isRunning = false,
        )
    }

    private fun tick() {
        val state = _uiState.value
        val preset = state.selectedPreset ?: return

        if (state.secondsRemaining > 1) {
            _uiState.value = state.copy(secondsRemaining = state.secondsRemaining - 1)
            return
        }

        val nextPhase = if (state.phase == Phase.FOCUS) Phase.BREAK else Phase.FOCUS
        val nextSeconds = if (nextPhase == Phase.FOCUS) {
            preset.focusMinutes * 60
        } else {
            preset.breakMinutes * 60
        }
        val completed = state.completedFocusSessions + if (state.phase == Phase.FOCUS) 1 else 0

        _uiState.value = state.copy(
            phase = nextPhase,
            secondsRemaining = nextSeconds,
            completedFocusSessions = completed,
        )
    }

    private fun stopTicking() {
        tickJob?.cancel()
        tickJob = null
    }

    override fun onCleared() {
        stopTicking()
    }
}
