/*
 * kongamusic (2026)
 * © Samk
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.kongamusic.ui.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import moe.kongamusic.lyrics.LyricsEntry
import moe.kongamusic.lyrics.LyricsUtils
import moe.kongamusic.playback.PlayerConnection
import moe.kongamusic.ui.player.bitchord.CurrentLyricLine
import moe.kongamusic.ui.player.bitchord.toBitChordLyrics
import androidx.compose.runtime.getValue

@Composable
fun rememberInlineLyricLines(playerConnection: PlayerConnection): List<LyricsEntry> {
    val entity by playerConnection.currentLyrics.collectAsStateWithLifecycle(initialValue = null)
    val text = entity?.lyrics?.trim()?.takeIf { it.isNotBlank() }
    return remember(text) {
        when {
            text == null -> emptyList()
            LyricsUtils.isTtml(text) ->
                LyricsUtils.parseTtml(text, playerConnection.player.duration.takeIf { it > 0 }?.toInt())
            LyricsUtils.isLineSyncedLrc(text) -> LyricsUtils.parseLyrics(text)
            else -> emptyList()
        }
    }
}

@Composable
fun InlineNowPlayingLyric(
    playerConnection: PlayerConnection,
    positionProvider: () -> Long,
    isPlaying: Boolean,
    durationMs: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    lyricsSyncOffsetMs: Long = 0L,
) {
    if (!visible) return
    val entries = rememberInlineLyricLines(playerConnection)
    if (entries.isEmpty()) return
    val lines = remember(entries) { entries.toBitChordLyrics() }
    CurrentLyricLine(
        lines = lines,
        trackKey = lines,
        positionMs = (positionProvider() + lyricsSyncOffsetMs).coerceAtLeast(0L),
        isPlaying = isPlaying,
        durationMs = durationMs,
        onClick = onClick,
        modifier = modifier,
        synced = true,
    )
}
