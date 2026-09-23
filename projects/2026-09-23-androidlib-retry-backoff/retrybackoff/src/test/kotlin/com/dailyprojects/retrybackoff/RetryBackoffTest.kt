package com.dailyprojects.retrybackoff

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class RetryBackoffTest {

    private val noDelayConfig = BackoffConfig(
        maxAttempts = 4,
        initialDelayMs = 0,
        maxDelayMs = 0,
        jitter = false
    )

    @Test
    fun `suspend retry succeeds after transient failures`() = runTest {
        var calls = 0
        val result = retryWithBackoff(noDelayConfig) {
            calls++
            if (calls < 3) error("boom $calls") else "ok"
        }
        assertEquals("ok", result)
        assertEquals(3, calls)
    }

    @Test
    fun `suspend retry gives up after maxAttempts and wraps the last error`() = runTest {
        var calls = 0
        try {
            retryWithBackoff(noDelayConfig) {
                calls++
                error("boom $calls")
            }
            fail("expected RetryExhaustedException")
        } catch (e: RetryExhaustedException) {
            assertEquals(noDelayConfig.maxAttempts, calls)
            assertEquals(noDelayConfig.maxAttempts, e.attempts)
            assertTrue(e.cause.message!!.contains("boom 4"))
        }
    }

    @Test
    fun `shouldRetry false stops retrying immediately`() = runTest {
        var calls = 0
        try {
            retryWithBackoff(noDelayConfig, shouldRetry = { it !is IllegalStateException }) {
                calls++
                throw IllegalStateException("not retryable")
            }
            fail("expected RetryExhaustedException")
        } catch (e: RetryExhaustedException) {
            assertEquals(1, calls)
        }
    }

    @Test
    fun `flow retryWithBackoff replays upstream from the start on each attempt`() = runTest {
        var attempts = 0
        val results = flow {
            attempts++
            emit(1)
            if (attempts < 3) error("flaky $attempts")
            emit(2)
        }.retryWithBackoff(noDelayConfig)

        assertEquals(listOf(1, 1, 1, 2), results.toList())
        assertEquals(3, attempts)
    }

    @Test
    fun `delayForAttempt grows exponentially and respects the cap`() {
        val config = BackoffConfig(initialDelayMs = 100, maxDelayMs = 1000, factor = 2.0, jitter = false)
        assertEquals(100L, config.delayForAttempt(1))
        assertEquals(200L, config.delayForAttempt(2))
        assertEquals(400L, config.delayForAttempt(3))
        assertEquals(800L, config.delayForAttempt(4))
        assertEquals(1000L, config.delayForAttempt(5)) // would be 1600, capped to 1000
    }

    @Test
    fun `invalid config is rejected eagerly`() {
        try {
            BackoffConfig(maxAttempts = 0)
            fail("expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            // expected
        }
    }
}
