package com.dailyprojects.retrybackoff

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.retryWhen

/**
 * Retries the failing terminal collection of this [Flow] with exponential backoff.
 *
 * Only exceptions for which [shouldRetry] returns `true` are retried; anything else
 * (and any failure once [BackoffConfig.maxAttempts] is exhausted) is rethrown as-is.
 * [onAttemptFailed] fires before each delay, so a caller can surface retry progress.
 */
fun <T> Flow<T>.retryWithBackoff(
    config: BackoffConfig = BackoffConfig(),
    shouldRetry: (Throwable) -> Boolean = { true },
    onAttemptFailed: (attempt: Int, delayMs: Long, error: Throwable) -> Unit = { _, _, _ -> }
): Flow<T> = retryWhen { cause, attempt ->
    // `attempt` is 0-based and counts prior failures; attempt 0 is the first failure.
    val attemptNumber = attempt.toInt() + 1
    if (attemptNumber >= config.maxAttempts || !shouldRetry(cause)) {
        false
    } else {
        val delayMs = config.delayForAttempt(attemptNumber)
        onAttemptFailed(attemptNumber, delayMs, cause)
        delay(delayMs)
        true
    }
}

/**
 * Runs [block] with exponential backoff, returning its result on the first success.
 * Throws [RetryExhaustedException] wrapping the last error if every attempt fails.
 */
suspend fun <T> retryWithBackoff(
    config: BackoffConfig = BackoffConfig(),
    shouldRetry: (Throwable) -> Boolean = { true },
    onAttemptFailed: (attempt: Int, delayMs: Long, error: Throwable) -> Unit = { _, _, _ -> },
    block: suspend () -> T
): T {
    var lastError: Throwable? = null
    for (attempt in 1..config.maxAttempts) {
        try {
            return block()
        } catch (error: Throwable) {
            lastError = error
            val isLastAttempt = attempt == config.maxAttempts
            if (isLastAttempt || !shouldRetry(error)) {
                throw RetryExhaustedException(attempt, error)
            }
            val delayMs = config.delayForAttempt(attempt)
            onAttemptFailed(attempt, delayMs, error)
            delay(delayMs)
        }
    }
    // Unreachable: the loop above always returns or throws, but the compiler
    // can't prove maxAttempts >= 1 makes this branch dead.
    throw RetryExhaustedException(config.maxAttempts, lastError ?: IllegalStateException())
}
