package com.mfexplorer.app.ui.viewall

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.mfexplorer.app.domain.model.FundCategory
import com.mfexplorer.app.ui.components.ErrorState
import com.mfexplorer.app.ui.components.FundListItem
import com.mfexplorer.app.ui.components.ShimmerListItem
import com.mfexplorer.app.ui.navigation.Screen
import com.mfexplorer.app.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewAllScreen(
    navController: NavController,
    category: FundCategory,
    viewModel: ViewAllViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(category) {
        viewModel.loadCategory(category)
    }

    // Infinite scroll trigger
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItem >= uiState.displayedFunds.size - 5 && uiState.hasMore
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            viewModel.loadMore()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(category.displayName) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        when (val funds = uiState.funds) {
            is Resource.Loading -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(10) { ShimmerListItem() }
                }
            }
            is Resource.Error -> {
                if (uiState.displayedFunds.isEmpty()) {
                    ErrorState(
                        message = funds.message ?: "Failed to load",
                        onRetry = { viewModel.loadCategory(category) },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    FundList(
                        uiState = uiState,
                        listState = listState,
                        navController = navController
                    )
                }
            }
            is Resource.Success -> {
                FundList(
                    uiState = uiState,
                    listState = listState,
                    navController = navController
                )
            }
        }
    }
}

@Composable
private fun FundList(
    uiState: ViewAllUiState,
    listState: androidx.compose.foundation.lazy.LazyListState,
    navController: NavController
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = uiState.displayedFunds,
            key = { it.schemeCode }
        ) { fund ->
            FundListItem(
                fund = fund,
                onClick = {
                    navController.navigate(Screen.FundDetail.createRoute(fund.schemeCode))
                }
            )
        }

        if (uiState.hasMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
