package com.mfexplorer.app.ui.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mfexplorer.app.data.repository.WatchlistRepository
import com.mfexplorer.app.domain.model.MutualFund
import com.mfexplorer.app.domain.model.Watchlist
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WatchlistUiState(
    val watchlists: List<WatchlistWithCount> = emptyList(),
    val isLoading: Boolean = true
)

data class WatchlistWithCount(
    val watchlist: Watchlist,
    val fundCount: Int
)

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val watchlistRepository: WatchlistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WatchlistUiState())
    val uiState: StateFlow<WatchlistUiState> = _uiState.asStateFlow()

    init {
        loadWatchlists()
    }

    private fun loadWatchlists() {
        viewModelScope.launch {
            watchlistRepository.getAllWatchlists().collect { watchlists ->
                val withCounts = watchlists.map { watchlist ->
                    val count = watchlistRepository.getFundCount(watchlist.id).first()
                    WatchlistWithCount(watchlist = watchlist, fundCount = count)
                }
                _uiState.value = WatchlistUiState(
                    watchlists = withCounts,
                    isLoading = false
                )
            }
        }
    }

    fun createWatchlist(name: String) {
        viewModelScope.launch {
            watchlistRepository.createWatchlist(name)
        }
    }

    fun deleteWatchlist(watchlistId: Long) {
        viewModelScope.launch {
            watchlistRepository.deleteWatchlist(watchlistId)
        }
    }
}
