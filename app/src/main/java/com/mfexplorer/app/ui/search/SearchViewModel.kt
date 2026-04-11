package com.mfexplorer.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mfexplorer.app.data.repository.FundRepository
import com.mfexplorer.app.domain.model.MutualFund
import com.mfexplorer.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: Resource<List<MutualFund>> = Resource.Success(emptyList()),
    val isInitial: Boolean = true
)

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val fundRepository: FundRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        viewModelScope.launch {
            _searchQuery
                .debounce(300) // 300ms debounce
                .distinctUntilChanged()
                .filter { it.isNotBlank() }
                .collectLatest { query ->
                    fundRepository.searchFunds(query).collect { resource ->
                        _uiState.value = _uiState.value.copy(
                            results = resource,
                            isInitial = false
                        )
                    }
                }
        }
    }

    fun onQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        _searchQuery.value = query

        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                results = Resource.Success(emptyList()),
                isInitial = true
            )
        }
    }

    fun clearQuery() {
        onQueryChanged("")
    }
}
