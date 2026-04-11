package com.mfexplorer.app.ui.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.mfexplorer.app.domain.model.FundCategory
import com.mfexplorer.app.domain.model.MutualFund
import com.mfexplorer.app.ui.components.ErrorState
import com.mfexplorer.app.ui.components.FundCard
import com.mfexplorer.app.ui.components.ShimmerFundCard
import com.mfexplorer.app.ui.navigation.Screen
import com.mfexplorer.app.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    navController: NavController,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = "MF Explorer",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                TextButton(onClick = { navController.navigate(Screen.AllFunds.route) }) {
                    Text(
                        text = "View All →",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onToggleTheme) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                        contentDescription = "Toggle theme"
                    )
                }
                IconButton(onClick = { navController.navigate(Screen.Search.route) }) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            FundCategory.entries.forEach { category ->
                item(key = category.name) {
                    CategorySection(
                        category = category,
                        resource = uiState.categories[category] ?: Resource.Loading(),
                        onViewAll = {
                            navController.navigate(Screen.ViewAll.createRoute(category.name))
                        },
                        onFundClick = { fund ->
                            navController.navigate(Screen.FundDetail.createRoute(fund.schemeCode))
                        },
                        onRetry = { viewModel.loadExploreData() }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategorySection(
    category: FundCategory,
    resource: Resource<List<MutualFund>>,
    onViewAll: () -> Unit,
    onFundClick: (MutualFund) -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        // Category header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            TextButton(onClick = onViewAll) {
                Text(
                    text = "View All →",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (resource) {
            is Resource.Loading -> {
                val cachedFunds = resource.data
                if (cachedFunds != null && cachedFunds.isNotEmpty()) {
                    FundCardGrid(funds = cachedFunds.take(4), onFundClick = onFundClick)
                } else {
                    // Shimmer loading
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(4) {
                            ShimmerFundCard(
                                modifier = Modifier.width(160.dp).height(120.dp)
                            )
                        }
                    }
                }
            }
            is Resource.Success -> {
                val funds = resource.data ?: emptyList()
                if (funds.isNotEmpty()) {
                    FundCardGrid(funds = funds.take(4), onFundClick = onFundClick)
                }
            }
            is Resource.Error -> {
                val cachedFunds = resource.data
                if (cachedFunds != null && cachedFunds.isNotEmpty()) {
                    FundCardGrid(funds = cachedFunds.take(4), onFundClick = onFundClick)
                } else {
                    ErrorState(
                        message = resource.message ?: "Failed to load",
                        onRetry = onRetry,
                        modifier = Modifier.height(150.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FundCardGrid(
    funds: List<MutualFund>,
    onFundClick: (MutualFund) -> Unit
) {
    // Display as 2x2 grid using two rows of LazyRow
    val rows = funds.chunked(2)
    Column(
        modifier = Modifier.padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rows.forEach { rowFunds ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowFunds.forEach { fund ->
                    FundCard(
                        fund = fund,
                        onClick = { onFundClick(fund) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill empty space if odd number
                if (rowFunds.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
