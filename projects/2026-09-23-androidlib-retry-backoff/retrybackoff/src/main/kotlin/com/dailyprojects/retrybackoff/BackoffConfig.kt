package com.dailyprojects.retrybackoff

import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

/**
 * Exponential backoff schedule. Delay grows as `initialDelayMs * factor^(attempt-1)`,
 * capped at [maxDelayMs]. With [jitter] on, the actual delay is a random value in
 * `[0, cappedDelay]` (full jitter) so retrying clients don't all wake up in lockstep.
 */
data class BackoffConfig(
    val maxAttempts: Int = 5,
    val initialDelayMs: Long = 500,
    val maxDelayMs: Long = 15_000,
    val factor: Double = 2.0,
    val jitter: Boolean = true
) {
    init {
        require(maxAttempts >= 1) { "maxAttempts must be at least 1, was $maxAttempts" }
        require(initialDelayMs >= 0) { "initialDelayMs must not be negative" }
        require(maxDelayMs >= initialDelayMs) { "maxDelayMs must be >= initialDelayMs" }
        require(factor >= 1.0) { "factor must be >= 1.0, was $factor" }
    }

    /** Delay before the given retry [attempt] (1-based: the delay before the 1st retry). */
    fun delayForAttempt(attempt: Int, random: Random = Random.Default): Long {
        val raw = initialDelayMs * factor.pow(attempt - 1)
        val capped = min(raw, maxDelayMs.toDouble()).toLong()
        return if (jitter) random.nextLong(0, capped + 1) else capped
    }
}

/** Thrown when a retried operation still fails after [attempts] tries. */
class RetryExhaustedException(
    val attempts: Int,
    override val cause: Throwable
) : Exception("Gave up after $attempts attempt(s): ${cause.message}", cause)
