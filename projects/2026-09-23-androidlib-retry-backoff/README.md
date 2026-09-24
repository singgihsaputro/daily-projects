# RetryBackoff

Retry a suspending call or a `Flow` with exponential backoff and full jitter, in
about as little code as the problem deserves.

```kotlin
val user = retryWithBackoff { api.fetchUser(id) }
```

## How to run

```bash
./gradlew :retrybackoff:test        # the library's unit tests
./gradlew :sample:assembleDebug     # the sample app that consumes it
```

Needs JDK 17 and the Android SDK (`ANDROID_HOME` set). compileSdk 35, minSdk 24.

## Depending on it

`com.dailyprojects.retrybackoff:retrybackoff:1.0.0` — publish it to your own
Maven repository, or include the module directly:

```kotlin
// settings.gradle.kts
include(":retrybackoff")

// build.gradle.kts
dependencies {
    implementation(project(":retrybackoff"))
}
```

Its only dependency is `kotlinx-coroutines-core`.

## Using it

**A suspending call.** Returns the first success; throws
`RetryExhaustedException` wrapping the last error if every attempt fails.

```kotlin
val user = retryWithBackoff(
    config = BackoffConfig(maxAttempts = 4, initialDelayMs = 300),
    shouldRetry = { it is IOException },          // 4xx should not be retried
    onAttemptFailed = { attempt, delayMs, error ->
        Log.w(TAG, "attempt $attempt failed (${error.message}), retrying in ${delayMs}ms")
    },
) {
    api.fetchUser(id)
}
```

**A Flow.** Retries the terminal collection; anything `shouldRetry` rejects, and
any failure past `maxAttempts`, is rethrown as-is rather than wrapped.

```kotlin
api.userStream(id)
    .retryWithBackoff(shouldRetry = { it is IOException })
    .collect { render(it) }
```

Note the difference: the `Flow` overload replays the upstream from the start on
each attempt, which is what `retryWhen` does. If your upstream is not safe to
re-run, retry inside it rather than around it.

## The schedule

`delay = initialDelayMs * factor^(attempt - 1)`, capped at `maxDelayMs`.

| Option | Default | |
|---|---|---|
| `maxAttempts` | 5 | total tries, not extra tries |
| `initialDelayMs` | 500 | |
| `maxDelayMs` | 15000 | the cap |
| `factor` | 2.0 | |
| `jitter` | true | |

With `jitter` on, the delay is a uniform random value in `[0, cappedDelay]` —
full jitter, not "the delay plus a bit of noise". Clients that fail together stop
retrying together, which is the point: a fixed schedule turns one outage into a
synchronised stampede when the service comes back.

`BackoffConfig` validates in its `init` block, so a nonsensical schedule fails at
construction rather than on the first retry at 3am.

## Sample app

`sample/` drives the library against fixtures in `mock/scenarios.json` — succeeds
immediately, flaky then recovers, fails permanently — and shows each attempt and
the delay chosen. It is how the library is exercised end to end; a library
nothing consumes is a library nothing tests.

## Tests

`./gradlew :retrybackoff:test` covers the behaviour worth pinning down:

- succeeds after transient failures
- gives up after `maxAttempts` and wraps the last error
- `shouldRetry` returning false stops retrying immediately
- the `Flow` overload replays upstream from the start on each attempt
- delays grow exponentially and respect the cap
- an invalid config is rejected eagerly

## Limitations

- No retry budget or circuit breaker. Every call site retries independently, so a
  wide outage still means every caller spending its full schedule.
- The `Flow` overload restarts the upstream; there is no resume-from-where-it-failed.
- Jitter is full jitter only — no decorrelated or equal-jitter variants.
