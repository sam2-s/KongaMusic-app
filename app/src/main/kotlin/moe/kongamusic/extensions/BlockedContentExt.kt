/*
 * kongamusic (2026)
 * © Samk
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.kongamusic.extensions

import moe.kongamusic.innertube.models.AlbumItem
import moe.kongamusic.innertube.models.ArtistItem
import moe.kongamusic.innertube.models.PlaylistItem
import moe.kongamusic.innertube.models.SongItem
import moe.kongamusic.innertube.models.YTItem
import moe.kongamusic.innertube.pages.BrowseResult

fun <T : YTItem> List<T>.filterBlockedArtists(blockedArtistIds: Set<String>): List<T> {
    if (blockedArtistIds.isEmpty()) return this

    return filter { item ->
        when (item) {
            is ArtistItem -> item.id !in blockedArtistIds
            is SongItem -> item.artists.none { it.id in blockedArtistIds }
            is AlbumItem -> item.artists.orEmpty().none { it.id in blockedArtistIds }
            is PlaylistItem -> item.author?.id !in blockedArtistIds
        }
    }
}

fun <T : YTItem> List<T>.filterBlockedSongs(blockedSongIds: Set<String>): List<T> {
    if (blockedSongIds.isEmpty()) return this
    return filter { item ->
        when (item) {
            is SongItem -> item.id !in blockedSongIds
            else -> true
        }
    }
}

fun BrowseResult.filterBlockedArtists(blockedArtistIds: Set<String>): BrowseResult {
    if (blockedArtistIds.isEmpty()) return this

    return copy(
        items =
            items.mapNotNull { section ->
                section.copy(
                    items =
                        section.items
                            .filterBlockedArtists(blockedArtistIds)
                            .ifEmpty { return@mapNotNull null },
                )
            },
    )
}

fun BrowseResult.filterBlockedSongs(blockedSongIds: Set<String>): BrowseResult {
    if (blockedSongIds.isEmpty()) return this

    return copy(
        items =
            items.mapNotNull { section ->
                section.copy(
                    items =
                        section.items
                            .filterBlockedSongs(blockedSongIds)
                            .ifEmpty { return@mapNotNull null },
                )
            },
    )
}
