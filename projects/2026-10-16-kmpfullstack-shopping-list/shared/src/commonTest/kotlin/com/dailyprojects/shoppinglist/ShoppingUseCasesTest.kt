package com.dailyprojects.shoppinglist

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

private class FakeShoppingRepository(seed: List<ShoppingItem> = emptyList()) : ShoppingRepository {
    private val items = seed.toMutableList()
    override suspend fun list(): List<ShoppingItem> = items.toList()
    override suspend fun add(name: String, quantity: Int, category: String): ShoppingItem {
        val item = ShoppingItem(items.size + 1, name, quantity, category, createdAtEpochSeconds = items.size.toLong())
        items += item
        return item
    }
    override suspend fun toggleChecked(id: Int): ShoppingItem? {
        val index = items.indexOfFirst { it.id == id }
        if (index == -1) return null
        items[index] = items[index].copy(checked = !items[index].checked)
        return items[index]
    }
    override suspend fun remove(id: Int): Boolean = items.removeAll { it.id == id }
    override suspend fun clearChecked(): Int {
        val before = items.size
        items.removeAll { it.checked }
        return before - items.size
    }
}

class ShoppingUseCasesTest {
    @Test
    fun addRejectsBlankNameAndBadQuantity() = runTest {
        val add = AddItemUseCase(FakeShoppingRepository())
        assertIs<ShopResult.Failure>(add("  ", 1))
        assertIs<ShopResult.Failure>(add("Milk", 0))
        assertIs<ShopResult.Failure>(add("Milk", MAX_QUANTITY + 1))
        assertIs<ShopResult.Failure>(add("x".repeat(MAX_NAME_LENGTH + 1), 1))
    }

    @Test
    fun addTrimsNameAndNormalizesCategory() = runTest {
        val result = AddItemUseCase(FakeShoppingRepository())("  Milk ", 2, " Dairy Aisle ")
        val item = assertIs<ShopResult.Success<ShoppingItem>>(result).value
        assertEquals("Milk", item.name)
        assertEquals("dairy-aisle", item.category)
    }

    @Test
    fun listPutsUncheckedFirstThenNewest() = runTest {
        val repo = FakeShoppingRepository(
            listOf(
                ShoppingItem(1, "old", createdAtEpochSeconds = 1),
                ShoppingItem(2, "bought", checked = true, createdAtEpochSeconds = 9),
                ShoppingItem(3, "new", createdAtEpochSeconds = 5),
            ),
        )
        assertEquals(listOf("new", "old", "bought"), ListItemsUseCase(repo)().map { it.name })
    }

    @Test
    fun listFiltersByCategoryCaseInsensitively() = runTest {
        val repo = FakeShoppingRepository(
            listOf(
                ShoppingItem(1, "apples", category = "produce", createdAtEpochSeconds = 1),
                ShoppingItem(2, "soap", category = "household", createdAtEpochSeconds = 2),
            ),
        )
        assertEquals(listOf("apples"), ListItemsUseCase(repo)("Produce").map { it.name })
    }

    @Test
    fun toggleAndRemoveReportMissingIds() = runTest {
        val repo = FakeShoppingRepository(listOf(ShoppingItem(1, "milk", createdAtEpochSeconds = 1)))
        val toggled = assertIs<ShopResult.Success<ShoppingItem>>(ToggleCheckedUseCase(repo)(1))
        assertEquals(true, toggled.value.checked)
        assertIs<ShopResult.Failure>(ToggleCheckedUseCase(repo)(42))
        assertIs<ShopResult.Success<Unit>>(RemoveItemUseCase(repo)(1))
        assertIs<ShopResult.Failure>(RemoveItemUseCase(repo)(1))
    }

    @Test
    fun clearCheckedCountsRemovedItems() = runTest {
        val repo = FakeShoppingRepository(
            listOf(
                ShoppingItem(1, "a", checked = true, createdAtEpochSeconds = 1),
                ShoppingItem(2, "b", createdAtEpochSeconds = 2),
                ShoppingItem(3, "c", checked = true, createdAtEpochSeconds = 3),
            ),
        )
        assertEquals(2, ClearCheckedUseCase(repo)())
        assertEquals(listOf("b"), repo.list().map { it.name })
    }
}
