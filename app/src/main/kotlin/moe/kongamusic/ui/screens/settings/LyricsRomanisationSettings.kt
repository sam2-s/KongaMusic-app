/*
 * kongamusic (2026)
 * © Samk
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package moe.kongamusic.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import moe.kongamusic.LocalPlayerAwareWindowInsets
import moe.kongamusic.R
import moe.kongamusic.constants.AiRomanizeLyricsKey
import moe.kongamusic.constants.LyricsRomanizeChineseKey
import moe.kongamusic.constants.LyricsRomanizeHindiKey
import moe.kongamusic.constants.LyricsRomanizeJapaneseKey
import moe.kongamusic.constants.LyricsRomanizeKoreanKey
import moe.kongamusic.constants.LyricsRomanizeOtherLanguagesKey
import moe.kongamusic.lyrics.JapaneseLanguagePackManager
import moe.kongamusic.lyrics.JapaneseLanguagePackState
import moe.kongamusic.ui.component.FrostedHeaderPill
import moe.kongamusic.ui.component.IconButton
import moe.kongamusic.ui.component.PreferenceGroup
import moe.kongamusic.ui.component.SwitchPreference
import moe.kongamusic.ui.utils.backToMain
import moe.kongamusic.utils.rememberPreference
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.runtime.getValue

@Composable
fun LyricsRomanisationSettings(
    navController: NavController,
    scrollTo: String? = null,
) {
    val (lyricsRomanizeJapanese, onLyricsRomanizeJapaneseChange) =
        rememberPreference(LyricsRomanizeJapaneseKey, defaultValue = false)
    val (lyricsRomanizeKorean, onLyricsRomanizeKoreanChange) =
        rememberPreference(LyricsRomanizeKoreanKey, defaultValue = true)
    val (lyricsRomanizeChinese, onLyricsRomanizeChineseChange) =
        rememberPreference(LyricsRomanizeChineseKey, defaultValue = true)
    val (lyricsRomanizeHindi, onLyricsRomanizeHindiChange) =
        rememberPreference(LyricsRomanizeHindiKey, defaultValue = true)
    val (lyricsRomanizeOtherLanguages, onLyricsRomanizeOtherLanguagesChange) =
        rememberPreference(LyricsRomanizeOtherLanguagesKey, defaultValue = true)
    val japaneseLanguagePackState by JapaneseLanguagePackManager.state.collectAsStateWithLifecycle()

    val (aiRomanizeLyrics) = rememberPreference(AiRomanizeLyricsKey, defaultValue = false)
    val builtInEnabled = !aiRomanizeLyrics
    val overriddenDescription =
        if (aiRomanizeLyrics) stringResource(R.string.romanization_overridden_by_ai) else null

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    FrostedHeaderPill(plain = true) {
                        IconButton(
                            onClick = navController::navigateUp,
                            onLongClick = navController::backToMain,
                        ) {
                            Icon(
                                painterResource(R.drawable.arrow_back),
                                contentDescription = null,
                            )
                        }
                        Text(
                            text = stringResource(R.string.romanization),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            modifier = Modifier.padding(end = 4.dp),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        val playerAwareBottomPadding =
            LocalPlayerAwareWindowInsets.current
                .only(WindowInsetsSides.Bottom)
                .asPaddingValues()
                .calculateBottomPadding()
        val scrollState = rememberScrollState()
        val positions = rememberPreferencePositions()
        androidx.compose.runtime.LaunchedEffect(scrollTo) { positions.scrollToKey(scrollTo, scrollState) }
        Column(
            Modifier
                .padding(top = innerPadding.calculateTopPadding())
                .windowInsetsPadding(
                    LocalPlayerAwareWindowInsets.current.only(
                        WindowInsetsSides.Horizontal,
                    ),
                )

                .then(positions.containerModifier())
                .verticalScroll(scrollState)
                .padding(bottom = playerAwareBottomPadding + SettingsDimensions.ScreenBottomPadding),
        ) {
            PreferenceGroup(title = stringResource(R.string.romanization)) {
                item {
                    SwitchPreference(
                        modifier = positions.modifierFor("lyrics_romanize_japanese"),
                        title = { Text(stringResource(R.string.lyrics_romanize_japanese)) },
                        description =
                            overriddenDescription
                                ?: if (japaneseLanguagePackState is JapaneseLanguagePackState.Installed) {
                                    null
                                } else {
                                    stringResource(R.string.language_pack_required)
                                },
                        icon = { Icon(painterResource(R.drawable.lyrics), null) },
                        checked = lyricsRomanizeJapanese,
                        onCheckedChange = onLyricsRomanizeJapaneseChange,
                        isEnabled =
                            builtInEnabled && japaneseLanguagePackState is JapaneseLanguagePackState.Installed,
                    )
                }

                item {
                    SwitchPreference(
                        modifier = positions.modifierFor("lyrics_romanize_korean"),
                        title = { Text(stringResource(R.string.lyrics_romanize_korean)) },
                        description = overriddenDescription,
                        icon = { Icon(painterResource(R.drawable.lyrics), null) },
                        checked = lyricsRomanizeKorean,
                        onCheckedChange = onLyricsRomanizeKoreanChange,
                        isEnabled = builtInEnabled,
                    )
                }

                item {
                    SwitchPreference(
                        modifier = positions.modifierFor("lyrics_romanize_chinese"),
                        title = { Text(stringResource(R.string.lyrics_romanize_chinese)) },
                        description = overriddenDescription,
                        icon = { Icon(painterResource(R.drawable.lyrics), null) },
                        checked = lyricsRomanizeChinese,
                        onCheckedChange = onLyricsRomanizeChineseChange,
                        isEnabled = builtInEnabled,
                    )
                }

                item {
                    SwitchPreference(
                        modifier = positions.modifierFor("lyrics_romanize_hindi"),
                        title = { Text(stringResource(R.string.lyrics_romanize_hindi)) },
                        description = overriddenDescription,
                        icon = { Icon(painterResource(R.drawable.lyrics), null) },
                        checked = lyricsRomanizeHindi,
                        onCheckedChange = onLyricsRomanizeHindiChange,
                        isEnabled = builtInEnabled,
                    )
                }

                item {
                    SwitchPreference(
                        modifier = positions.modifierFor("lyrics_romanize_other_languages", "lyrics_romanize_other"),
                        title = { Text(stringResource(R.string.lyrics_romanize_other_languages)) },
                        description = overriddenDescription,
                        icon = { Icon(painterResource(R.drawable.lyrics), null) },
                        checked = lyricsRomanizeOtherLanguages,
                        onCheckedChange = onLyricsRomanizeOtherLanguagesChange,
                        isEnabled = builtInEnabled,
                    )
                }
            }
        }
    }
}
