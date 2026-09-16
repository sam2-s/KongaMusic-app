/*
 * kongamusic (2026)
 * © Samk
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.kongamusic.ui.player.tui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.navigation.NavController
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.kongamusic.db.entities.FormatEntity
import moe.kongamusic.db.entities.containerLabel
import moe.kongamusic.models.MediaMetadata
import moe.kongamusic.playback.PlayerConnection
import moe.kongamusic.ui.component.BottomSheetPageState
import moe.kongamusic.ui.component.BottomSheetState
import moe.kongamusic.ui.component.MenuState
import moe.kongamusic.ui.utils.highRes
import moe.kongamusic.utils.makeTimeString

@Composable
fun TuiPlayerContent(
    mediaMetadata: MediaMetadata,
    isPlaying: Boolean,
    isLoading: Boolean,
    canSkipPrevious: Boolean,
    canSkipNext: Boolean,
    sliderPosition: Long?,
    position: Long,
    duration: Long,
    playerConnection: PlayerConnection,
    navController: NavController,
    state: BottomSheetState,
    menuState: MenuState,
    bottomSheetPageState: BottomSheetPageState,
    currentSongLiked: Boolean,
    currentFormat: FormatEntity?,
    onQueueClick: () -> Unit,
    onLyricsClick: () -> Unit,
    onSliderValueChange: (Long) -> Unit,
    onSliderValueChangeFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val shuffleEnabled by playerConnection.shuffleModeEnabled.collectAsStateWithLifecycle()
    val repeatMode by playerConnection.repeatMode.collectAsStateWithLifecycle()
    val queueWindows by playerConnection.queueWindows.collectAsStateWithLifecycle()
    val currentWindowIndex by playerConnection.currentWindowIndex.collectAsStateWithLifecycle()

    val displayPositionMs = sliderPosition ?: position
    val displayDurationMs = if (duration > 0L) duration else 0L
    val fraction =
        if (displayDurationMs > 0L) {
            (displayPositionMs.toFloat() / displayDurationMs).coerceIn(0f, 1f)
        } else {
            0f
        }

    val artUrl = remember(mediaMetadata.id, mediaMetadata.thumbnailUrl) {
        mediaMetadata.thumbnailUrl?.highRes()
    }
    var asciiCover by remember {
        mutableStateOf(generateAsciiPlaceholder(mediaMetadata.id.hashCode().toLong(), COVER_COLS))
    }

    LaunchedEffect(artUrl, mediaMetadata.id) {
        asciiCover =
            generateAsciiPlaceholder(mediaMetadata.id.hashCode().toLong(), COVER_COLS)
        val bitmap =
            if (artUrl.isNullOrBlank()) {
                null
            } else {
                withContext(Dispatchers.IO) {
                    runCatching {
                        val result =
                            context.imageLoader.execute(
                                ImageRequest
                                    .Builder(context)
                                    .data(artUrl)
                                    .size(300, 300)
                                    .allowHardware(false)
                                    .build(),
                            )
                        if (result is SuccessResult) result.image.toBitmap() else null
                    }.getOrNull()
                }
            }
        val source = bitmap
        asciiCover =
            when {
                source != null ->
                    withContext(Dispatchers.Default) {
                        runCatching { source.toAsciiBitmap(COVER_COLS) }.getOrNull()
                    }
                        ?: generateAsciiPlaceholder(mediaMetadata.id.hashCode().toLong(), COVER_COLS)
                else -> generateAsciiPlaceholder(mediaMetadata.id.hashCode().toLong(), COVER_COLS)
            }
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(TuiBg),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
        ) {
            Spacer(Modifier.height(6.dp))
            PlayerHeader(
                title = mediaMetadata.title.orEmpty(),
                artist = mediaMetadata.artists?.joinToString(", ") { it.name.orEmpty() }.orEmpty(),
                album = mediaMetadata.album?.title.orEmpty(),
                onCollapse = { playerConnection.player.pause() },
                onQueueClick = onQueueClick,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                TuiPanel(
                    modifier = Modifier
                        .fillMaxWidth(0.82f)
                        .aspectRatio(1f),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                    ) {
                        AsciiCover(
                            cover = asciiCover,
                            playing = isPlaying,
                            wave = true,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                CursorTitle(
                    text = mediaMetadata.title.orEmpty(),
                    maxLines = 1,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = buildString {
                        append(mediaMetadata.artists?.joinToString(", ") { it.name.orEmpty() }.orEmpty())
                        mediaMetadata.album?.title
                            ?.takeIf { it.isNotBlank() }
                            ?.let { albumTitle ->
                                append(" · ")
                                append(albumTitle)
                            }
                    },
                    color = TuiDim,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                currentFormat?.let { format ->
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TuiChip(text = format.containerLabel())
                        if (format.bitrate > 0) {
                            TuiChip(text = "${format.bitrate / 1000}k")
                        }
                        if ((format.sampleRate ?: 0) > 0) {
                            TuiChip(text = "${(format.sampleRate ?: 0) / 1000}k")
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                SeekReadout(
                    positionMs = displayPositionMs,
                    durationMs = displayDurationMs,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(6.dp))
                ThinSlider(
                    fraction = fraction,
                    onSeek = { targetFraction ->
                        if (displayDurationMs > 0L) {
                            onSliderValueChange((targetFraction * displayDurationMs).toLong())
                            onSliderValueChangeFinished()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                TransportRow(
                    canSkipPrevious = canSkipPrevious,
                    canSkipNext = canSkipNext,
                    isPlaying = isPlaying,
                    isLoading = isLoading,
                    onPrev = { playerConnection.seekToPrevious() },
                    onPlayPause = {
                        if (playerConnection.player.playbackState == Player.STATE_ENDED) {
                            playerConnection.player.seekToDefaultPosition()
                            playerConnection.player.play()
                        } else {
                            if (isPlaying) {
                                playerConnection.player.pause()
                            } else {
                                playerConnection.player.play()
                            }
                        }
                    },
                    onNext = { playerConnection.seekToNext() },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                StatusRow(
                    shuffleEnabled = shuffleEnabled,
                    repeatMode = repeatMode,
                    liked = currentSongLiked,
                    onToggleShuffle = { playerConnection.player.shuffleModeEnabled = !shuffleEnabled },
                    onCycleRepeat = {
                        playerConnection.player.repeatMode =
                            when (repeatMode) {
                                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                                else -> Player.REPEAT_MODE_OFF
                            }
                    },
                    onToggleLike = { playerConnection.toggleLike() },
                    onLyricsClick = onLyricsClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.height(6.dp))
            QueueFooter(
                queueSize = queueWindows.size,
                currentIndex = currentWindowIndex,
                onQueueClick = onQueueClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun PlayerHeader(
    title: String,
    artist: String,
    album: String,
    onCollapse: () -> Unit,
    onQueueClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier,
    ) {
        TuiKey(
            label = "[ k ]",
            onClick = onCollapse,
            contentDescription = "close player",
        )
        Text(
            text = "NOW PLAYING",
            color = TuiFaint,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            letterSpacing = 2.sp,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TuiKey(label = "QUEUE", onClick = onQueueClick)
        }
    }
}

@Composable
private fun SeekReadout(
    positionMs: Long,
    durationMs: Long,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Text(
            text = ">> ${makeTimeString(positionMs.coerceAtLeast(0L))}",
            color = TuiBright,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
        )
        if (durationMs > 0L) {
            Text(
                text = makeTimeString(durationMs),
                color = TuiFaint,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
            )
        }
    }
}

@Composable
private fun TransportRow(
    canSkipPrevious: Boolean,
    canSkipNext: Boolean,
    isPlaying: Boolean,
    isLoading: Boolean,
    onPrev: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        modifier = modifier,
    ) {
        TuiKey(
            label = "|<<",
            onClick = onPrev,
            bright = true,
            big = true,
            modifier = Modifier.weight(1f),
        )
        TuiKey(
            label = if (isLoading) "···" else if (isPlaying) "||" else ">",
            onClick = onPlayPause,
            bright = isPlaying && !isLoading,
            accent = !isPlaying || isLoading,
            big = true,
            fill = true,
            modifier = Modifier.weight(1.5f),
        )
        TuiKey(
            label = ">>|",
            onClick = onNext,
            bright = true,
            big = true,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatusRow(
    shuffleEnabled: Boolean,
    repeatMode: Int,
    liked: Boolean,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleLike: () -> Unit,
    onLyricsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val repeatLabel =
        when (repeatMode) {
            Player.REPEAT_MODE_OFF -> "off"
            Player.REPEAT_MODE_ALL -> "all"
            else -> "one"
        }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier,
    ) {
        TuiStatus(label = "shuffle", value = if (shuffleEnabled) "on" else "off", on = shuffleEnabled, onClick = onToggleShuffle)
        TuiStatus(label = "repeat", value = repeatLabel, onClick = onCycleRepeat)
        TuiStatus(label = "like", value = if (liked) "on" else "off", on = liked, onClick = onToggleLike)
        TuiStatus(label = "lyrics", value = "~", onClick = onLyricsClick)
    }
}

@Composable
private fun QueueFooter(
    queueSize: Int,
    currentIndex: Int,
    onQueueClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .background(TuiRaised)
                .border(1.dp, TuiLine, RectangleShape)
                .clickable(onClick = onQueueClick)
                .padding(horizontal = 10.dp, vertical = 7.dp),
    ) {
        Text(
            text = if (queueSize > 0) "queue: ${currentIndex + 1}/$queueSize" else "queue: -",
            color = TuiFg,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
        )
    }
}