/*
 * kongamusic (2026)
 * © Samk
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.kongamusic.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import moe.kongamusic.constants.InnerTubeCookieKey
import moe.kongamusic.constants.YtmSyncKey
import moe.kongamusic.db.MusicDatabase
import moe.kongamusic.db.entities.PlaylistEntity
import moe.kongamusic.extensions.isInternetConnected
import moe.kongamusic.innertube.YouTube
import moe.kongamusic.innertube.utils.hasYouTubeLoginCookie
import moe.kongamusic.utils.dataStore
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

data class PlaylistCreationAvailability(
    val isSignedIn: Boolean,
    val isSyncEnabled: Boolean,
)

@Singleton
class PlaylistCreationRepository
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val database: MusicDatabase,
    ) {
        suspend fun getAvailability(): PlaylistCreationAvailability =
            withContext(Dispatchers.IO) {
                val preferences = context.dataStore.data.first()
                val isSignedIn = hasYouTubeLoginCookie(preferences[InnerTubeCookieKey].orEmpty())
                PlaylistCreationAvailability(
                    isSignedIn = isSignedIn,
                    isSyncEnabled =
                        isSignedIn &&
                            (preferences[YtmSyncKey] ?: true) &&
                            context.isInternetConnected(),
                )
            }

        suspend fun createRemotePlaylist(name: String): Result<String> =
            withContext(Dispatchers.IO) {
                YouTube.createPlaylist(name)
            }

        suspend fun createLocalPlaylist(
            name: String,
            browseId: String?,
        ) = withContext(Dispatchers.IO) {
            database.withTransaction {
                insert(
                    PlaylistEntity(
                        name = name,
                        browseId = browseId,
                        bookmarkedAt = LocalDateTime.now(),
                        isEditable = true,
                    ),
                )
            }
        }
    }
