package com.dailyprojects.unitconverter

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private const val SAMPLE_JSON = """
[
  {"id": "meter", "label": "Meter", "category": "LENGTH", "factorToBase": 1.0},
  {"id": "kilometer", "label": "Kilometer", "category": "LENGTH", "factorToBase": 1000.0},
  {"id": "gram", "label": "Gram", "category": "WEIGHT", "factorToBase": 1.0}
]
"""

private class FakeJsonSource(private val json: String) : UnitJsonSource {
    override fun readJson(): String = json
}

class MockUnitRepositoryTest {

    @Test
    fun parsesFixtureIntoDomainUnits() = runTest {
        val repository = MockUnitRepository(FakeJsonSource(SAMPLE_JSON))
        val units = repository.loadUnits()

        assertEquals(3, units.size)
        assertEquals(UnitCategory.LENGTH, units[0].category)
        assertEquals(1000.0, units[1].factorToBase)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun modelDefaultsToFirstTwoUnitsInTheStartingCategory() = runTest {
        val model = ConverterModel(
            repository = MockUnitRepository(FakeJsonSource(SAMPLE_JSON)),
            scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
        )
        model.load()

        val state = model.state.value
        assertEquals("meter", state.fromUnitId)
        assertEquals("kilometer", state.toUnitId)
        assertEquals(0.001, state.result)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun switchingToACategoryWithOnlyOneUnitLeavesResultUndefinable() = runTest {
        val model = ConverterModel(
            repository = MockUnitRepository(FakeJsonSource(SAMPLE_JSON)),
            scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
        )
        model.load()
        model.selectCategory(UnitCategory.WEIGHT)

        val state = model.state.value
        assertEquals("gram", state.fromUnitId)
        assertEquals("gram", state.toUnitId)
        assertEquals(1.0, state.result)

        model.setInput("abc")
        assertNull(model.state.value.result)
    }
}
