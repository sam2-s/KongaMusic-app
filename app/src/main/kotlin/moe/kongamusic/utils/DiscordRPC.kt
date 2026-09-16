/*
 * kongamusic (2026)
 * © Samk
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.kongamusic.utils

import android.content.Context
import me.bush.translator.Language
import me.bush.translator.Translator
import moe.kongamusic.BuildConfig
import moe.kongamusic.R
import moe.kongamusic.constants.DiscordActivityButton1CustomUrlKey
import moe.kongamusic.constants.DiscordActivityButton1EnabledKey
import moe.kongamusic.constants.DiscordActivityButton1LabelKey
import moe.kongamusic.constants.DiscordActivityButton1UrlSourceKey
import moe.kongamusic.constants.DiscordActivityButton2CustomUrlKey
import moe.kongamusic.constants.DiscordActivityButton2EnabledKey
import moe.kongamusic.constants.DiscordActivityButton2LabelKey
import moe.kongamusic.constants.DiscordActivityButton2UrlSourceKey
import moe.kongamusic.constants.DiscordActivityDetailsKey
import moe.kongamusic.constants.DiscordActivityNameKey
import moe.kongamusic.constants.DiscordActivityPlatformKey
import moe.kongamusic.constants.DiscordActivityStateKey
import moe.kongamusic.constants.DiscordActivityTypeKey
import moe.kongamusic.constants.DiscordLargeImageCustomUrlKey
import moe.kongamusic.constants.DiscordLargeImageTypeKey
import moe.kongamusic.constants.DiscordLargeTextCustomKey
import moe.kongamusic.constants.DiscordLargeTextSourceKey
import moe.kongamusic.constants.DiscordPresenceStatusKey
import moe.kongamusic.constants.DiscordSmallImageCustomUrlKey
import moe.kongamusic.constants.DiscordSmallImageTypeKey
import moe.kongamusic.constants.EnableTranslatorKey
import moe.kongamusic.constants.TranslatorContextsKey
import moe.kongamusic.constants.TranslatorTargetLangKey
import moe.kongamusic.db.entities.Song
import moe.kongamusic.discord.DiscordActivityPlatform
import moe.kongamusic.telegram.isTelegramMediaId
import moe.kongamusic.discord.DiscordActivityType
import moe.kongamusic.discord.DiscordOnlineStatus
import moe.kongamusic.discord.DiscordPresenceActivity
import moe.kongamusic.discord.DiscordPresenceAssets
import moe.kongamusic.discord.DiscordPresenceButton
import moe.kongamusic.discord.DiscordPresenceTimestamps
import moe.kongamusic.discord.DiscordSocialPresenceClient
import moe.kongamusic.discord.DiscordStatusDisplayType
import timber.log.Timber

class DiscordRPC(
    private val context: Context,
    private val accessToken: String,
) {
    companion object {
        private const val PAUSE_IMAGE_URL =
            "https://raw.githubusercontent.com/4nx3b/ArchiveTune/main/fastlane/metadata/android/en-US/images/RPC/pause_icon.png"
        private const val APP_ICON_URL =
            "https://raw.githubusercontent.com/4nx3b/ArchiveTune/main/fastlane/metadata/android/en-US/images/icon.png"
        private const val TAG = "DiscordRPC"

        private const val TRANSLATION_COOLDOWN_MS = 5 * 60 * 1000L
    }

    private val translationCache: MutableMap<String, String> = mutableMapOf()
    private var lastSongId: String? = null

    @Volatile
    private var translationCooldownUntilMs = 0L

    fun isRpcRunning(): Boolean = DiscordSocialPresenceClient.isStarted

    suspend fun stopActivity() {
        DiscordSocialPresenceClient.clearPresence(accessToken).getOrElse {
            Timber.tag(TAG).v(it, "clearPresence failed")
        }
    }

    suspend fun closeRPC() {
        DiscordSocialPresenceClient.close().getOrElse {
            Timber.tag(TAG).v(it, "close failed")
        }
    }

    suspend fun updateSong(
        song: Song,
        currentPlaybackTimeMillis: Long,
        isPaused: Boolean = false,
    ) = runCatching {
        if (lastSongId != song.song.id) {
            translationCache.clear()
            DiscordImageResolver.clearCache()
            lastSongId = song.song.id
        }

        val translatedMap = translateSongFields(song)
        val appName = context.getString(R.string.app_name)
        val namePref = context.dataStore[DiscordActivityNameKey] ?: "APP"
        val detailsPref = context.dataStore[DiscordActivityDetailsKey] ?: "SONG"
        val statePref = context.dataStore[DiscordActivityStateKey] ?: "ARTIST"

        val activityName =
            sourceValue(
                pref = namePref,
                song = song,
                translatedMap = translatedMap,
                default = appName,
            )

        val activityDetails =
            sourceValue(
                pref = detailsPref,
                song = song,
                translatedMap = translatedMap,
                default = song.song.title.ifBlank { appName },
            ).toDiscordText(maxLength = 128, fallback = song.song.title.ifBlank { appName })

        val activityState =
            sourceValue(
                pref = statePref,
                song = song,
                translatedMap = translatedMap,
                default = song.artists.joinToString { it.name }.ifBlank { appName },
            ).toDiscordText(maxLength = 128, fallback = appName)

        val baseSongUrl = song.youtubeMusicUrl()
        val resolvedImages = DiscordImageResolver.resolveImagesForSong(context, song)
        val largeImageType = context.dataStore[DiscordLargeImageTypeKey] ?: "thumbnail"
        val largeImageCustomUrl = context.dataStore[DiscordLargeImageCustomUrlKey] ?: ""
        val smallImageType = context.dataStore[DiscordSmallImageTypeKey] ?: "artist"
        val smallImageCustomUrl = context.dataStore[DiscordSmallImageCustomUrlKey] ?: ""

        val largeImage =
            when (largeImageType.lowercase()) {
                "appicon" -> {
                    APP_ICON_URL
                }

                else -> {
                    DiscordImageResolver.buildImageUrl(
                        imageType = largeImageType,
                        customUrl = largeImageCustomUrl,
                        resolvedImages = resolvedImages,
                        song = song,
                    )
                }
            }

        val smallImage =
            when {
                isPaused -> {
                    PAUSE_IMAGE_URL
                }

                smallImageType.equals("appicon", ignoreCase = true) -> {
                    APP_ICON_URL
                }

                else -> {
                    DiscordImageResolver.buildImageUrl(
                        imageType = smallImageType,
                        customUrl = smallImageCustomUrl,
                        resolvedImages = resolvedImages,
                        song = song,
                    )
                }
            }

        val largeText = resolveLargeText(song, translatedMap)
        val smallText =
            if (isPaused) {
                context.getString(R.string.discord_paused)
            } else {
                resolveSmallText(song, translatedMap, smallImageType, appName)
            }

        val buttons = resolveButtons(song)
        val activityType =
            DiscordActivityType.fromPreference(
                context.dataStore[DiscordActivityTypeKey] ?: "LISTENING",
            )
        val platform =
            DiscordActivityPlatform.fromPreference(
                context.dataStore[DiscordActivityPlatformKey] ?: "android",
            )
        val status =
            DiscordOnlineStatus.fromPreference(
                context.dataStore[DiscordPresenceStatusKey] ?: "online",
            )

        val timestamps =
            buildTimestamps(
                song = song,
                currentPlaybackTimeMillis = currentPlaybackTimeMillis,
                isPaused = isPaused,
            )

        val activity =
            DiscordPresenceActivity(
                applicationId = BuildConfig.DISCORD_APPLICATION_ID_LONG,
                name = activityName.toDiscordText(maxLength = 128, fallback = appName),
                type = activityType,
                details = activityDetails,
                state = activityState,
                detailsUrl = baseSongUrl.toDiscordUrl(),
                assets =
                    DiscordPresenceAssets(
                        largeImage = largeImage.toDiscordUrl(),
                        largeText = largeText.toDiscordText(maxLength = 128),
                        largeUrl = largeImage.toDiscordUrl(),
                        smallImage = smallImage.toDiscordUrl(),
                        smallText = smallText.toDiscordText(maxLength = 128),
                        smallUrl = baseSongUrl.toDiscordUrl(),
                    ),
                buttons = buttons,
                timestamps = timestamps,
                statusDisplayType = DiscordStatusDisplayType.State,
                supportedPlatforms = platform,
                onlineStatus = status,
            )

        DiscordSocialPresenceClient
            .updatePresence(
                accessToken = accessToken,
                activity = activity,
            ).getOrThrow()

        Timber.tag(TAG).i(
            "Updated Discord presence via Gateway name=%s details=%s state=%s",
            activityName,
            activityDetails,
            activityState,
        )
    }

    suspend fun refreshActivity(
        song: Song,
        currentPlaybackTimeMillis: Long,
        isPaused: Boolean = false,
    ) = runCatching {
        updateSong(song, currentPlaybackTimeMillis, isPaused).getOrThrow()
    }

    private suspend fun translateSongFields(song: Song): Map<String, String> {
        val translatorEnabled = context.dataStore[EnableTranslatorKey] ?: false
        if (!translatorEnabled) return emptyMap()

        if (System.currentTimeMillis() < translationCooldownUntilMs) return emptyMap()

        val contextList =
            (context.dataStore[TranslatorContextsKey] ?: "{song}")
                .split(",")
                .map { it.trim() }
        val targetLang = context.dataStore[TranslatorTargetLangKey] ?: "ENGLISH"
        val rawMap =
            mapOf(
                "{song}" to song.song.title,
                "{artist}" to song.artists.joinToString { it.name },
                "{album}" to (song.song.albumName ?: song.album?.title ?: ""),
            )
        val translatedMap = mutableMapOf<String, String>()

        runCatching {
            val translator = Translator()
            contextList.forEach { key ->
                val value = rawMap[key]?.takeIf { it.isNotBlank() } ?: return@forEach
                val cacheKey = "${song.song.id}:$key:$targetLang"
                val cached = translationCache[cacheKey]
                if (cached != null) {
                    translatedMap[key] = cached
                } else {
                    val translated =
                        runCatching {
                            translator
                                .translateBlocking(
                                    value,
                                    Language.valueOf(targetLang.uppercase()),
                                ).translatedText
                        }.getOrElse { error ->
                            val isRateLimited =
                                error.message?.contains("429", ignoreCase = true) == true ||
                                    error.message?.contains("Too Many Requests", ignoreCase = true) == true
                            if (isRateLimited) {
                                translationCooldownUntilMs =
                                    System.currentTimeMillis() + TRANSLATION_COOLDOWN_MS
                                Timber.tag(TAG).w(
                                    "Translation rate-limited (429) — pausing translations for %d minutes",
                                    TRANSLATION_COOLDOWN_MS / 60000,
                                )
                            } else {
                                Timber.tag(TAG).w(error, "Translation failed for %s", key)
                            }
                            value
                        }
                    translationCache[cacheKey] = translated
                    translatedMap[key] = translated
                }
            }
        }.onFailure {
            Timber.tag(TAG).e(it, "Translator init failed")
        }

        return translatedMap
    }

    private fun sourceValue(
        pref: String,
        song: Song,
        translatedMap: Map<String, String>,
        default: String,
    ): String =
        when (pref.uppercase()) {
            "ARTIST" -> translatedMap["{artist}"] ?: song.artists.joinToString { it.name }.ifBlank { default }
            "ALBUM" -> translatedMap["{album}"] ?: song.song.albumName ?: song.album?.title ?: default
            "SONG" -> translatedMap["{song}"] ?: song.song.title.ifBlank { default }
            "APP" -> context.getString(R.string.app_name)
            else -> default
        }

    private suspend fun resolveLargeText(
        song: Song,
        translatedMap: Map<String, String>,
    ): String? =
        when ((context.dataStore[DiscordLargeTextSourceKey] ?: "album").lowercase()) {
            "song" -> translatedMap["{song}"] ?: song.song.title
            "artist" -> translatedMap["{artist}"] ?: song.artists.joinToString { it.name }.takeIf { it.isNotBlank() }
            "album" -> translatedMap["{album}"] ?: song.song.albumName ?: song.album?.title ?: song.song.title
            "app" -> context.getString(R.string.app_name)
            "custom" -> (context.dataStore[DiscordLargeTextCustomKey] ?: "").ifBlank { null }
            "dontshow" -> null
            else -> translatedMap["{album}"] ?: song.song.albumName ?: song.album?.title
        }

    private fun resolveSmallText(
        song: Song,
        translatedMap: Map<String, String>,
        smallImageType: String,
        appName: String,
    ): String? {
        val base =
            when (smallImageType.lowercase()) {
                "song" -> translatedMap["{song}"] ?: song.song.title
                "artist" -> translatedMap["{artist}"] ?: song.artists.joinToString { it.name }.takeIf { it.isNotBlank() }
                "thumbnail", "album" -> translatedMap["{album}"] ?: song.song.albumName ?: song.album?.title
                "appicon", "app" -> appName
                "dontshow", "none" -> null
                else -> translatedMap["{artist}"] ?: song.artists.joinToString { it.name }.takeIf { it.isNotBlank() }
            }

        return base?.let { "$it on $appName" }
    }

    private suspend fun resolveButtons(song: Song): List<DiscordPresenceButton> {
        val button1Label = context.dataStore[DiscordActivityButton1LabelKey] ?: "Listen on YouTube Music"
        val button1Enabled = context.dataStore[DiscordActivityButton1EnabledKey] ?: true
        val button2Label = context.dataStore[DiscordActivityButton2LabelKey] ?: "Go to kongamusic"
        val button2Enabled = context.dataStore[DiscordActivityButton2EnabledKey] ?: true
        val button1UrlSource = context.dataStore[DiscordActivityButton1UrlSourceKey] ?: "songurl"
        val button1CustomUrl = context.dataStore[DiscordActivityButton1CustomUrlKey] ?: ""
        val button2UrlSource = context.dataStore[DiscordActivityButton2UrlSourceKey] ?: "custom"
        val button2CustomUrl =
            context.dataStore[DiscordActivityButton2CustomUrlKey]
                ?: "https://github.com/4nx3b/ArchiveTune"

        return buildList {
            if (button1Enabled) {
                val url = resolveUrl(button1UrlSource, song, button1CustomUrl)
                if (button1Label.isNotBlank() && !url.isNullOrBlank()) {
                    add(DiscordPresenceButton(button1Label.toButtonLabel(), url))
                }
            }

            if (button2Enabled) {
                val url = resolveUrl(button2UrlSource, song, button2CustomUrl)
                if (button2Label.isNotBlank() && !url.isNullOrBlank()) {
                    add(DiscordPresenceButton(button2Label.toButtonLabel(), url))
                }
            }
        }.take(2)
    }

    private fun resolveUrl(
        source: String,
        song: Song,
        custom: String,
    ): String? =
        when (source.lowercase()) {
            "songurl" -> {
                song.youtubeMusicUrl()
            }

            "artisturl" -> {
                song.discordArtistMusicUrl()
            }

            "albumurl" -> {
                song.discordAlbumMusicUrl()
            }

            "custom" -> {
                custom.normalizeUrl()
            }

            else -> {
                null
            }
        }?.toDiscordUrl()

    private fun buildTimestamps(
        song: Song,
        currentPlaybackTimeMillis: Long,
        isPaused: Boolean,
    ): DiscordPresenceTimestamps {
        if (isPaused) {
            val pausedStartMs = System.currentTimeMillis() - currentPlaybackTimeMillis.coerceAtLeast(0L)
            return DiscordPresenceTimestamps(
                startEpochSeconds = pausedStartMs / 1000L,
            )
        }

        val durationSeconds = song.song.duration.toLong()
        if (isPaused || durationSeconds <= 0L) {
            return DiscordPresenceTimestamps()
        }

        val startMs = System.currentTimeMillis() - currentPlaybackTimeMillis.coerceAtLeast(0L)
        return DiscordPresenceTimestamps(
            startEpochSeconds = startMs / 1000L,
            endEpochSeconds = (startMs + durationSeconds * 1000L) / 1000L,
        )
    }

    private fun String.normalizeUrl(): String? {
        val trimmed = trim()
        if (trimmed.isBlank()) return null
        return if (
            trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true)
        ) {
            trimmed
        } else {
            "https://$trimmed"
        }
    }

    private fun String?.toDiscordText(
        maxLength: Int,
        fallback: String? = null,
    ): String? {
        val value = this?.trim().orEmpty().ifBlank { fallback?.trim().orEmpty() }
        return value
            .takeIf { it.length >= 2 }
            ?.take(maxLength)
    }

    private fun String?.toDiscordUrl(): String? = this?.normalizeUrl()?.take(256)

    private fun String.toButtonLabel(): String = trim().take(32).ifBlank { context.getString(R.string.app_name) }

    private fun Song.youtubeMusicUrl(): String? =
        song.id
            .takeUnless { song.isLocal || it.isLocalMediaId() || it.isTelegramMediaId() }
            ?.let { "https://music.youtube.com/watch?v=$it" }

    private fun Song.discordArtistMusicUrl(): String? {
        if (song.isLocal || song.id.isLocalMediaId() || song.id.isTelegramMediaId()) return null

        return artists.firstNotNullOfOrNull { artist ->
            (artist.channelId ?: artist.id)
                .takeUnless { it.isBlank() || it.isLocalArtistId() || it.isLocalMediaId() }
                ?.let { "https://music.youtube.com/channel/$it" }
        }
    }

    private fun String.isLocalArtistId(): Boolean =
        startsWith("LOCAL_ARTIST_") ||
            startsWith("LA") ||
            contains("privately_owned_artist", ignoreCase = true)
}
