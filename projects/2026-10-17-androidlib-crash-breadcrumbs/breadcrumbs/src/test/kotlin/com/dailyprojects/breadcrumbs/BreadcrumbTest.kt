package com.dailyprojects.breadcrumbs

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BreadcrumbTest {
    private var now = 1_792_229_405_120L // 2026-10-17T09:30:05.120Z
    private fun recorder(capacity: Int = 3, max: Int = 200) =
        BreadcrumbRecorder(capacity, max) { now++ }

    @Test
    fun keepsOnlyNewestWhenFull() {
        val r = recorder()
        (1..5).forEach { r.record("t", "m$it") }
        assertEquals(listOf("m3", "m4", "m5"), r.snapshot().map { it.message })
    }

    @Test
    fun truncatesLongMessages() {
        val r = recorder(max = 10)
        r.record("t", "x".repeat(50))
        assertEquals(10, r.snapshot().single().message.length)
        assertTrue(r.snapshot().single().message.endsWith("…"))
    }

    @Test
    fun formatsLine() {
        val r = recorder()
        r.record("nav", "opened Cart", Level.WARN)
        assertEquals("2026-10-17T09:30:05.120Z WARN  [nav] opened Cart", r.dump())
    }

    @Test
    fun survivesConcurrentWriters() {
        val r = BreadcrumbRecorder(50)
        val threads = (1..8).map { t -> Thread { repeat(500) { r.record("t$t", "m$it") } } }
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        assertEquals(50, r.snapshot().size)
    }

    @Test
    fun reportContainsTraceAndTrail() {
        val r = recorder()
        r.record("pay", "tapped Pay")
        val file = File.createTempFile("crash", ".txt").also { it.delete() }
        val reporter = CrashReporter(r, file)
        val report = reporter.buildReport(Thread.currentThread(), IllegalStateException("boom"))
        assertTrue(report.contains("java.lang.IllegalStateException: boom"))
        assertTrue(report.contains("[pay] tapped Pay"))
    }

    @Test
    fun handlerWritesFileAndChainsToPrevious() {
        val original = Thread.getDefaultUncaughtExceptionHandler()
        var chained: Throwable? = null
        Thread.setDefaultUncaughtExceptionHandler { _, e -> chained = e }
        val r = recorder()
        r.record("cart", "added item")
        val file = File.createTempFile("crash", ".txt").also { it.delete() }
        val reporter = CrashReporter(r, file)
        try {
            reporter.install()
            assertNull(reporter.pendingReport())
            val t = Thread { throw RuntimeException("kaboom") }
            t.start(); t.join()
            assertTrue(reporter.pendingReport()!!.contains("[cart] added item"))
            assertEquals("kaboom", chained?.message)
            reporter.clearReport()
            assertNull(reporter.pendingReport())
        } finally {
            reporter.uninstall()
            Thread.setDefaultUncaughtExceptionHandler(original)
        }
    }
}
