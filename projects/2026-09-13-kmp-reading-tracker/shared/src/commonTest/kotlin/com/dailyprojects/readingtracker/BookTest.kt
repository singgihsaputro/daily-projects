package com.dailyprojects.readingtracker

import kotlin.test.Test
import kotlin.test.assertEquals

class BookTest {

    private fun book(pagesRead: Int, totalPages: Int = 300, status: ReadingStatus = ReadingStatus.READING) =
        Book(id = "x", title = "t", author = "a", totalPages = totalPages, pagesRead = pagesRead, status = status)

    @Test
    fun progressIsFractionOfPagesRead() {
        assertEquals(0.5f, book(pagesRead = 150).progress)
    }

    @Test
    fun addingPagesBeyondTotalClampsAndMarksFinished() {
        val finished = book(pagesRead = 280).withPagesAdded(100)
        assertEquals(300, finished.pagesRead)
        assertEquals(ReadingStatus.FINISHED, finished.status)
    }

    @Test
    fun addingFirstPagesMovesWantToReadIntoReading() {
        val started = book(pagesRead = 0, status = ReadingStatus.WANT_TO_READ).withPagesAdded(20)
        assertEquals(ReadingStatus.READING, started.status)
    }
}
