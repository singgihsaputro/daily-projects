package com.dailyprojects.readingtracker

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private const val SAMPLE_JSON = """
[
  {"id": "1", "title": "Dune", "author": "Frank Herbert", "totalPages": 412, "pagesRead": 412, "status": "FINISHED"},
  {"id": "2", "title": "Foundation", "author": "Isaac Asimov", "totalPages": 255, "pagesRead": 60, "status": "READING"}
]
"""

private class FakeJsonSource(private val json: String) : BookJsonSource {
    override fun readJson(): String = json
}

class MockBookRepositoryTest {

    @Test
    fun parsesFixtureIntoDomainBooks() = runTest {
        val repository = MockBookRepository(FakeJsonSource(SAMPLE_JSON))
        val books = repository.loadBooks()

        assertEquals(2, books.size)
        assertEquals("Dune", books[0].title)
        assertEquals(ReadingStatus.FINISHED, books[0].status)
        assertEquals(60, books[1].pagesRead)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun modelExposesLoadedBooksThroughState() = runTest {
        val model = ReadingListModel(
            repository = MockBookRepository(FakeJsonSource(SAMPLE_JSON)),
            scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
        )
        model.load()

        val state = model.state.value
        assertEquals(1, state.booksFinished)
        assertEquals(472, state.pagesReadTotal)
    }
}
