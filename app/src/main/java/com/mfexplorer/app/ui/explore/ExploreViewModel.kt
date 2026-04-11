package com.mfexplorer.app.ui.explore

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

data class ExploreUiState(
    val categories: Map<FundCategory, Resource<List<MutualFund>>> = FundCategory.entries.associateWith {
        Resource.Loading()
    }
)

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val fundRepository: FundRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    init {
        loadExploreData()
    }

    fun loadExploreData() {
        viewModelScope.launch {
            _uiState.value = ExploreUiState() // Reset to loading
            fundRepository.getExploreCategories().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        val data = resource.data
                        if (data != null) {
                            _uiState.value = ExploreUiState(
                                categories = FundCategory.entries.associateWith { category ->
                                    val funds = data[category]
                                    if (funds != null) Resource.Success(funds)
                                    else Resource.Loading()
                                }
                            )
                        }
                    }
                    is Resource.Success -> {
                        val data = resource.data ?: emptyMap()
                        _uiState.value = ExploreUiState(
                            categories = FundCategory.entries.associateWith { category ->
                                Resource.Success(data[category] ?: emptyList())
                            }
                        )
                    }
                    is Resource.Error -> {
                        val data = resource.data
                        _uiState.value = ExploreUiState(
                            categories = FundCategory.entries.associateWith { category ->
                                val funds = data?.get(category)
                                if (funds != null) Resource.Error(resource.message ?: "", funds)
                                else Resource.Error(resource.message ?: "Failed to load")
                            }
                        )
                    }
                }
            }
        }
    }
}
