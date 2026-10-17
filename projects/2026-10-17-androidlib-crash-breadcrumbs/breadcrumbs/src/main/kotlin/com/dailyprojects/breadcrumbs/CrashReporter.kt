package com.dailyprojects.breadcrumbs

import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Writes a crash report (stack trace + breadcrumb trail) to [file] when an
 * uncaught exception kills a thread, then hands the crash to whichever
 * handler was installed before — so Crashlytics and friends keep working.
 *
 * On the next launch call [pendingReport] to read it and [clearReport] once
 * it has been shown or uploaded.
 */
class CrashReporter(
    private val recorder: BreadcrumbRecorder,
    private val file: File,
) {
    private var previous: Thread.UncaughtExceptionHandler? = null
    private var installed = false

    @Synchronized
    fun install() {
        if (installed) return
        previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            runCatching { file.writeText(buildReport(thread, error)) }
            previous?.uncaughtException(thread, error)
        }
        installed = true
    }

    /** Restores the previous handler. Mainly useful in tests. */
    @Synchronized
    fun uninstall() {
        if (!installed) return
        Thread.setDefaultUncaughtExceptionHandler(previous)
        installed = false
    }

    fun buildReport(thread: Thread, error: Throwable): String {
        val trace = StringWriter().also { error.printStackTrace(PrintWriter(it)) }
        return buildString {
            appendLine("Crash on thread '${thread.name}'")
            appendLine()
            appendLine(trace.toString().trimEnd())
            appendLine()
            appendLine("Breadcrumbs (oldest first):")
            appendLine(recorder.dump().ifEmpty { "(none recorded)" })
        }
    }

    fun pendingReport(): String? = file.takeIf { it.isFile }?.readText()

    fun clearReport() {
        file.delete()
    }
}
