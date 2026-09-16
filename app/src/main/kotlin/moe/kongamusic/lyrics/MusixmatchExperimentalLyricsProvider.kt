/*
 * kongamusic (2026)
 * © Samk
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.kongamusic.lyrics

import android.content.Context
import android.util.Log
import moe.kongamusic.constants.EnableMusixmatchExperimentalKey
import moe.kongamusic.musixmatch.Musixmatch
import moe.kongamusic.utils.GlobalLog
import moe.kongamusic.utils.dataStore
import moe.kongamusic.utils.get

object MusixmatchExperimentalLyricsProvider : LyricsProvider {

    init {
        Musixmatch.logger = { message ->
            GlobalLog.append(Log.INFO, "Musixmatch", message)
        }
    }

    override val name = "Musixmatch (experimental)"

    override fun isEnabled(context: Context): Boolean =
        context.dataStore[EnableMusixmatchExperimentalKey] ?: false

    override suspend fun getLyrics(
        id: String,
        title: String,
        artist: String,
        album: String?,
        duration: Int,
    ): Result<String> =
        Musixmatch.getLyrics(
            title = title,
            artist = artist,
            album = album,
            duration = duration,
        )

    override suspend fun getAllLyrics(
        id: String,
        title: String,
        artist: String,
        album: String?,
        duration: Int,
        callback: (String) -> Unit,
    ) {
        Musixmatch.getAllLyrics(
            title = title,
            artist = artist,
            album = album,
            duration = duration,
            callback = callback,
        )
    }
}
