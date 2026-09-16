/*
 * kongamusic (2026)
 * © Samk
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.kongamusic.viewmodels

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import moe.kongamusic.aicontentfilter.FilterAiContentUseCase
import moe.kongamusic.aicontentfilter.LoadAiContentFilterPolicyUseCase
import moe.kongamusic.constants.HideExplicitKey
import moe.kongamusic.constants.HideVideoKey
import moe.kongamusic.db.MusicDatabase
import moe.kongamusic.extensions.filterBlockedArtists
import moe.kongamusic.innertube.YouTube
import moe.kongamusic.innertube.pages.BrowseResult
import moe.kongamusic.utils.dataStore
import moe.kongamusic.utils.get
import moe.kongamusic.utils.reportException
import javax.inject.Inject

@HiltViewModel
class YouTubeBrowseViewModel
    @Inject
    constructor(
        @ApplicationContext val context: Context,
        private val database: MusicDatabase,
        savedStateHandle: SavedStateHandle,
        private val loadAiContentFilterPolicy: LoadAiContentFilterPolicyUseCase,
        private val filterAiContent: FilterAiContentUseCase,
    ) : ViewModel() {
        private val browseId = savedStateHandle.get<String>("browseId")!!
        private val params = savedStateHandle.get<String>("params")

        val result = MutableStateFlow<BrowseResult?>(null)

        init {
            viewModelScope.launch {
                YouTube
                    .browse(browseId, params)
                    .onSuccess {
                        val hideVideo = context.dataStore.get(HideVideoKey, false)
                        val aiContentFilterPolicy = loadAiContentFilterPolicy()
                        val contentFilteredResult =
                            it
                                .filterExplicit(context.dataStore.get(HideExplicitKey, false))
                                .filterVideo(hideVideo)
                                .filterBlockedArtists(database.getBlockedArtistIds().toSet())
                        result.value =
                            contentFilteredResult.copy(
                                items =
                                    contentFilteredResult.items.mapNotNull { section ->
                                        section
                                            .copy(items = filterAiContent(section.items, aiContentFilterPolicy))
                                            .takeIf { filteredSection -> filteredSection.items.isNotEmpty() }
                                    },
                            )
                    }.onFailure {
                        reportException(it)
                    }
            }
        }
    }
