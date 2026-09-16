/*
 * kongamusic (2026)
 * © Samk
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.kongamusic.home

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import moe.kongamusic.constants.DisableBlurKey
import moe.kongamusic.constants.MinimalHomeModeKey
import moe.kongamusic.constants.QuickPicks
import moe.kongamusic.constants.QuickPicksKey
import moe.kongamusic.constants.QuickPicksDisplayMode
import moe.kongamusic.constants.QuickPicksDisplayModeKey
import moe.kongamusic.constants.ShowHomeCategoryChipsKey
import moe.kongamusic.extensions.toEnum
import moe.kongamusic.utils.dataStore
import javax.inject.Inject

class HomeRepository
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) {
        val showCategoryChips: Flow<Boolean> =
            context.dataStore.data
                .map { preferences -> preferences[ShowHomeCategoryChipsKey] ?: true }
                .distinctUntilChanged()

        val quickPicksDisplayMode: Flow<QuickPicksDisplayMode> =
            context.dataStore.data
                .map { preferences ->
                    preferences[QuickPicksDisplayModeKey].toEnum(QuickPicksDisplayMode.CARD)
                }.distinctUntilChanged()

        val quickPicksMode: Flow<QuickPicks> =
            context.dataStore.data
                .map { preferences -> preferences[QuickPicksKey].toEnum(QuickPicks.QUICK_PICKS) }
                .distinctUntilChanged()

        val showTonalBackdrop: Flow<Boolean> =
            context.dataStore.data
                .map { preferences -> preferences[DisableBlurKey] != true }
                .distinctUntilChanged()

        val minimalHomeMode: Flow<Boolean> =
            context.dataStore.data
                .map { preferences -> preferences[MinimalHomeModeKey] ?: false }
                .distinctUntilChanged()
    }
