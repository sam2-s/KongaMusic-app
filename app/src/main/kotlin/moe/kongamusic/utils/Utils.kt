/*
 * kongamusic (2026)
 * © Samk
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.kongamusic.utils

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import kotlinx.coroutines.CancellationException
import java.util.Locale

fun reportException(throwable: Throwable) {

    if (throwable is CancellationException) return

    Log.w("kongamusic", "reportException", throwable)
}

@Suppress("DEPRECATION")
fun setAppLocale(
    context: Context,
    locale: Locale,
) {
    val config = Configuration(context.resources.configuration)
    config.setLocale(locale)
    context.resources.updateConfiguration(config, context.resources.displayMetrics)
}
