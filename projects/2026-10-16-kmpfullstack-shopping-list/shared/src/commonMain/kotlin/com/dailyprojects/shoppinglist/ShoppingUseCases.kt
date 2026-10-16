package com.dailyprojects.shoppinglist

sealed class ShopResult<out T> {
    data class Success<T>(val value: T) : ShopResult<T>()
    data class Failure(val message: String) : ShopResult<Nothing>()
}

/** Still-to-buy items first, then newest first. A blank [category] means "all". */
class ListItemsUseCase(private val repository: ShoppingRepository) {
    suspend operator fun invoke(category: String = ""): List<ShoppingItem> {
        val wanted = normalizeCategory(category)
        return repository.list()
            .filter { wanted.isEmpty() || it.category == wanted }
            .sortedWith(compareBy<ShoppingItem> { it.checked }.thenByDescending { it.createdAtEpochSeconds })
    }
}

class AddItemUseCase(private val repository: ShoppingRepository) {
    suspend operator fun invoke(name: String, quantity: Int = 1, category: String = ""): ShopResult<ShoppingItem> {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return ShopResult.Failure("Give the item a name")
        if (cleanName.length > MAX_NAME_LENGTH) {
            return ShopResult.Failure("Name must be $MAX_NAME_LENGTH characters or fewer")
        }
        if (quantity !in 1..MAX_QUANTITY) return ShopResult.Failure("Quantity must be between 1 and $MAX_QUANTITY")
        return ShopResult.Success(repository.add(cleanName, quantity, normalizeCategory(category)))
    }
}

class ToggleCheckedUseCase(private val repository: ShoppingRepository) {
    suspend operator fun invoke(id: Int): ShopResult<ShoppingItem> {
        val updated = repository.toggleChecked(id) ?: return ShopResult.Failure("No item with id $id")
        return ShopResult.Success(updated)
    }
}

class RemoveItemUseCase(private val repository: ShoppingRepository) {
    suspend operator fun invoke(id: Int): ShopResult<Unit> =
        if (repository.remove(id)) ShopResult.Success(Unit) else ShopResult.Failure("No item with id $id")
}

/** Drops everything already in the trolley; answers how many items went. */
class ClearCheckedUseCase(private val repository: ShoppingRepository) {
    suspend operator fun invoke(): Int = repository.clearChecked()
}
