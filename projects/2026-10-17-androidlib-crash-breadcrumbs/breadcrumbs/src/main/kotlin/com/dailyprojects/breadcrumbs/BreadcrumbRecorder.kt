package com.dailyprojects.breadcrumbs

/**
 * Thread-safe ring buffer of the most recent [capacity] breadcrumbs.
 * Oldest entries are dropped first. Messages longer than [maxMessageLength]
 * are truncated so one huge log line cannot crowd out the trail.
 */
class BreadcrumbRecorder(
    val capacity: Int = 100,
    private val maxMessageLength: Int = 200,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private val lock = Any()
    private val buffer = ArrayDeque<Breadcrumb>(capacity)

    init {
        require(capacity > 0) { "capacity must be positive, was $capacity" }
        require(maxMessageLength > 0) { "maxMessageLength must be positive" }
    }

    fun record(category: String, message: String, level: Level = Level.INFO) {
        val text = if (message.length > maxMessageLength) {
            message.take(maxMessageLength - 1) + "…"
        } else {
            message
        }
        val crumb = Breadcrumb(clock(), category, text, level)
        synchronized(lock) {
            if (buffer.size == capacity) buffer.removeFirst()
            buffer.addLast(crumb)
        }
    }

    /** Oldest-first copy of the current trail. */
    fun snapshot(): List<Breadcrumb> = synchronized(lock) { buffer.toList() }

    fun clear() = synchronized(lock) { buffer.clear() }

    /** The trail as plain text, one line per breadcrumb, oldest first. */
    fun dump(): String = snapshot().joinToString("\n") { it.format() }
}
