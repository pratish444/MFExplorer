package com.mfexplorer.app.ui.watchlist

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.mfexplorer.app.ui.components.EmptyState
import com.mfexplorer.app.ui.components.FundListItem
import com.mfexplorer.app.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistDetailScreen(
    navController: NavController,
    watchlistId: Long,
    viewModel: WatchlistDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(watchlistId) {
        viewModel.loadWatchlist(watchlistId)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(uiState.watchlist?.name ?: "Watchlist")
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        if (uiState.funds.isEmpty() && !uiState.isLoading) {
            EmptyState(
                title = "No Funds Added Yet",
                description = "Explore the market to save funds into this watchlist.",
                actionLabel = "Explore Funds",
                onAction = {
                    navController.navigate(Screen.Explore.route) {
                        popUpTo(Screen.Explore.route) { inclusive = true }
                    }
                },
                icon = Icons.Outlined.Explore,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .animateContentSize()
            ) {
                items(
                    items = uiState.funds,
                    key = { it.schemeCode }
                ) { fund ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                viewModel.removeFund(watchlistId, fund.schemeCode)
                                true
                            } else false
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            // Background is handled by the default red swipe
                        },
                        enableDismissFromStartToEnd = false
                    ) {
                        FundListItem(
                            fund = fund,
                            onClick = {
                                navController.navigate(
                                    Screen.FundDetail.createRoute(fund.schemeCode)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
