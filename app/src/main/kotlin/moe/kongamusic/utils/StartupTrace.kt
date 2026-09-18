/*
 * kongamusic (2026)
 * © Samk
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.kongamusic.utils

private val startupTraceIds = java.util.concurrent.atomic.AtomicInteger()

internal inline fun <T> traceStartup(name: String, block: () -> T): T {
    android.os.Trace.beginSection(name)
    return try {
        block()
    } finally {
        android.os.Trace.endSection()
    }
}

internal suspend fun <T> traceStartupAsync(name: String, block: suspend () -> T): T {
    val cookie = startupTraceIds.incrementAndGet()
    val supportsAsyncTrace = android.os.Build.VERSION.SDK_INT >= 29
    if (supportsAsyncTrace) android.os.Trace.beginAsyncSection(name, cookie)
    return try {
        block()
    } finally {
        if (supportsAsyncTrace) android.os.Trace.endAsyncSection(name, cookie)
    }
}