package com.mfexplorer.app.ui.viewall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mfexplorer.app.data.repository.FundRepository
import com.mfexplorer.app.domain.model.FundCategory
import com.mfexplorer.app.domain.model.MutualFund
import com.mfexplorer.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ViewAllUiState(
    val funds: Resource<List<MutualFund>> = Resource.Loading(),
    val displayedFunds: List<MutualFund> = emptyList(),
    val hasMore: Boolean = false,
    val pageSize: Int = 20
)

@HiltViewModel
class ViewAllViewModel @Inject constructor(
    private val fundRepository: FundRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ViewAllUiState())
    val uiState: StateFlow<ViewAllUiState> = _uiState.asStateFlow()

    private var allFunds: List<MutualFund> = emptyList()
    private var currentPage = 0

    fun loadCategory(category: FundCategory) {
        viewModelScope.launch {
            fundRepository.getFundsForCategory(category).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(funds = resource)
                    }
                    is Resource.Success -> {
                        allFunds = resource.data ?: emptyList()
                        currentPage = 0
                        val initial = allFunds.take(_uiState.value.pageSize)
                        _uiState.value = _uiState.value.copy(
                            funds = resource,
                            displayedFunds = initial,
                            hasMore = allFunds.size > initial.size
                        )
                    }
                    is Resource.Error -> {
                        allFunds = resource.data ?: emptyList()
                        currentPage = 0
                        val initial = allFunds.take(_uiState.value.pageSize)
                        _uiState.value = _uiState.value.copy(
                            funds = resource,
                            displayedFunds = initial,
                            hasMore = allFunds.size > initial.size
                        )
                    }
                }
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
