/*
 * kongamusic (2026)
 * © Samk
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.kongamusic.spotify

import moe.kongamusic.spotify.models.SpotifyAlbum
import moe.kongamusic.spotify.models.SpotifyArtist
import moe.kongamusic.spotify.models.SpotifyPaging
import moe.kongamusic.spotify.models.SpotifyPlaylist
import moe.kongamusic.spotify.models.SpotifySearchResult
import moe.kongamusic.spotify.models.SpotifySimpleArtist
import moe.kongamusic.spotify.models.SpotifyTrack
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SpotifySearchItemTest {
    @Test
    fun flattensAndDeduplicatesAllSpotifySearchTypes() {
        val track = SpotifyTrack(id = "track-1", name = "Song")
        val result =
            SpotifySearchResult(
                tracks = SpotifyPaging(items = listOf(track, track), total = 2),
                albums = SpotifyPaging(items = listOf(SpotifyAlbum(id = "album-1", name = "Album"),), total = 1),
                artists = SpotifyPaging(items = listOf(SpotifyArtist(id = "artist-1", name = "Artist")), total = 1),
                playlists = SpotifyPaging(items = listOf(SpotifyPlaylist(id = "playlist-1", name = "Playlist")), total = 1),
            )

        assertEquals(
            listOf("track:track-1", "album:album-1", "artist:artist-1", "playlist:playlist-1"),
            result.toSearchItems().map(SpotifySearchItem::key),
        )
    }

    @Test
    fun mapperUsesSpotifyArtistAndTitleForIdentification() {
        val track =
            SpotifyTrack(
                id = "track-1",
                name = "Long Way Home",
                artists = listOf(SpotifySimpleArtist(name = "Example Artist")),
                durationMs = 201_000,
            )

        assertTrue(
            SpotifyMapper.matchScore(
                spotifyTitle = track.name,
                spotifyArtist = "Example Artist",
                spotifyDurationMs = track.durationMs,
                candidateTitle = "Long Way Home",
                candidateArtist = "Example Artist",
                candidateDurationSec = 201,
            ) > 0.9,
        )
    }
}
