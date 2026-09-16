/*
 * YumaPlayer (2026) | Modified work by MuwMx
 * kongamusic (2026) | © Samk
 * GPL-3.0 License | Contributors: see git history
 */

package moe.kongamusic.spotify

import androidx.compose.runtime.Immutable
import moe.kongamusic.spotify.models.SpotifyHomeFeedItem
import moe.kongamusic.spotify.models.SpotifyTrack

@Immutable
sealed interface SpotifyHomeSection {
    val title: String

    @Immutable
    data class Tracks(
        override val title: String,
        val tracks: List<SpotifyTrack>,
    ) : SpotifyHomeSection

    @Immutable
    data class Cards(
        override val title: String,
        val items: List<SpotifyHomeFeedItem>,
    ) : SpotifyHomeSection
}
