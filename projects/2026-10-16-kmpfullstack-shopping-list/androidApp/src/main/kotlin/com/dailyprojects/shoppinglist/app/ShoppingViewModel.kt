package com.dailyprojects.shoppinglist.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailyprojects.shoppinglist.AddItemUseCase
import com.dailyprojects.shoppinglist.ClearCheckedUseCase
import com.dailyprojects.shoppinglist.ListItemsUseCase
import com.dailyprojects.shoppinglist.RemoveItemUseCase
import com.dailyprojects.shoppinglist.ShopResult
import com.dailyprojects.shoppinglist.ShoppingItem
import com.dailyprojects.shoppinglist.ShoppingRepository
import com.dailyprojects.shoppinglist.ToggleCheckedUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ShoppingUiState(
    val items: List<ShoppingItem> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val nameInput: String = "",
    val quantityInput: String = "1",
    val categoryInput: String = "",
)

class ShoppingViewModel(repository: ShoppingRepository = HttpShoppingRepository()) : ViewModel() {
    private val listItems = ListItemsUseCase(repository)
    private val addItem = AddItemUseCase(repository)
    private val toggleChecked = ToggleCheckedUseCase(repository)
    private val removeItem = RemoveItemUseCase(repository)
    private val clearChecked = ClearCheckedUseCase(repository)

    private val _state = MutableStateFlow(ShoppingUiState())
    val state: StateFlow<ShoppingUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun onNameChange(value: String) = _state.update { it.copy(nameInput = value) }
    fun onQuantityChange(value: String) = _state.update { it.copy(quantityInput = value.filter(Char::isDigit).take(2)) }
    fun onCategoryChange(value: String) = _state.update { it.copy(categoryInput = value) }

    fun selectCategory(category: String) {
        _state.update { it.copy(selectedCategory = category) }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching { listItems(_state.value.selectedCategory) to listItems().map { it.category }.filter { it.isNotEmpty() }.distinct().sorted() }
                .onSuccess { (visible, categories) ->
                    _state.update { it.copy(items = visible, categories = categories, isLoading = false, errorMessage = null) }
                }
                .onFailure { error -> _state.update { it.copy(isLoading = false, errorMessage = error.message) } }
        }
    }

    fun add() {
        val current = _state.value
        viewModelScope.launch {
            val quantity = current.quantityInput.toIntOrNull() ?: 0
            when (val result = addItem(current.nameInput, quantity, current.categoryInput)) {
                is ShopResult.Success -> {
                    _state.update { it.copy(nameInput = "", quantityInput = "1", categoryInput = "", errorMessage = null) }
                    refresh()
                }
                is ShopResult.Failure -> _state.update { it.copy(errorMessage = result.message) }
            }
        }
    }

    fun check(id: Int) = act { toggleChecked(id) }

    fun remove(id: Int) = act { removeItem(id) }

    fun clearTicked() {
        viewModelScope.launch {
            runCatching { clearChecked() }
                .onSuccess { refresh() }
                .onFailure { error -> _state.update { it.copy(errorMessage = error.message) } }
        }
    }

    private fun act(action: suspend () -> ShopResult<*>) {
        viewModelScope.launch {
            when (val result = action()) {
                is ShopResult.Success -> refresh()
                is ShopResult.Failure -> _state.update { it.copy(errorMessage = result.message) }
            }
        }
    }
}
