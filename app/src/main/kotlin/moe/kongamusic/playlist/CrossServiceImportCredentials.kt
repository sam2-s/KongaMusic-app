/*
 * kongamusic (2026)
 * © Samk
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.kongamusic.playlist

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import moe.kongamusic.constants.QobuzTokensKey
import moe.kongamusic.constants.TidalAccessTokenKey
import moe.kongamusic.constants.TidalCountryCodeKey
import moe.kongamusic.qobuz.QobuzToken
import moe.kongamusic.utils.PoolAccountManager
import moe.kongamusic.utils.dataStore

object CrossServiceImportCredentials {

    suspend fun load(context: Context): CrossServicePlaylistImporter.Credentials =
        withContext(Dispatchers.IO) {

            runCatching { PoolAccountManager.loadCached(context) }

            val prefs = runCatching { context.dataStore.data.first() }.getOrNull()

            val userTidalToken = prefs?.get(TidalAccessTokenKey)?.takeIf { it.isNotBlank() }
            val poolTidal = PoolAccountManager.tidalAccounts().firstOrNull()
            val tidalToken = userTidalToken ?: poolTidal?.token?.takeIf { it.isNotBlank() }
            val tidalCountry = prefs?.get(TidalCountryCodeKey)?.takeIf { it.isNotBlank() }
                ?: poolTidal?.countryCode?.takeIf { it.isNotBlank() }
                ?: "US"

            val qobuz = QobuzToken.listFromJson(prefs?.get(QobuzTokensKey))
                .firstOrNull { it.token.isNotBlank() && it.appId.isNotBlank() }
                ?.let { it.appId to it.token }
                ?: PoolAccountManager.qobuzAccounts()
                    .firstOrNull { it.token.isNotBlank() && it.appId.isNotBlank() }
                    ?.let { it.appId to it.token }

            CrossServicePlaylistImporter.Credentials(
                tidalAccessToken = tidalToken,
                tidalCountryCode = tidalCountry,
                qobuzAppId = qobuz?.first,
                qobuzAuthToken = qobuz?.second,
            )
        }
}
