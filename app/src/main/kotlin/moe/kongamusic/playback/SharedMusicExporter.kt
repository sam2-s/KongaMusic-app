/*
 * kongamusic (2026)
 * © Samk
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.kongamusic.playback

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import dagger.hilt.android.EntryPointAccessors
import moe.kongamusic.di.LyricsHelperEntryPoint
import moe.kongamusic.lyrics.LyricsUtils
import moe.kongamusic.models.MediaMetadata
import java.io.File
import java.net.URL

/**
 * Mirrors completed downloads into shared storage, the way LastWave does.
 *
 * Media3 keeps downloads in its own app-private cache, which nothing else on the
 * device can see: other players, file managers and the user's own music library
 * all ignore it. So once a track finishes downloading we publish a real copy into
 * `Music/KongaMusic/<Artist>/`, tag it with ID3 through the existing [AudioTagger],
 * and drop a `.lrc` sidecar next to it when line-synced lyrics are available.
 *
 * The audio is tagged on a scratch copy first, because jaudiotagger needs a
 * seekable file path and the download cache entry must stay untouched, and only
 * then streamed into MediaStore.
 */
object SharedMusicExporter {
    private const val EXPORT_DIR = "KongaMusic"

    /** Largest cover we are willing to pull in to embed; artwork is not re-encoded. */
    private const val MAX_ARTWORK_BYTES = 4 * 1024 * 1024

    private fun mimeFor(name: String): String =
        when (name.substringAfterLast('.', "").lowercase()) {
            "mp3" -> "audio/mpeg"
            "m4a", "mp4" -> "audio/mp4"
            "aac" -> "audio/aac"
            "ogg", "opus" -> "audio/ogg"
            "flac" -> "audio/flac"
            "wav" -> "audio/wav"
            else -> "audio/*"
        }

    /**
     * Filesystem-safe path segment. Titles and artist names come from remote
     * metadata and user playlists, so separators and control characters are
     * stripped rather than trusted.
     */
    private fun sanitize(segment: String, fallback: String): String {
        val cleaned =
            segment
                .replace(Regex("""[/\\:*?"<>|\u0000-\u001F]"""), "_")
                .replace(Regex("""\s+"""), " ")
                .trim()
                .trim('.')
        return cleaned.ifBlank { fallback }.take(180)
    }

    private fun artistName(song: MediaMetadata): String =
        song.artists.joinToString(", ") { it.name }.ifBlank { "Unknown Artist" }

    private fun relativePath(song: MediaMetadata): String =
        "${Environment.DIRECTORY_MUSIC}/$EXPORT_DIR/${sanitize(artistName(song), "Unknown Artist")}"

    private fun displayName(song: MediaMetadata, extension: String): String =
        "${sanitize(artistName(song), "Unknown Artist")} - ${sanitize(song.title, "Unknown Title")}.$extension"

    /**
     * Publishes [source] as `Music/KongaMusic/<Artist>/<Artist> - <Title>.<ext>`.
     *
     * Returns the MediaStore uri, or null if the copy or tagging failed.
     * Re-exporting the same track replaces the previous entry rather than
     * duplicating it.
     */
    suspend fun export(context: Context, source: File, song: MediaMetadata): Uri? =
        withContext(Dispatchers.IO) {
            if (!source.exists() || source.length() == 0L) return@withContext null

            val extension = source.extension.ifBlank { "mp3" }
            val name = displayName(song, extension)

            // Tag a scratch copy: jaudiotagger needs a real path, and the
            // Media3 download cache file must not be modified in place.
            val stagingDir = File(context.cacheDir, "shared_music_export").apply { mkdirs() }
            val staged = File(stagingDir, "${song.id.hashCode()}_$name")
            runCatching {
                source.inputStream().use { input ->
                    staged.outputStream().use { output -> input.copyTo(output) }
                }
                AudioTagger.tag(staged, metadataFor(song, staged.name))
            }.getOrElse { staged.delete(); return@withContext null }

            val resolver = context.contentResolver
            val collection =
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val uri =
                resolver.insert(
                    collection,
                    ContentValues().apply {
                        put(MediaStore.Audio.Media.DISPLAY_NAME, name)
                        put(MediaStore.Audio.Media.MIME_TYPE, mimeFor(name))
                        put(MediaStore.Audio.Media.RELATIVE_PATH, relativePath(song))
                        put(MediaStore.Audio.Media.IS_MUSIC, 1)
                        put(MediaStore.Audio.Media.IS_PENDING, 1)
                    },
                ) ?: return@withContext null.also { staged.delete() }

            val copied =
                runCatching {
                        resolver.openOutputStream(uri)?.use { output ->
                            staged.inputStream().use { input -> input.copyTo(output) }
                        } ?: error("openOutputStream returned null")
                    }
                    .isSuccess
            if (!copied) {
                resolver.delete(uri, null, null)
                staged.delete()
                return@withContext null
            }

            resolver.update(
                uri,
                ContentValues().apply { put(MediaStore.Audio.Media.IS_PENDING, 0) },
                null,
                null,
            )
            staged.delete()
            writeLyricsSidecar(context, song, name.substringBeforeLast('.'))
            uri
        }

    private fun metadataFor(song: MediaMetadata, fallbackAlbum: String): AudioTagger.Metadata =
        AudioTagger.Metadata(
            title = song.title,
            artist = artistName(song),
            albumArtist = song.artists.firstOrNull()?.name,
            album = song.album?.title?.takeIf { it.isNotBlank() } ?: fallbackAlbum,
            isrc = song.isrc,
            artworkBytes = song.thumbnailUrl?.let { fetchArtwork(it) },
            artworkMimeType = "image/jpeg",
        )

    /**
     * Best-effort cover fetch for embedding. Failure just means untagged artwork,
     * so every error is swallowed.
     */
    private fun fetchArtwork(url: String): ByteArray? =
        runCatching {
            URL(url).openStream().use { it.readBytes() }.takeIf { it.size <= MAX_ARTWORK_BYTES }
        }
            .getOrNull()

    /**
     * Writes `<Artist> - <Title>.lrc` beside the audio when the providers return
     * line-synced lyrics. A missing or unsynced sidecar must never fail the
     * export, so this is entirely best-effort.
     */
    private suspend fun writeLyricsSidecar(context: Context, song: MediaMetadata, baseName: String) {
        val helper =
            EntryPointAccessors
                .fromApplication(
                    context.applicationContext,
                    LyricsHelperEntryPoint::class.java,
                )
                .lyricsHelper()
        val body =
            runCatching {
                val lyrics = helper.getLyrics(mediaMetadata = song)
                lyrics.takeIf { it.isNotBlank() && LyricsUtils.isLineSyncedLrc(it) }
            }
                .getOrNull() ?: return

        runCatching {
            val resolver = context.contentResolver
            val uri =
                resolver.insert(
                    MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                    ContentValues().apply {
                        put(MediaStore.Files.FileColumns.DISPLAY_NAME, "$baseName.lrc")
                        put(MediaStore.Files.FileColumns.MIME_TYPE, "text/plain")
                        put(MediaStore.Files.FileColumns.RELATIVE_PATH, relativePath(song))
                        put(MediaStore.Files.FileColumns.IS_PENDING, 1)
                    },
                ) ?: return@runCatching
            resolver.openOutputStream(uri)?.use { it.write(body.toByteArray()) }
            resolver.update(
                uri,
                ContentValues().apply { put(MediaStore.Files.FileColumns.IS_PENDING, 0) },
                null,
                null,
            )
        }
    }
}