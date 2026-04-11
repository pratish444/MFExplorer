package com.mfexplorer.app.ui.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mfexplorer.app.data.repository.WatchlistRepository
import com.mfexplorer.app.domain.model.MutualFund
import com.mfexplorer.app.domain.model.Watchlist
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WatchlistDetailUiState(
    val watchlist: Watchlist? = null,
    val funds: List<MutualFund> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class WatchlistDetailViewModel @Inject constructor(
    private val watchlistRepository: WatchlistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WatchlistDetailUiState())
    val uiState: StateFlow<WatchlistDetailUiState> = _uiState.asStateFlow()

    fun loadWatchlist(watchlistId: Long) {
        viewModelScope.launch {
            val watchlist = watchlistRepository.getWatchlistById(watchlistId)
            _uiState.value = _uiState.value.copy(watchlist = watchlist)
        }
        viewModelScope.launch {
            watchlistRepository.getFundsInWatchlist(watchlistId).collect { funds ->
                _uiState.value = _uiState.value.copy(
                    funds = funds,
                    isLoading = false
                )
            }
        }
    }

    fun removeFund(watchlistId: Long, schemeCode: Int) {
        viewModelScope.launch {
            watchlistRepository.removeFundFromWatchlist(watchlistId, schemeCode)
        }
    }
}
