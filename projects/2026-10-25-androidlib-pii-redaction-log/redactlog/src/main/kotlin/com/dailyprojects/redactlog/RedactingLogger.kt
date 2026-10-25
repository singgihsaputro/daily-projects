package com.dailyprojects.redactlog

/**
 * Logger that scrubs every message, and the stack trace of any throwable, before
 * it reaches the [sink]. Raw text never leaves this class.
 */
class RedactingLogger(
    private val sink: LogSink = LogcatSink,
    private val redactor: Redactor = Redactor(),
) {
    fun d(tag: String, message: String, t: Throwable? = null) = log(LogLevel.DEBUG, tag, message, t)
    fun i(tag: String, message: String, t: Throwable? = null) = log(LogLevel.INFO, tag, message, t)
    fun w(tag: String, message: String, t: Throwable? = null) = log(LogLevel.WARN, tag, message, t)
    fun e(tag: String, message: String, t: Throwable? = null) = log(LogLevel.ERROR, tag, message, t)

    fun log(level: LogLevel, tag: String, message: String, t: Throwable? = null) {
        val body = if (t == null) message else message + "\n" + t.stackTraceToString().trimEnd()
        sink.write(level, tag, redactor.redact(body))
    }
}
