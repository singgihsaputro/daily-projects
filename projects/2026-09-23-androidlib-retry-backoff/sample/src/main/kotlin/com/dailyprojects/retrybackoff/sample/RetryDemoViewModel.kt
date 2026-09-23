package com.dailyprojects.retrybackoff.sample

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailyprojects.retrybackoff.BackoffConfig
import com.dailyprojects.retrybackoff.RetryExhaustedException
import com.dailyprojects.retrybackoff.retryWithBackoff
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RunStatus { IDLE, RUNNING, SUCCESS, FAILED }

data class RetryDemoUiState(
    val scenarios: List<ApiScenario> = emptyList(),
    val selected: ApiScenario? = null,
    val status: RunStatus = RunStatus.IDLE,
    val log: List<String> = emptyList(),
    val resultText: String? = null
)

private val demoConfig = BackoffConfig(
    maxAttempts = 4,
    initialDelayMs = 300,
    maxDelayMs = 2_000,
    factor = 2.0,
    jitter = false
)

class RetryDemoViewModel(private val repository: ScenarioRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RetryDemoUiState())
    val uiState: StateFlow<RetryDemoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val scenarios = repository.loadScenarios()
            _uiState.update { it.copy(scenarios = scenarios, selected = scenarios.firstOrNull()) }
        }
    }

    fun select(scenario: ApiScenario) {
        _uiState.update { it.copy(selected = scenario, status = RunStatus.IDLE, log = emptyList(), resultText = null) }
    }

    fun run() {
        val scenario = _uiState.value.selected ?: return
        _uiState.update { it.copy(status = RunStatus.RUNNING, log = emptyList(), resultText = null) }
        val api = MockFlakyApi(scenario)

        viewModelScope.launch {
            try {
                val result = retryWithBackoff(
                    config = demoConfig,
                    onAttemptFailed = { attempt, delayMs, error ->
                        appendLog("Attempt $attempt failed (${error.message}) — retrying in ${delayMs}ms")
                    }
                ) { api.call() }
                _uiState.update { it.copy(status = RunStatus.SUCCESS, resultText = result) }
            } catch (e: RetryExhaustedException) {
                appendLog("Gave up after ${e.attempts} attempt(s)")
                _uiState.update { it.copy(status = RunStatus.FAILED, resultText = e.cause.message) }
            }
        }
    }

    private fun appendLog(line: String) {
        _uiState.update { it.copy(log = it.log + line) }
    }
}
