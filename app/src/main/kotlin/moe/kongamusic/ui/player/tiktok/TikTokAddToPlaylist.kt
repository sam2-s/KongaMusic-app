/*
 * kongamusic (2026)
 * © Samk
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.kongamusic.ui.player.tiktok

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import moe.kongamusic.LocalDatabase
import moe.kongamusic.R
import moe.kongamusic.models.MediaMetadata
import moe.kongamusic.ui.menu.AddToPlaylistDialog

@Composable
internal fun TikTokAddToPlaylist(
    song: MediaMetadata,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val database = LocalDatabase.current

    AddToPlaylistDialog(
        isVisible = true,
        onGetSong = {
            database.withTransaction { insert(song) }
            listOf(song.id)
        },
        onDismiss = onDismiss,
        onAddComplete = { _, playlistNames ->
            val message =
                when {
                    playlistNames.size == 1 ->
                        context.getString(R.string.added_to_playlist, playlistNames.first())

                    else -> context.getString(R.string.added_to_n_playlists, playlistNames.size)
                }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        },
    )
}
