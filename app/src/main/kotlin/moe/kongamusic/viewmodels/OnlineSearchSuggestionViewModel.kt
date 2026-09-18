/*
 * kongamusic (2026)
 * © Samk
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.kongamusic.viewmodels

import android.content.Context
import kotlinx.coroutines.CancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import moe.kongamusic.aicontentfilter.FilterAiContentUseCase
import moe.kongamusic.aicontentfilter.LoadAiContentFilterPolicyUseCase
import moe.kongamusic.constants.HideExplicitKey
import moe.kongamusic.constants.HideVideoKey
import moe.kongamusic.db.MusicDatabase
import moe.kongamusic.db.entities.SearchHistory
import kotlinx.coroutines.flow.combine
import moe.kongamusic.innertube.YouTube
import moe.kongamusic.innertube.models.YTItem
import moe.kongamusic.innertube.models.filterExplicit
import moe.kongamusic.innertube.models.filterVideo
import moe.kongamusic.utils.dataStore
import moe.kongamusic.utils.get
import moe.kongamusic.constants.SearchProvider
import moe.kongamusic.applemusic.AppleMusicCatalog
import moe.kongamusic.applemusic.AppleMusicSearchItem
import moe.kongamusic.spotify.SpotifyLibraryRepository
import moe.kongamusic.spotify.SpotifySearchItem
import moe.kongamusic.spotify.toSearchItems
import moe.kongamusic.amazon.AmazonMusicCatalog
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class OnlineSearchSuggestionViewModel
    @Inject
    constructor(
        @ApplicationContext val context: Context,
        private val database: MusicDatabase,
        private val loadAiContentFilterPolicy: LoadAiContentFilterPolicyUseCase,
        private val filterAiContent: FilterAiContentUseCase,
        private val spotifyRepository: SpotifyLibraryRepository,
    ) : ViewModel() {
        private val query = MutableStateFlow("")
        private val provider = MutableStateFlow(SearchProvider.YOUTUBE)
        private val _viewState = MutableStateFlow(SearchSuggestionViewState())
        val viewState = _viewState.asStateFlow()

        init {
            viewModelScope.launch {
                query
                    .combine(provider) { query, provider -> query to provider }
                    .flatMapLatest { (query, provider) ->
                        if (query.isEmpty()) {
                            database.searchHistory().map { history ->
                                SearchSuggestionViewState(history = history)
                            }
                        } else if (provider == SearchProvider.APPLE_MUSIC) {
                            val appleMusicItems =
                                try {
                                    AppleMusicCatalog.searchTrackSuggestions(query, limit = 8)
                                } catch (error: CancellationException) {
                                    throw error
                                } catch (_: Throwable) {
                                    emptyList()
                                }
                            database.searchHistory(query).map { history ->
                                SearchSuggestionViewState(
                                    history = history.take(3),
                                    appleMusicItems = appleMusicItems,
                                )
                            }
                        } else if (provider == SearchProvider.AMAZON) {
                            // Anonymous catalogue search — no Amazon sign-in needed (see
                            // AmazonMusicCatalog's header); failures degrade to empty like the
                            // Apple Music branch above.
                            val amazonItems =
                                try {
                                    AmazonMusicCatalog.searchTrackSuggestions(query, limit = 8)
                                } catch (error: CancellationException) {
                                    throw error
                                } catch (_: Throwable) {
                                    emptyList()
                                }
                            database.searchHistory(query).map { history ->
                                SearchSuggestionViewState(
                                    history = history.take(3),
                                    amazonItems = amazonItems,
                                )
                            }
                        } else if (provider == SearchProvider.SPOTIFY) {
                            val spotifyItems =
                                try {
                                    spotifyRepository
                                        .search(query = query, limit = 8)
                                        .toSearchItems()
                                        .take(8)
                                } catch (error: CancellationException) {
                                    throw error
                                } catch (_: Throwable) {
                                    emptyList()
                                }
                            database.searchHistory(query).map { history ->
                                SearchSuggestionViewState(
                                    history = history.take(3),
                                    spotifyItems = spotifyItems,
                                )
                            }
                        } else {
                            val result = YouTube.searchSuggestions(query).getOrNull()
                            val aiContentFilterPolicy = loadAiContentFilterPolicy()
                            database
                                .searchHistory(query)
                                .map { it.take(3) }
                                .map { history ->
                                    SearchSuggestionViewState(
                                        history = history,
                                        suggestions =
                                            result
                                                ?.queries
                                                ?.filter { suggestion ->
                                                    history.none { it.query == suggestion }
                                                }.orEmpty(),
                                        items =
                                            filterAiContent(
                                                result
                                                    ?.recommendedItems
                                                    ?.filterExplicit(
                                                        context.dataStore.get(
                                                            HideExplicitKey,
                                                            false,
                                                        ),
                                                    )?.filterVideo(context.dataStore.get(HideVideoKey, false))
                                                    .orEmpty(),
                                                aiContentFilterPolicy,
                                            ),
                                    )
                                }
                        }
                    }.collect {
                        _viewState.value = it
                    }
            }
        }

        fun updateQuery(query: String) {
            this.query.value = query
        }

        fun updateProvider(provider: SearchProvider) {
            this.provider.value = provider
        }

        fun deleteHistory(history: SearchHistory) {
            database.query {
                delete(history)
            }
        }
    }

data class SearchSuggestionViewState(
    val history: List<SearchHistory> = emptyList(),
    val spotifyItems: List<SpotifySearchItem> = emptyList(),
    val appleMusicItems: List<AppleMusicSearchItem> = emptyList(),
    // Amazon items reuse the Apple Music search-item type — see AmazonMusicCatalog's header.
    val amazonItems: List<AppleMusicSearchItem.Track> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val items: List<YTItem> = emptyList(),
)
