package com.dailyprojects.grocerylist

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val SAMPLE_JSON = """
[
  {"id": "apple", "name": "Apples", "quantity": 6, "category": "PRODUCE"},
  {"id": "milk", "name": "Milk", "quantity": 1, "category": "DAIRY", "checked": true}
]
"""

private class FakeJsonSource(private val json: String) : GroceryJsonSource {
    override fun readJson(): String = json
}

class MockGroceryRepositoryTest {

    @Test
    fun parsesFixtureIntoDomainItems() = runTest {
        val repository = MockGroceryRepository(FakeJsonSource(SAMPLE_JSON))
        val items = repository.loadItems()

        assertEquals(2, items.size)
        assertEquals(GroceryCategory.PRODUCE, items[0].category)
        assertTrue(items[1].checked)
        assertFalse(items[0].checked)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun modelTracksProgressAsItemsAreCheckedAndAdded() = runTest {
        val model = GroceryListModel(
            repository = MockGroceryRepository(FakeJsonSource(SAMPLE_JSON)),
            scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
        )
        model.load()

        assertEquals(0.5, model.state.value.progress)

        model.toggle("apple")
        assertEquals(1.0, model.state.value.progress)

        model.addItem("Eggs", GroceryCategory.DAIRY, quantity = 12)
        assertEquals(3, model.state.value.items.size)
        assertEquals("custom-1", model.state.value.items.last().id)

        model.removeItem("milk")
        assertEquals(2, model.state.value.items.size)

        model.clearChecked()
        assertEquals(1, model.state.value.items.size)
        assertEquals("Eggs", model.state.value.items.single().name)
    }
}
