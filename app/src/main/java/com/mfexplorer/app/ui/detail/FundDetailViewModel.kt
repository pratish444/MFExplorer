package com.mfexplorer.app.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mfexplorer.app.data.repository.FundRepository
import com.mfexplorer.app.data.repository.WatchlistRepository
import com.mfexplorer.app.domain.model.FundDetail
import com.mfexplorer.app.domain.model.MutualFund
import com.mfexplorer.app.domain.model.Watchlist
import com.mfexplorer.app.ui.components.ChartPeriod
import com.mfexplorer.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FundDetailUiState(
    val fundDetail: Resource<FundDetail> = Resource.Loading(),
    val selectedPeriod: ChartPeriod = ChartPeriod.ONE_YEAR
)

@HiltViewModel
class FundDetailViewModel @Inject constructor(
    private val fundRepository: FundRepository,
    private val watchlistRepository: WatchlistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FundDetailUiState())
    val uiState: StateFlow<FundDetailUiState> = _uiState.asStateFlow()

    private val _isInWatchlist = MutableStateFlow(false)
    val isInWatchlist: StateFlow<Boolean> = _isInWatchlist.asStateFlow()

    private val _allWatchlists = MutableStateFlow<List<Watchlist>>(emptyList())
    val allWatchlists: StateFlow<List<Watchlist>> = _allWatchlists.asStateFlow()

    private val _watchlistsContainingFund = MutableStateFlow<List<Watchlist>>(emptyList())
    val watchlistsContainingFund: StateFlow<List<Watchlist>> = _watchlistsContainingFund.asStateFlow()

    private var currentSchemeCode: Int = 0

    fun loadFundDetail(schemeCode: Int) {
        currentSchemeCode = schemeCode
        viewModelScope.launch {
            fundRepository.getFundDetail(schemeCode).collect { resource ->
                _uiState.value = _uiState.value.copy(fundDetail = resource)
            }
        }
        viewModelScope.launch {
            watchlistRepository.isInAnyWatchlist(schemeCode).collect { inWatchlist ->
                _isInWatchlist.value = inWatchlist
            }
        }
        viewModelScope.launch {
            watchlistRepository.getAllWatchlists().collect { watchlists ->
                _allWatchlists.value = watchlists
            }
        }
        viewModelScope.launch {
            watchlistRepository.getWatchlistsContainingFund(schemeCode).collect { watchlists ->
                _watchlistsContainingFund.value = watchlists
            }
        }
    }

    fun selectPeriod(period: ChartPeriod) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
    }

    fun addToWatchlist(watchlistId: Long) {
        val detail = (_uiState.value.fundDetail as? Resource.Success)?.data ?: return
        viewModelScope.launch {
            watchlistRepository.addFundToWatchlist(
                watchlistId = watchlistId,
                fund = MutualFund(
                    schemeCode = detail.schemeCode,
                    schemeName = detail.schemeName,
                    latestNav = detail.latestNav,
                    fundHouse = detail.fundHouse
                )
            )
        }
    }

    fun removeFromWatchlist(watchlistId: Long) {
        viewModelScope.launch {
            watchlistRepository.removeFundFromWatchlist(watchlistId, currentSchemeCode)
        }
    }

    fun createWatchlistAndAdd(name: String) {
        val detail = (_uiState.value.fundDetail as? Resource.Success)?.data ?: return
        viewModelScope.launch {
            val watchlistId = watchlistRepository.createWatchlist(name)
            watchlistRepository.addFundToWatchlist(
                watchlistId = watchlistId,
                fund = MutualFund(
                    schemeCode = detail.schemeCode,
                    schemeName = detail.schemeName,
                    latestNav = detail.latestNav,
                    fundHouse = detail.fundHouse
                )
            )
        }
    }
}
