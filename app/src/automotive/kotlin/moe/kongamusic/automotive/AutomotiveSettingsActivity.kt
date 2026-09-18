/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.kongamusic.automotive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import moe.kongamusic.ui.screens.settings.AndroidAutoSettingsRoute
import moe.kongamusic.ui.theme.KongamusicTheme

@AndroidEntryPoint
class AutomotiveSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KongamusicTheme(disableAnimations = true) {
                AndroidAutoSettingsRoute(onBack = ::finish)
            }
        }
    }
}
