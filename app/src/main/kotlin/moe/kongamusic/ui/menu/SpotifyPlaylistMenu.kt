/*
 * kongamusic (2026)
 * © Samk
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.kongamusic.ui.menu

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.kongamusic.LocalPlayerConnection
import moe.kongamusic.R
import moe.kongamusic.constants.ListThumbnailSize
import moe.kongamusic.constants.ThumbnailCornerRadius
import moe.kongamusic.spotify.Spotify
import moe.kongamusic.spotify.SpotifyPlaybackResolver
import moe.kongamusic.spotify.SpotifyPlaylistQueue
import moe.kongamusic.spotify.models.SpotifyPlaylist
import moe.kongamusic.ui.component.AppleMusicStyleAccentColor
import moe.kongamusic.ui.component.ItemThumbnail
import moe.kongamusic.ui.component.MenuSurfaceSection
import moe.kongamusic.ui.component.NewAction
import moe.kongamusic.ui.component.NewActionGrid
import moe.kongamusic.ui.component.NewMenuContainer
import moe.kongamusic.ui.component.NewMenuContent
import moe.kongamusic.ui.component.NewMenuItem

@Composable
fun SpotifyPlaylistMenu(
    playlist: SpotifyPlaylist,
    coroutineScope: CoroutineScope,
    onDismiss: () -> Unit,
    onHide: () -> Unit,

    viewModel: moe.kongamusic.spotify.SpotifyLibraryViewModel =
        androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel(),
) {
    val context = LocalContext.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val playlistId = playlist.id
    val playlistName = playlist.name
    val songCount = playlist.tracks?.total ?: 0
    val coverUrl = playlist.images.firstOrNull()?.url
    val accentColor = AppleMusicStyleAccentColor

    val shareIntent = remember(playlistId) {
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://open.spotify.com/playlist/$playlistId")
            putExtra(Intent.EXTRA_SUBJECT, playlistName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    val onPlay: () -> Unit = {
        onDismiss()
        coroutineScope.launch {

            viewModel.ensureAccessToken()
            playerConnection.playQueue(SpotifyPlaylistQueue(playlistId = playlistId, title = playlistName))
        }
    }
    val onShuffle: () -> Unit = {
        onDismiss()
        coroutineScope.launch {
            viewModel.ensureAccessToken()

            val randomStart = kotlin.random.Random.nextInt(50)
            playerConnection.playQueue(
                SpotifyPlaylistQueue(
                    playlistId = playlistId,
                    title = playlistName,
                    startIndex = randomStart,
                ),
            )
        }
    }
    val onShare: () -> Unit = {
        onDismiss()
        val chooser = Intent.createChooser(shareIntent, null).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
    val onPlayNext: () -> Unit = {
        onDismiss()
        coroutineScope.launch {
            viewModel.ensureAccessToken()
            val mediaItems = resolveFirstPageAsMediaItems(playlistId)
            if (mediaItems.isNotEmpty()) {
                playerConnection.playNext(mediaItems)
            }
        }
    }
    val onAddToQueue: () -> Unit = {
        onDismiss()
        coroutineScope.launch {
            viewModel.ensureAccessToken()
            val mediaItems = resolveFirstPageAsMediaItems(playlistId)
            if (mediaItems.isNotEmpty()) {
                playerConnection.addToQueue(mediaItems)
            }
        }
    }
    val onToggleHide: () -> Unit = {
        onDismiss()
        onHide()
    }

    NewMenuContainer(
        content = {
            NewMenuContent(
                headerContent = {
                    MenuSurfaceSection(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            ItemThumbnail(
                                thumbnailUrl = coverUrl,
                                isActive = false,
                                isPlaying = false,
                                shape = RoundedCornerShape(ThumbnailCornerRadius),
                                contentScale = ContentScale.Crop,
                                showPlaceholder = true,
                                modifier = Modifier.size(ListThumbnailSize),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = playlistName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                if (songCount > 0) {
                                    Text(
                                        text = pluralStringResource(R.plurals.n_song, songCount, songCount),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                },
                actionGrid = {
                    NewActionGrid(
                        actions =
                            listOf(
                                NewAction(
                                    icon = { Icon(painter = painterResource(R.drawable.play), contentDescription = null, tint = accentColor) },
                                    text = stringResource(R.string.play),
                                    onClick = onPlay,
                                    contentColor = accentColor,
                                ),
                                NewAction(
                                    icon = { Icon(painter = painterResource(R.drawable.shuffle), contentDescription = null, tint = accentColor) },
                                    text = stringResource(R.string.shuffle),
                                    onClick = onShuffle,
                                    contentColor = accentColor,
                                ),
                                NewAction(
                                    icon = { Icon(painter = painterResource(R.drawable.share), contentDescription = null, tint = accentColor) },
                                    text = stringResource(R.string.share),
                                    onClick = onShare,
                                    contentColor = accentColor,
                                ),
                            ),
                    )
                },
                menuItems = {
                    MenuSurfaceSection {
                        NewMenuItem(
                            leadingContent = { Icon(painter = painterResource(R.drawable.playlist_play), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                            headlineContent = { Text(text = stringResource(R.string.play_next)) },
                            onClick = onPlayNext,
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 0.5.dp,
                        )
                        NewMenuItem(
                            leadingContent = { Icon(painter = painterResource(R.drawable.queue_music), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                            headlineContent = { Text(text = stringResource(R.string.add_to_queue)) },
                            onClick = onAddToQueue,
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 0.5.dp,
                        )
                        NewMenuItem(
                            leadingContent = { Icon(painter = painterResource(R.drawable.visibility_off), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                            headlineContent = { Text(text = stringResource(R.string.hide_playlist)) },
                            onClick = onToggleHide,
                        )
                    }
                },
            )
        },
    )
}

private suspend fun resolveFirstPageAsMediaItems(playlistId: String): List<MediaItem> =
    withContext(Dispatchers.IO) {
        val result = Spotify.playlistTracks(playlistId = playlistId, limit = 50, offset = 0).getOrNull() ?: return@withContext emptyList()
        val tracks = result.items.mapNotNull { it.track?.takeUnless { t -> t.isLocal } }
        if (tracks.isEmpty()) return@withContext emptyList()
        tracks.mapNotNull { track -> SpotifyPlaybackResolver.resolveToMediaItem(track) }
    }
