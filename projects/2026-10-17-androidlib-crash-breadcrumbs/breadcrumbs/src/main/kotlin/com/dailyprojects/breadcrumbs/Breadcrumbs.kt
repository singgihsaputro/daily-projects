package com.dailyprojects.breadcrumbs

import android.content.Context
import java.io.File

/**
 * One-line entry point:
 *
 *     Breadcrumbs.install(this)                 // in Application.onCreate
 *     Breadcrumbs.log("nav", "opened Cart")
 *     Breadcrumbs.pendingCrashReport()          // on next launch
 */
object Breadcrumbs {
    private const val REPORT_FILE = "last_crash_breadcrumbs.txt"

    @Volatile
    var recorder = BreadcrumbRecorder()
        private set

    @Volatile
    private var reporter: CrashReporter? = null

    @Synchronized
    fun install(context: Context, capacity: Int = 100) {
        if (reporter != null) return
        recorder = BreadcrumbRecorder(capacity)
        reporter = CrashReporter(recorder, File(context.applicationContext.filesDir, REPORT_FILE))
            .also { it.install() }
    }

    fun log(category: String, message: String, level: Level = Level.INFO) =
        recorder.record(category, message, level)

    fun pendingCrashReport(): String? = reporter?.pendingReport()

    fun clearCrashReport() {
        reporter?.clearReport()
    }
}
