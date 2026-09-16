/*
 * kongamusic (2026)
 * © Samk
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package moe.kongamusic.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import moe.kongamusic.LocalPlayerAwareWindowInsets
import moe.kongamusic.R
import moe.kongamusic.constants.EnableBetterLyricsKey
import moe.kongamusic.constants.EnableBetterLyricsPortatoKey
import moe.kongamusic.constants.EnableKugouKey
import moe.kongamusic.constants.EnableLrcLibKey
import moe.kongamusic.constants.EnableMusixmatchExperimentalKey
import moe.kongamusic.constants.EnableUnisonLyricsKey
import moe.kongamusic.constants.EnableYouLyPlusLyricsKey
import moe.kongamusic.constants.LyricsProviderOrderKey
import moe.kongamusic.constants.PrioritizeWordSyncedLyricsKey
import moe.kongamusic.constants.deserializeLyricsProviderOrder
import moe.kongamusic.lyrics.LyricsProviderTestOutcome
import moe.kongamusic.ui.component.DefaultDialog
import moe.kongamusic.ui.component.FrostedHeaderPill
import moe.kongamusic.ui.component.IconButton
import moe.kongamusic.ui.component.PreferenceEntry
import moe.kongamusic.ui.component.PreferenceGroup
import moe.kongamusic.ui.component.SwitchPreference
import moe.kongamusic.ui.utils.backToMain
import moe.kongamusic.utils.rememberPreference
import moe.kongamusic.viewmodels.ContentSettingsViewModel
import moe.kongamusic.viewmodels.LyricsTestState
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun LyricsProvidersSettings(
    navController: NavController,
    viewModel: ContentSettingsViewModel = hiltViewModel(),
    scrollTo: String? = null,
) {
    val (enableLrclib, onEnableLrclibChange) = rememberPreference(key = EnableLrcLibKey, defaultValue = true)
    val (enableKugou, onEnableKugouChange) = rememberPreference(key = EnableKugouKey, defaultValue = true)
    val (enableBetterLyrics, onEnableBetterLyricsChange) =
        rememberPreference(key = EnableBetterLyricsKey, defaultValue = true)
    val (enableBetterLyricsPortato, onEnableBetterLyricsPortatoChange) =
        rememberPreference(key = EnableBetterLyricsPortatoKey, defaultValue = true)
    val (enableYouLyPlusLyrics, onEnableYouLyPlusLyricsChange) =
        rememberPreference(key = EnableYouLyPlusLyricsKey, defaultValue = true)
    val (enableUnisonLyrics, onEnableUnisonLyricsChange) =
        rememberPreference(key = EnableUnisonLyricsKey, defaultValue = true)
    val (prioritizeWordSynced, onPrioritizeWordSyncedChange) =
        rememberPreference(key = PrioritizeWordSyncedLyricsKey, defaultValue = false)
    val (enableMusixmatchExperimental, onEnableMusixmatchExperimentalChange) =
        rememberPreference(key = EnableMusixmatchExperimentalKey, defaultValue = false)
    val (providerOrderStr, onProviderOrderStrChange) =
        rememberPreference(key = LyricsProviderOrderKey, defaultValue = "")
    val providerOrder =
        remember(providerOrderStr) {
            deserializeLyricsProviderOrder(providerOrderStr)
        }

    var showProviderOrderDialog by remember { mutableStateOf(false) }
    var showLyricsTestDialog by remember { mutableStateOf(false) }

    if (showLyricsTestDialog) {
        val testState by viewModel.lyricsTestState.collectAsStateWithLifecycle()
        androidx.compose.runtime.LaunchedEffect(Unit) {
            if (testState is LyricsTestState.Idle) {
                viewModel.runLyricsTest()
            }
        }
        LyricsTestDialog(
            state = testState,
            onDismiss = { showLyricsTestDialog = false },
            onRetry = { viewModel.runLyricsTest() },
        )
    }

    if (showProviderOrderDialog) {
        LyricsProviderOrderDialog(
            initialOrder = providerOrder,
            onDismiss = { showProviderOrderDialog = false },
            onConfirm = { newOrder ->
                onProviderOrderStrChange(newOrder.joinToString(",") { it.name })
                showProviderOrderDialog = false
            },
        )
    }

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
                            text = stringResource(R.string.providers),
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
            PreferenceGroup(title = stringResource(R.string.providers)) {

                item {
                    SwitchPreference(
                        modifier = positions.modifierFor("prioritize_word_synced_lyrics"),
                        title = { Text(stringResource(R.string.prioritize_word_synced_lyrics)) },
                        description = stringResource(R.string.prioritize_word_synced_lyrics_desc),
                        icon = { Icon(painterResource(R.drawable.lyrics), null) },
                        checked = prioritizeWordSynced,
                        onCheckedChange = onPrioritizeWordSyncedChange,
                    )
                }

                val providerTogglesEnabled = !prioritizeWordSynced

                item {
                    SwitchPreference(
                        modifier = positions.modifierFor("enable_betterlyrics", "betterlyrics"),
                        title = { Text(stringResource(R.string.enable_betterlyrics)) },
                        icon = { Icon(painterResource(R.drawable.lyrics), null) },
                        checked = enableBetterLyrics,
                        onCheckedChange = onEnableBetterLyricsChange,
                        isEnabled = providerTogglesEnabled,
                    )
                }

                item {
                    SwitchPreference(
                        modifier = positions.modifierFor("enable_betterlyrics_portato", "betterlyrics_portato"),
                        title = { Text(stringResource(R.string.enable_betterlyrics_portato)) },
                        icon = { Icon(painterResource(R.drawable.lyrics), null) },
                        checked = enableBetterLyricsPortato,
                        onCheckedChange = onEnableBetterLyricsPortatoChange,
                        isEnabled = providerTogglesEnabled,
                    )
                }

                item {
                    SwitchPreference(
                        modifier = positions.modifierFor("enable_youlyplus_lyrics", "youlyplus_lyrics"),
                        title = { Text(stringResource(R.string.enable_youlyplus_lyrics)) },
                        icon = { Icon(painterResource(R.drawable.lyrics), null) },
                        checked = enableYouLyPlusLyrics,
                        onCheckedChange = onEnableYouLyPlusLyricsChange,
                        isEnabled = providerTogglesEnabled,
                    )
                }

                item {
                    SwitchPreference(
                        modifier = positions.modifierFor("enable_lrclib", "lrclib"),
                        title = { Text(stringResource(R.string.enable_lrclib)) },
                        icon = { Icon(painterResource(R.drawable.lyrics), null) },
                        checked = enableLrclib,
                        onCheckedChange = onEnableLrclibChange,
                        isEnabled = providerTogglesEnabled,
                    )
                }

                item {
                    SwitchPreference(
                        modifier = positions.modifierFor("enable_kugou", "kugou"),
                        title = { Text(stringResource(R.string.enable_kugou)) },
                        icon = { Icon(painterResource(R.drawable.lyrics), null) },
                        checked = enableKugou,
                        onCheckedChange = onEnableKugouChange,
                        isEnabled = providerTogglesEnabled,
                    )
                }

                item {
                    SwitchPreference(
                        modifier = positions.modifierFor("enable_unison_lyrics", "unison_lyrics"),
                        title = { Text(stringResource(R.string.enable_unison_lyrics)) },
                        icon = { Icon(painterResource(R.drawable.lyrics), null) },
                        checked = enableUnisonLyrics,
                        onCheckedChange = onEnableUnisonLyricsChange,
                        isEnabled = providerTogglesEnabled,
                    )
                }

                item {
                    PreferenceEntry(
                        modifier = positions.modifierFor("lyrics_test"),
                        title = { Text(stringResource(R.string.lyrics_test)) },
                        description = stringResource(R.string.lyrics_test_description),
                        icon = { Icon(painterResource(R.drawable.lyrics), null) },
                        onClick = { showLyricsTestDialog = true },
                        isEnabled = providerTogglesEnabled,
                    )
                }

                item {
                    PreferenceEntry(
                        modifier = positions.modifierFor("set_first_lyrics_provider", "first_lyrics_provider"),
                        title = { Text(stringResource(R.string.set_first_lyrics_provider)) },
                        description = providerOrder.firstOrNull()?.displayName(),
                        icon = { Icon(painterResource(R.drawable.lyrics), null) },
                        onClick = { showProviderOrderDialog = true },
                        isEnabled = providerTogglesEnabled,
                    )
                }
            }

            PreferenceGroup(title = stringResource(R.string.musixmatch_experimental_section)) {
                item {
                    SwitchPreference(
                        modifier = positions.modifierFor("enable_musixmatch_experimental"),
                        title = { Text(stringResource(R.string.enable_musixmatch_experimental)) },
                        description = stringResource(R.string.enable_musixmatch_experimental_desc),
                        icon = { Icon(painterResource(R.drawable.lyrics), null) },
                        checked = enableMusixmatchExperimental,
                        onCheckedChange = onEnableMusixmatchExperimentalChange,
                        isEnabled = !prioritizeWordSynced,
                    )
                }
                item(visible = enableMusixmatchExperimental) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                    ) {
                        Text(
                            text = stringResource(R.string.musixmatch_experimental_warning),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LyricsTestDialog(
    state: LyricsTestState,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
) {
    DefaultDialog(
        onDismiss = onDismiss,
        title = { Text(stringResource(R.string.lyrics_test)) },
        icon = { Icon(painterResource(R.drawable.lyrics), contentDescription = null) },
        buttons = {
            if (state is LyricsTestState.Done) {
                TextButton(onClick = onRetry) {
                    Text(stringResource(R.string.retry))
                }
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.ok))
            }
        },
    ) {
        when (state) {
            LyricsTestState.Idle,
            LyricsTestState.Loading,
            -> {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        LoadingIndicator()
                    }
                    Text(
                        text = stringResource(R.string.lyrics_test_running),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            is LyricsTestState.Done -> {
                val working = state.results.count { it.outcome == LyricsProviderTestOutcome.OK }
                val total = state.results.size
                val summary =
                    if (working == 0) {
                        stringResource(R.string.lyrics_test_summary_none)
                    } else {
                        stringResource(R.string.lyrics_test_summary_ok, working, total)
                    }
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    state.results.forEach { result ->
                        val (label, tint, iconRes) =
                            when (result.outcome) {
                                LyricsProviderTestOutcome.OK ->
                                    Triple(
                                        stringResource(R.string.lyrics_test_ok),
                                        MaterialTheme.colorScheme.primary,
                                        R.drawable.check,
                                    )
                                LyricsProviderTestOutcome.NO_MATCH ->
                                    Triple(
                                        stringResource(R.string.lyrics_test_no_match),
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                        R.drawable.info,
                                    )
                                LyricsProviderTestOutcome.TIMEOUT ->
                                    Triple(
                                        stringResource(R.string.lyrics_test_timeout),
                                        MaterialTheme.colorScheme.error,
                                        R.drawable.error,
                                    )
                                LyricsProviderTestOutcome.FAILED ->
                                    Triple(
                                        stringResource(R.string.lyrics_test_failed),
                                        MaterialTheme.colorScheme.error,
                                        R.drawable.error,
                                    )
                            }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                painterResource(iconRes),
                                contentDescription = null,
                                tint = tint,
                                modifier = Modifier.size(18.dp),
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = result.providerName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = tint,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
