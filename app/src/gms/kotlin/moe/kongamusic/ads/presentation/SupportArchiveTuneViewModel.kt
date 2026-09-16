/*
 * kongamusic (2026)
 * © Samk
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.kongamusic.ads.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import moe.kongamusic.ads.domain.OpenSupportPageUseCase
import moe.kongamusic.ads.domain.SupportPageOpenResult
import javax.inject.Inject

internal sealed interface KongamusicSupportScreenState {
    @Immutable
    data object Loading : KongamusicSupportScreenState

    @Immutable
    data object Success : KongamusicSupportScreenState

    @Immutable
    data object Empty : KongamusicSupportScreenState

    @Immutable
    data class Error(
        val reason: KongamusicSupportError,
    ) : KongamusicSupportScreenState
}

internal enum class KongamusicSupportError {
    PageUnavailable,
}

internal enum class KongamusicSupportUiEvent {
    OpenFailed,
}

@HiltViewModel
internal class KongamusicSupportViewModel
    @Inject
    constructor(
        private val openSupportPage: OpenSupportPageUseCase,
    ) : ViewModel() {
        private val _screenState =
            MutableStateFlow<KongamusicSupportScreenState>(KongamusicSupportScreenState.Success)
        val screenState: StateFlow<KongamusicSupportScreenState> = _screenState.asStateFlow()

        private val eventChannel = Channel<KongamusicSupportUiEvent>(Channel.BUFFERED)
        val events = eventChannel.receiveAsFlow()

        fun onKongamusicSupportClick() {
            if (_screenState.value is KongamusicSupportScreenState.Loading) return
            _screenState.value = KongamusicSupportScreenState.Loading
            when (openSupportPage()) {
                SupportPageOpenResult.Opened -> {
                    _screenState.value = KongamusicSupportScreenState.Success
                }

                SupportPageOpenResult.Unavailable -> {
                    _screenState.value =
                        KongamusicSupportScreenState.Error(KongamusicSupportError.PageUnavailable)
                    eventChannel.trySend(KongamusicSupportUiEvent.OpenFailed)
                }
            }
        }
    }
