/*
 * kongamusic (2026)
 * © Samk
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.kongamusic.cast

import android.content.Context

object CastPlaybackRepositoryLocator {
    @Volatile private var repository: CastPlaybackRepository? = null

    fun get(context: Context): CastPlaybackRepository =
        repository ?: synchronized(this) {
            repository ?: DefaultCastPlaybackRepository(context.applicationContext).also { repository = it }
        }
}
