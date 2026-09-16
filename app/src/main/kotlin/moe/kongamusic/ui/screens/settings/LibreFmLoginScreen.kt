/*
 * kongamusic (2026)
 * © Samk
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.kongamusic.ui.screens.settings

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.datastore.preferences.core.edit
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.kongamusic.R
import moe.kongamusic.constants.CustomScrobbleApiKeyOverrideKey
import moe.kongamusic.constants.CustomScrobbleSecretOverrideKey
import moe.kongamusic.constants.LastFmProvider
import moe.kongamusic.constants.LastFMCustomEndpointKey
import moe.kongamusic.constants.LastFMProviderKey
import moe.kongamusic.constants.LastFMSessionKey
import moe.kongamusic.constants.LastFMUsernameKey
import moe.kongamusic.constants.LibreFMApiKeyOverrideKey
import moe.kongamusic.constants.LibreFMSecretOverrideKey
import moe.kongamusic.lastfm.LastFM
import moe.kongamusic.lastfm.LastFmAppCredentials
import moe.kongamusic.lastfm.models.Authentication
import moe.kongamusic.ui.component.AuthWebViewScreen
import moe.kongamusic.utils.dataStore
import moe.kongamusic.utils.resetAuthWebViewSession
import java.util.concurrent.atomic.AtomicBoolean

const val LASTFM_LIBREFM_LOGIN_ROUTE = "settings/lastfm/librefm-login"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LibreFmLoginScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val handled = remember { AtomicBoolean(false) }

    fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun finishLogin(auth: Authentication) {
        scope.launch {

            LastFM.configure(
                endpoint = LastFM.LIBREFM_API_ENDPOINT,
                apiKey = LastFmAppCredentials.API_KEY,
                secret = LastFmAppCredentials.API_SECRET,
                sessionKey = auth.session.key,
            )
            context.dataStore.edit { prefs ->
                prefs[LastFMProviderKey] = LastFmProvider.LIBREFM.name
                prefs[LastFMCustomEndpointKey] = ""

                prefs[LibreFMApiKeyOverrideKey] = LastFmAppCredentials.API_KEY
                prefs[LibreFMSecretOverrideKey] = LastFmAppCredentials.API_SECRET

                prefs[CustomScrobbleApiKeyOverrideKey] = ""
                prefs[CustomScrobbleSecretOverrideKey] = ""
                prefs[LastFMUsernameKey] = auth.session.name
                prefs[LastFMSessionKey] = auth.session.key
            }
            withContext(Dispatchers.Main) {
                toast(context.getString(R.string.lastfm_login_success))
                navController.navigateUp()
            }
        }
    }

    fun handleRedirect(url: String?): Boolean {
        if (url == null || !url.startsWith(LastFmAppCredentials.AUTH_CALLBACK_URI)) return false
        if (!handled.compareAndSet(false, true)) return true
        val uri = runCatching { Uri.parse(url) }.getOrNull()
        val token = uri?.getQueryParameter("token")?.trim()
        if (token.isNullOrBlank()) {
            android.util.Log.w("LibreFmLogin", "Auth callback without token: $url")
            scope.launch {
                withContext(Dispatchers.Main) {
                    toast(context.getString(R.string.lastfm_login_cancelled))
                    navController.navigateUp()
                }
            }
            return true
        }
        scope.launch {
            val result = withContext(Dispatchers.IO) {

                LastFM.configure(
                    endpoint = LastFM.LIBREFM_API_ENDPOINT,
                    apiKey = LastFmAppCredentials.API_KEY,
                    secret = LastFmAppCredentials.API_SECRET,
                    sessionKey = null,
                )
                LastFM.getSession(token)
            }
            result
                .onSuccess { auth -> finishLogin(auth) }
                .onFailure { error ->
                    android.util.Log.e("LibreFmLogin", "auth.getSession failed", error)
                    handled.set(false)
                    withContext(Dispatchers.Main) {
                        toast(context.getString(R.string.lastfm_login_failed))
                        navController.navigateUp()
                    }
                }
        }
        return true
    }

    AuthWebViewScreen(
        navController = navController,
        title = stringResource(R.string.lastfm_connect_librefm_button),
        subtitle = stringResource(R.string.auth_webview_librefm_subtitle),
        factory = { ctx ->
            WebView(ctx).apply {
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(
                        view: WebView,
                        url: String?,
                        favicon: Bitmap?,
                    ) {
                        handleRedirect(url)
                    }

                    @Deprecated("Deprecated in Java")
                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        url: String?,
                    ): Boolean = handleRedirect(url)
                }
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                }
                resetAuthWebViewSession(ctx, this, clearCookies = true) {

                    loadUrl(libreFmAuthUrl())
                }
            }
        },
    )
}

private fun libreFmAuthUrl(): String =
    "https://libre.fm/api/auth/?api_key=${LastFmAppCredentials.API_KEY}&cb=${LastFmAppCredentials.AUTH_CALLBACK_URI}"
