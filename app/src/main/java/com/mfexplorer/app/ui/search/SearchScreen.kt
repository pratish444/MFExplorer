package com.mfexplorer.app.ui.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.mfexplorer.app.ui.components.EmptyState
import com.mfexplorer.app.ui.components.ErrorState
import com.mfexplorer.app.ui.components.FundListItem
import com.mfexplorer.app.ui.components.ShimmerListItem
import com.mfexplorer.app.ui.navigation.Screen
import com.mfexplorer.app.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Search") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        // Search field
        OutlinedTextField(
            value = uiState.query,
            onValueChange = viewModel::onQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .focusRequester(focusRequester),
            placeholder = {
                Text(
                    "Search mutual funds...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (uiState.query.isNotEmpty()) {
                    IconButton(onClick = viewModel::clearQuery) {
                        Icon(Icons.Filled.Clear, "Clear")
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                keyboardController?.hide()
            }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            shape = MaterialTheme.shapes.medium
        )

        // Results
        when {
            uiState.isInitial -> {
                EmptyState(
                    title = "Search Funds",
                    description = "Type a fund name, AMC, or keyword to search across all mutual funds.",
                    icon = Icons.Outlined.Search,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                when (val results = uiState.results) {
                    is Resource.Loading -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(5) {
                                ShimmerListItem()
                            }
                        }
                    }
                    is Resource.Success -> {
                        val funds = results.data ?: emptyList()
                        if (funds.isEmpty()) {
                            EmptyState(
                                title = "No Results",
                                description = "No funds found for \"${uiState.query}\". Try a different keyword.",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(
                                    items = funds,
                                    key = { it.schemeCode }
                                ) { fund ->
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
                    is Resource.Error -> {
                        ErrorState(
                            message = results.message ?: "Search failed",
                            onRetry = { viewModel.onQueryChanged(uiState.query) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
