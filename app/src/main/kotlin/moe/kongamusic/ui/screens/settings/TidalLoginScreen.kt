/*
 * kongamusic (2026)
 * © Samk
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 *
 * WebView-based Tidal sign-in. Primary path is the official PKCE authorization-code flow
 * (durable refresh token, can unlock HiRes); if that fails it falls back to capturing the live
 * Bearer token that the Tidal web player (listen.tidal.com) sends to the API. Mirrors the
 * YouTube [LoginScreen] WebView pattern but persists the Tidal session directly to DataStore.
 */

package moe.kongamusic.ui.screens.settings

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.JavascriptInterface
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
import moe.kongamusic.constants.TidalAccessTokenKey
import moe.kongamusic.constants.TidalAccountNameKey
import moe.kongamusic.constants.TidalAuthFlowKey
import moe.kongamusic.constants.TidalCountryCodeKey
import moe.kongamusic.constants.TidalNeedsReloginKey
import moe.kongamusic.constants.TidalRefreshTokenKey
import moe.kongamusic.constants.TidalSubscriptionKey
import moe.kongamusic.constants.TidalSubscriptionStatus
import moe.kongamusic.constants.TidalTokenExpiryKey
import moe.kongamusic.constants.TidalUserIdKey
import moe.kongamusic.tidal.TidalAccountManager
import moe.kongamusic.ui.component.AuthWebViewScreen
import moe.kongamusic.utils.dataStore
import moe.kongamusic.utils.resetAuthWebViewSession
import java.util.concurrent.atomic.AtomicBoolean

const val TIDAL_LOGIN_ROUTE = "settings/tidal/login"

private const val WEB_PLAYER_URL = "https://listen.tidal.com"

private val BEARER_HOOK_JS =
    """
    javascript:(function(){
      if(window.__atTidalHook)return;window.__atTidalHook=1;
      function send(h){try{if(!h)return;var m=/Bearer\s+([A-Za-z0-9._\-]+)/i.exec(h);if(m&&m[1]&&m[1].length>20){TidalAuth.onBearer(m[1]);}}catch(e){}}
      try{
        var of=window.fetch;
        if(of){window.fetch=function(){try{var a=arguments[1];if(a&&a.headers){var hh=a.headers;var v=hh.get?hh.get('Authorization'):(hh['Authorization']||hh['authorization']);send(v);}}catch(e){}return of.apply(this,arguments);};}
      }catch(e){}
      try{
        var os=XMLHttpRequest.prototype.setRequestHeader;
        XMLHttpRequest.prototype.setRequestHeader=function(k,v){try{if(k&&String(k).toLowerCase()==='authorization'){send(v);}}catch(e){}return os.apply(this,arguments);};
      }catch(e){}
    })()
    """.trimIndent()

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TidalLoginScreen(navController: NavController) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()
    val pkce = remember { TidalAccountManager.buildPkceChallenge() }

    val handled = remember { AtomicBoolean(false) }

    fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun finishLogin(
        token: TidalAccountManager.TokenResult,
        flow: String,
    ) {
        scope.launch {
            val sub =
                token.userId?.let { uid ->
                    withContext(Dispatchers.IO) {
                        runCatching { TidalAccountManager.fetchSubscription(token.accessToken, uid) }
                            .getOrDefault(TidalAccountManager.Subscription.UNKNOWN)
                    }
                } ?: TidalAccountManager.Subscription.UNKNOWN
            val status =
                when (sub) {
                    TidalAccountManager.Subscription.PREMIUM -> TidalSubscriptionStatus.PREMIUM
                    TidalAccountManager.Subscription.FREE -> TidalSubscriptionStatus.FREE
                    TidalAccountManager.Subscription.UNKNOWN -> TidalSubscriptionStatus.UNKNOWN
                }
            context.dataStore.edit { prefs ->
                prefs[TidalAccessTokenKey] = token.accessToken
                if (token.refreshToken != null) {
                    prefs[TidalRefreshTokenKey] = token.refreshToken
                } else {
                    prefs.remove(TidalRefreshTokenKey)
                }
                prefs[TidalTokenExpiryKey] = token.expiresAtMillis
                prefs[TidalAccountNameKey] = token.username ?: "Tidal"
                token.userId?.let { prefs[TidalUserIdKey] = it }
                token.countryCode?.let { prefs[TidalCountryCodeKey] = it }
                prefs[TidalAuthFlowKey] = flow
                prefs[TidalSubscriptionKey] = status.name
                prefs[TidalNeedsReloginKey] = false
            }
            toast(context.getString(R.string.tidal_login_success))
            if (status == TidalSubscriptionStatus.FREE) {
                toast(context.getString(R.string.tidal_account_free_warning))
            }
            navController.navigateUp()
        }
    }

    fun switchToCapture(view: WebView) {
        toast(context.getString(R.string.tidal_login_webplayer_fallback))
        view.loadUrl(WEB_PLAYER_URL)
    }

    fun handleRedirect(
        view: WebView,
        url: String?,
    ): Boolean {
        if (url == null || !url.startsWith(TidalAccountManager.PKCE_REDIRECT_URI)) return false
        if (!handled.compareAndSet(false, true)) return true
        val uri = runCatching { Uri.parse(url) }.getOrNull()
        val code = uri?.getQueryParameter("code")
        val error = uri?.getQueryParameter("error")
        if (code.isNullOrBlank()) {

            handled.set(false)
            android.util.Log.w("TidalLogin", "PKCE redirect without code (error=$error)")
            switchToCapture(view)
            return true
        }
        scope.launch {
            val token =
                withContext(Dispatchers.IO) {
                    TidalAccountManager.exchangePkceCode(code, pkce.verifier, pkce.uniqueKey)
                }
            if (token != null) {
                finishLogin(token, TidalAccountManager.FLOW_PKCE)
            } else {

                handled.set(false)
                switchToCapture(view)
            }
        }
        return true
    }

    AuthWebViewScreen(
        navController = navController,
        title = stringResource(R.string.tidal_login),
        subtitle = stringResource(R.string.auth_webview_tidal_subtitle),
        factory = { ctx ->
            WebView(ctx).apply {
                webViewClient =
                    object : WebViewClient() {
                        override fun onPageStarted(
                            view: WebView,
                            url: String?,
                            favicon: Bitmap?,
                        ) {
                            handleRedirect(view, url)
                        }

                        @Deprecated("Deprecated in Java")
                        override fun shouldOverrideUrlLoading(
                            view: WebView,
                            url: String?,
                        ): Boolean = handleRedirect(view, url)

                        override fun onPageFinished(
                            view: WebView,
                            url: String?,
                        ) {

                            if (url?.contains("tidal.com", ignoreCase = true) == true &&
                                url.contains("listen", ignoreCase = true)
                            ) {
                                view.loadUrl(BEARER_HOOK_JS)
                            }
                        }
                    }
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                }
                addJavascriptInterface(
                    object {
                        @JavascriptInterface
                        fun onBearer(bearer: String?) {
                            if (bearer.isNullOrBlank()) return
                            if (!handled.compareAndSet(false, true)) return
                            scope.launch {
                                val token =
                                    withContext(Dispatchers.IO) {
                                        TidalAccountManager.buildSessionFromBearer(bearer)
                                    }
                                if (token != null) {
                                    finishLogin(token, TidalAccountManager.FLOW_WEBCAPTURE)
                                } else {
                                    handled.set(false)
                                }
                            }
                        }
                    },
                    "TidalAuth",
                )
                resetAuthWebViewSession(ctx, this, clearCookies = true) {
                    loadUrl(pkce.authUrl)
                }
            }
        },
    )
}
