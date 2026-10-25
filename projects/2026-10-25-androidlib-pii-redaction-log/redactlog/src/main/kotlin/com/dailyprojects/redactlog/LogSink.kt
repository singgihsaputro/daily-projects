package com.dailyprojects.redactlog

import android.util.Log

enum class LogLevel(val priority: Int) { DEBUG(Log.DEBUG), INFO(Log.INFO), WARN(Log.WARN), ERROR(Log.ERROR) }

/** Where finished (already redacted) lines go. */
fun interface LogSink {
    fun write(level: LogLevel, tag: String, message: String)
}

/** Writes to logcat. */
object LogcatSink : LogSink {
    override fun write(level: LogLevel, tag: String, message: String) {
        Log.println(level.priority, tag, message)
    }
}
