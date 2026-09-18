package com.dailyprojects.unitconverter

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ConverterState(
    val units: List<UnitDefinition> = emptyList(),
    val category: UnitCategory = UnitCategory.LENGTH,
    val fromUnitId: String = "",
    val toUnitId: String = "",
    val inputText: String = "1",
    val isLoading: Boolean = true,
) {
    val unitsInCategory: List<UnitDefinition> get() = units.filter { it.category == category }
    val fromUnit: UnitDefinition? get() = unitsInCategory.find { it.id == fromUnitId }
    val toUnit: UnitDefinition? get() = unitsInCategory.find { it.id == toUnitId }

    val result: Double?
        get() {
            val value = inputText.toDoubleOrNull() ?: return null
            val from = fromUnit ?: return null
            val to = toUnit ?: return null
            return UnitConverter.convert(value, from, to)
        }
}

/** Platform-agnostic state holder: both the Android and iOS UIs drive their screen from the same [state]. */
class ConverterModel(
    private val repository: UnitRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob()),
) {
    private val _state = MutableStateFlow(ConverterState())
    val state: StateFlow<ConverterState> = _state.asStateFlow()

    fun load() {
        scope.launch {
            val units = repository.loadUnits()
            _state.value = defaultStateFor(units, UnitCategory.LENGTH)
        }
    }

    fun selectCategory(category: UnitCategory) {
        _state.value = defaultStateFor(_state.value.units, category, inputText = _state.value.inputText)
    }

    fun selectFromUnit(id: String) {
        _state.value = _state.value.copy(fromUnitId = id)
    }

    fun selectToUnit(id: String) {
        _state.value = _state.value.copy(toUnitId = id)
    }

    fun setInput(text: String) {
        _state.value = _state.value.copy(inputText = text)
    }

    fun swap() {
        val s = _state.value
        _state.value = s.copy(fromUnitId = s.toUnitId, toUnitId = s.fromUnitId)
    }

    private fun defaultStateFor(units: List<UnitDefinition>, category: UnitCategory, inputText: String = "1"): ConverterState {
        val unitsInCategory = units.filter { it.category == category }
        return ConverterState(
            units = units,
            category = category,
            fromUnitId = unitsInCategory.getOrNull(0)?.id ?: "",
            toUnitId = unitsInCategory.getOrNull(1)?.id ?: unitsInCategory.getOrNull(0)?.id ?: "",
            inputText = inputText,
            isLoading = false,
        )
    }
}
