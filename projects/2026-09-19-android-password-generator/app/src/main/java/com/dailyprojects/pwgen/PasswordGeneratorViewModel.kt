package com.dailyprojects.pwgen

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PasswordUiState(
    val options: PasswordOptions = PasswordOptions(16, true, true, true, true),
    val password: String = "",
    val strength: String = "",
    val history: List<HistoryEntry> = emptyList(),
    val error: String? = null,
)

private const val UPPERCASE = "ABCDEFGHJKLMNPQRSTUVWXYZ"
private const val LOWERCASE = "abcdefghijkmnpqrstuvwxyz"
private const val NUMBERS = "23456789"
private const val SYMBOLS = "!@#$%^&*-_+=?"
private const val MAX_HISTORY = 8

class PasswordGeneratorViewModel(private val repository: PasswordHistoryRepository) : ViewModel() {

    private val random = SecureRandom()

    private val _uiState = MutableStateFlow(PasswordUiState())
    val uiState: StateFlow<PasswordUiState> = _uiState.asStateFlow()

    private var nextId: Int

    init {
        val fixture = repository.loadFixture()
        _uiState.value = PasswordUiState(options = fixture.defaultOptions, history = fixture.history)
        nextId = (fixture.history.mapNotNull { it.id.toIntOrNull() }.maxOrNull() ?: 0) + 1
        generate()
    }

    fun setLength(length: Int) {
        _uiState.value = _uiState.value.copy(options = _uiState.value.options.copy(length = length))
    }

    fun toggleUppercase(enabled: Boolean) = updateOptions { it.copy(useUppercase = enabled) }
    fun toggleLowercase(enabled: Boolean) = updateOptions { it.copy(useLowercase = enabled) }
    fun toggleNumbers(enabled: Boolean) = updateOptions { it.copy(useNumbers = enabled) }
    fun toggleSymbols(enabled: Boolean) = updateOptions { it.copy(useSymbols = enabled) }

    private fun updateOptions(transform: (PasswordOptions) -> PasswordOptions) {
        _uiState.value = _uiState.value.copy(options = transform(_uiState.value.options))
    }

    fun generate() {
        val options = _uiState.value.options
        val pool = buildString {
            if (options.useUppercase) append(UPPERCASE)
            if (options.useLowercase) append(LOWERCASE)
            if (options.useNumbers) append(NUMBERS)
            if (options.useSymbols) append(SYMBOLS)
        }

        if (pool.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Pick at least one character set.")
            return
        }

        val password = (1..options.length).map { pool[random.nextInt(pool.length)] }.joinToString("")
        val strength = strengthOf(password, pool.length)
        val entry = HistoryEntry(
            id = (nextId++).toString(),
            password = password,
            length = options.length,
            strength = strength,
            time = currentTimeLabel(),
        )

        _uiState.value = _uiState.value.copy(
            password = password,
            strength = strength,
            history = (listOf(entry) + _uiState.value.history).take(MAX_HISTORY),
            error = null,
        )
    }

    private fun strengthOf(password: String, poolSize: Int): String {
        val bitsOfEntropy = password.length * (Math.log(poolSize.toDouble()) / Math.log(2.0))
        return when {
            bitsOfEntropy < 40 -> "Weak"
            bitsOfEntropy < 70 -> "Fair"
            bitsOfEntropy < 100 -> "Strong"
            else -> "Very strong"
        }
    }

    private fun currentTimeLabel(): String =
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
}
