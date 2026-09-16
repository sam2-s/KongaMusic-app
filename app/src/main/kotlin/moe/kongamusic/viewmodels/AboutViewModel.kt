/*
 * kongamusic (2026)
 * © Samk
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.kongamusic.viewmodels

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import moe.kongamusic.BuildConfig
import moe.kongamusic.R
import javax.inject.Inject

@Immutable
data class AboutUiModel(
    @StringRes val appNameResId: Int,
    val versionName: String,
    val buildVariant: String,
    val author: String = "Samk",
    val developerUrl: String = "https://github.com/sam2-s",
)

@HiltViewModel
class AboutViewModel
    @Inject
    constructor() : ViewModel() {
        val state: StateFlow<AboutUiModel> =
            MutableStateFlow(
                AboutUiModel(
                    appNameResId = R.string.app_name,
                    versionName = "v1",
                    buildVariant = if (BuildConfig.DEBUG) "DEBUG" else BuildConfig.ARCHITECTURE.uppercase(),
                ),
            ).asStateFlow()
    }