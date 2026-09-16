/*
 * kongamusic (2026)
 * © Samk
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.kongamusic.voicesearch

import android.content.Context

object VoiceSearchControllerLocator {
    @Volatile private var instance: VoiceSearchController? = null

    fun get(context: Context): VoiceSearchController =
        instance ?: synchronized(this) {

            instance ?: DefaultVoiceSearchController().also { instance = it }
        }
}
