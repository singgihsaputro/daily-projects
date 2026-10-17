package com.dailyprojects.breadcrumbs

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class Level { DEBUG, INFO, WARN, ERROR }

/** One thing that happened in the app, e.g. "nav: opened Cart". */
data class Breadcrumb(
    val timestampMs: Long,
    val category: String,
    val message: String,
    val level: Level = Level.INFO,
) {
    /** `2026-10-17T09:30:05.120Z INFO  [nav] opened Cart` */
    fun format(): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        fmt.timeZone = TimeZone.getTimeZone("UTC")
        return "${fmt.format(Date(timestampMs))} ${level.name.padEnd(5)} [$category] $message"
    }
}
