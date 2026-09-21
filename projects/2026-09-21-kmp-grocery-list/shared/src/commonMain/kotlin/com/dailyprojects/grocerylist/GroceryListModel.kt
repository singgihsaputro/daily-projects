package com.dailyprojects.grocerylist

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GroceryListState(
    val items: List<GroceryItem> = emptyList(),
    val isLoading: Boolean = true,
) {
    val grouped: Map<GroceryCategory, List<GroceryItem>> get() = items.groupedByCategory()
    val checkedCount: Int get() = items.count { it.checked }
    val progress: Double get() = if (items.isEmpty()) 0.0 else checkedCount.toDouble() / items.size
}

/** Platform-agnostic state holder: both the Android and iOS UIs drive their screen from the same [state]. */
class GroceryListModel(
    private val repository: GroceryRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob()),
) {
    private val _state = MutableStateFlow(GroceryListState())
    val state: StateFlow<GroceryListState> = _state.asStateFlow()
    private var nextCustomId = 1

    fun load() {
        scope.launch {
            val items = repository.loadItems()
            _state.value = GroceryListState(items = items, isLoading = false)
        }
    }

    fun toggle(id: String) {
        _state.value = _state.value.copy(
            items = _state.value.items.map { if (it.id == id) it.copy(checked = !it.checked) else it },
        )
    }

    fun addItem(name: String, category: GroceryCategory, quantity: Int = 1) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        val item = GroceryItem(id = "custom-${nextCustomId++}", name = trimmed, quantity = quantity, category = category)
        _state.value = _state.value.copy(items = _state.value.items + item)
    }

    fun removeItem(id: String) {
        _state.value = _state.value.copy(items = _state.value.items.filterNot { it.id == id })
    }

    fun clearChecked() {
        _state.value = _state.value.copy(items = _state.value.items.filterNot { it.checked })
    }
}
