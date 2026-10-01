/*
 * kongamusic (2026)
 * © Samk
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 *
 * Adapters between KongaMusic's playback model and the BitChord navigation
 * components (github.com/kushagrasinghx/bitchord, GPL-3.0).
 *
 * BitChord's bar components take a `Song` and read `AppSettings.reduceBlur` /
 * `reduceAnimation` directly. Here the same surface is satisfied by
 * [BitChordBarSong] and the two remember helpers below, so the ported files
 * stay otherwise verbatim.
 */

package moe.kongamusic.ui.component.bitchord

import moe.kongamusic.models.MediaMetadata

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import moe.kongamusic.constants.DisableAnimationsKey
import moe.kongamusic.constants.DisableBlurKey
import moe.kongamusic.utils.rememberPreference

/**
 * The slice of a track the bar components actually read. BitChord passes its
 * own `Song`; the call site maps the player's metadata onto this once, rather
 * than the components reaching into PlayerConnection themselves.
 */
data class BitChordBarSong(
    val id: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String?,
)

/**
 * A song title with the catalogue-standard outlined E for explicit audio.
 * BitChord gates that badge on `song.isExplicit`; this model does not carry
 * the flag, so the title renders plain.
 */
@Composable
internal fun ExplicitSongTitle(
    song: BitChordBarSong,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = song.title,
        style = style,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

/** Maps the current player metadata onto [BitChordBarSong], or null if idle. */
fun MediaMetadata?.toBitChordBarSong(): BitChordBarSong? {
    val meta = this ?: return null
    val trackId = meta.id.takeIf { it.isNotBlank() } ?: return null
    return BitChordBarSong(
        id = trackId,
        title = meta.title,
        artist = meta.artists.joinToString(", ") { it.name },
        thumbnailUrl = meta.thumbnailUrl,
    )
}

/**
 * BitChord's `AppSettings.reduceAnimation` → our Disable Animations setting.
 * Returns the state rather than the value because the ported files use `by`
 * delegation, exactly as they do with BitChord's own StateFlow-backed settings.
 */
@Composable
fun rememberBitChordReduceAnimation(): MutableState<Boolean> =
    rememberPreference(DisableAnimationsKey, false)

/** BitChord's `AppSettings.reduceDynamicBlur` → our Disable Blur setting. */
@Composable
fun rememberBitChordReduceBlur(): MutableState<Boolean> =
    rememberPreference(DisableBlurKey, false)

/**
 * BitChord's `Modifier.thumbnailBorder`, which is private in their
 * `FloatingBottomBar.kt`. Same behaviour: a 1dp hairline that flips with the
 * system theme so the sleeve edge reads on both light and dark artwork.
 */
internal fun Modifier.thumbnailBorder(shape: Shape): Modifier = composed {
    this.border(
        width = 1.dp,
        color = if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.15f),
        shape = shape,
    )
}
