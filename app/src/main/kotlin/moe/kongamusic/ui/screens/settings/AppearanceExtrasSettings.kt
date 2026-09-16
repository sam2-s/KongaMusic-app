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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import moe.kongamusic.LocalPlayerAwareWindowInsets
import moe.kongamusic.R
import moe.kongamusic.constants.HideCachedCardKey
import moe.kongamusic.constants.HideLikedSongsCardKey
import moe.kongamusic.constants.HideLocalFilesCardKey
import moe.kongamusic.constants.HideOfflineCardKey
import moe.kongamusic.constants.HideTop50CardKey
import moe.kongamusic.constants.ShowHomeCategoryChipsKey
import moe.kongamusic.constants.ShowTagsInLibraryKey
import androidx.compose.material3.TopAppBar
import moe.kongamusic.ui.component.FrostedHeaderPill
import moe.kongamusic.ui.component.IconButton
import moe.kongamusic.ui.component.PreferenceGroup
import moe.kongamusic.ui.component.SwitchPreference
import moe.kongamusic.ui.utils.backToMain
import moe.kongamusic.utils.rememberPreference
import androidx.compose.foundation.layout.asPaddingValues
import moe.kongamusic.ui.screens.ScreenHeaderHaze
import moe.kongamusic.ui.screens.rememberScreenHeaderHaze
import moe.kongamusic.LocalStableSystemBarsTopPadding
import dev.chrisbanes.haze.hazeSource
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize

@Composable
fun AppearanceExtrasSettings(
    navController: NavController,
    scrollTo: String? = null,
) {
    val (showHomeCategoryChips, onShowHomeCategoryChipsChange) =
        rememberPreference(ShowHomeCategoryChipsKey, defaultValue = false)
    val (showTagsInLibrary, onShowTagsInLibraryChange) =
        rememberPreference(ShowTagsInLibraryKey, defaultValue = false)
    val (hideLikedSongsCard, onHideLikedSongsCardChange) =
        rememberPreference(HideLikedSongsCardKey, defaultValue = false)
    val (hideOfflineCard, onHideOfflineCardChange) =
        rememberPreference(HideOfflineCardKey, defaultValue = false)
    val (hideCachedCard, onHideCachedCardChange) =
        rememberPreference(HideCachedCardKey, defaultValue = false)
    val (hideLocalFilesCard, onHideLocalFilesCardChange) =
        rememberPreference(HideLocalFilesCardKey, defaultValue = false)
    val (hideTop50Card, onHideTop50CardChange) =
        rememberPreference(HideTop50CardKey, defaultValue = false)

    val headerHaze = rememberScreenHeaderHaze()
    val systemBarsTopPadding = LocalStableSystemBarsTopPadding.current

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
                                painter = painterResource(R.drawable.arrow_back),
                                contentDescription = null,
                            )
                        }
                        Text(
                            text = stringResource(R.string.extras),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            modifier = Modifier.padding(end = 4.dp),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                ),
            )
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {

        val playerAwareBottomPadding =
            LocalPlayerAwareWindowInsets.current
                .only(WindowInsetsSides.Bottom)
                .asPaddingValues()
                .calculateBottomPadding()
        val topPadding = innerPadding.calculateTopPadding()
        val scrollState = rememberScrollState()
        val positions = rememberPreferencePositions()
        androidx.compose.runtime.LaunchedEffect(scrollTo) { positions.scrollToKey(scrollTo, scrollState) }

        Column(
            Modifier
                .windowInsetsPadding(
                    LocalPlayerAwareWindowInsets.current.only(
                        WindowInsetsSides.Horizontal,
                    ),
                )

                .then(positions.containerModifier())
                .verticalScroll(scrollState)
                .hazeSource(headerHaze)
                .padding(top = topPadding)
                .padding(bottom = playerAwareBottomPadding + SettingsDimensions.ScreenBottomPadding),
        ) {
            PreferenceGroup(title = stringResource(R.string.extras)) {
                item {
                    SwitchPreference(
                        modifier = positions.modifierFor("show_home_category_chips"),
                        title = { Text(stringResource(R.string.show_home_category_chips)) },
                        description = stringResource(R.string.show_home_category_chips_desc),
                        icon = { Icon(painterResource(R.drawable.home_outlined), null) },
                        checked = showHomeCategoryChips,
                        onCheckedChange = onShowHomeCategoryChipsChange,
                    )
                }

                item {
                    SwitchPreference(
                        modifier = positions.modifierFor("show_tags_in_library"),
                        title = { Text(stringResource(R.string.show_tags_in_library)) },
                        description = stringResource(R.string.show_tags_in_library_desc),
                        icon = { Icon(painterResource(R.drawable.filter_alt), null) },
                        checked = showTagsInLibrary,
                        onCheckedChange = onShowTagsInLibraryChange,
                    )
                }

                item {
                    SwitchPreference(
                        modifier = positions.modifierFor("hide_liked_songs_card"),
                        title = { Text(stringResource(R.string.hide_liked_songs_card)) },
                        description = stringResource(R.string.hide_liked_songs_card_desc),
                        icon = { Icon(painterResource(R.drawable.favorite), null) },
                        checked = hideLikedSongsCard,
                        onCheckedChange = onHideLikedSongsCardChange,
                    )
                }

                item {
                    SwitchPreference(
                        modifier = positions.modifierFor("hide_offline_card"),
                        title = { Text(stringResource(R.string.hide_offline_card)) },
                        description = stringResource(R.string.hide_offline_card_desc),
                        icon = { Icon(painterResource(R.drawable.offline), null) },
                        checked = hideOfflineCard,
                        onCheckedChange = onHideOfflineCardChange,
                    )
                }

                item {
                    SwitchPreference(
                        modifier = positions.modifierFor("hide_cached_card"),
                        title = { Text(stringResource(R.string.hide_cached_card)) },
                        description = stringResource(R.string.hide_cached_card_desc),
                        icon = { Icon(painterResource(R.drawable.cached), null) },
                        checked = hideCachedCard,
                        onCheckedChange = onHideCachedCardChange,
                    )
                }

                item {
                    SwitchPreference(
                        modifier = positions.modifierFor("hide_local_files_card"),
                        title = { Text(stringResource(R.string.hide_local_files_card)) },
                        description = stringResource(R.string.hide_local_files_card_desc),
                        icon = { Icon(painterResource(R.drawable.snippet_folder), null) },
                        checked = hideLocalFilesCard,
                        onCheckedChange = onHideLocalFilesCardChange,
                    )
                }

                item {
                    SwitchPreference(
                        modifier = positions.modifierFor("hide_top50_card"),
                        title = { Text(stringResource(R.string.hide_top50_card)) },
                        description = stringResource(R.string.hide_top50_card_desc),
                        icon = { Icon(painterResource(R.drawable.trending_up), null) },
                        checked = hideTop50Card,
                        onCheckedChange = onHideTop50CardChange,
                    )
                }
            }
        }

        ScreenHeaderHaze(
            hazeState = headerHaze,
            systemBarsTopPadding = systemBarsTopPadding,
        )
        }
}
}
