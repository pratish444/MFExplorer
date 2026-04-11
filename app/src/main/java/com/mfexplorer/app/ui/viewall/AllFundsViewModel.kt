package com.mfexplorer.app.ui.viewall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mfexplorer.app.data.remote.MfApi
import com.mfexplorer.app.domain.model.FundCategory
import com.mfexplorer.app.domain.model.MutualFund
import com.mfexplorer.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AllFundsUiState(
    val funds: Resource<List<MutualFund>> = Resource.Loading(),
    val displayedFunds: List<MutualFund> = emptyList(),
    val hasMore: Boolean = false,
    val pageSize: Int = 20
)

@HiltViewModel
class AllFundsViewModel @Inject constructor(
    private val api: MfApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(AllFundsUiState())
    val uiState: StateFlow<AllFundsUiState> = _uiState.asStateFlow()

    private var allFunds: List<MutualFund> = emptyList()
    private var currentPage = 0

    init {
        loadAllFunds()
    }

    fun loadAllFunds() {
        viewModelScope.launch {
            _uiState.value = AllFundsUiState()
            try {
                val combined = coroutineScope {
                    FundCategory.entries.map { category ->
                        async {
                            try {
                                api.searchFunds(category.searchQuery).map { dto ->
                                    MutualFund(
                                        schemeCode = dto.schemeCode,
                                        schemeName = dto.schemeName
                                    )
                                }
                            } catch (e: Exception) {
                                emptyList()
                            }
                        }
                    }.awaitAll().flatten()
                }

                // Remove duplicates by schemeCode
                allFunds = combined.distinctBy { it.schemeCode }
                currentPage = 0
                val initial = allFunds.take(_uiState.value.pageSize)
                _uiState.value = _uiState.value.copy(
                    funds = Resource.Success(allFunds),
                    displayedFunds = initial,
                    hasMore = allFunds.size > initial.size
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    funds = Resource.Error(e.message ?: "Failed to load funds")
                )
            }
        }
    }

    fun loadMore() {
        currentPage++
        val endIndex = ((currentPage + 1) * _uiState.value.pageSize).coerceAtMost(allFunds.size)
        val displayed = allFunds.take(endIndex)
        _uiState.value = _uiState.value.copy(
            displayedFunds = displayed,
            hasMore = endIndex < allFunds.size
        )
    }
}
