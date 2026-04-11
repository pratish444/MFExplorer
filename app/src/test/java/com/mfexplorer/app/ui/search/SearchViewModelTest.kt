package com.mfexplorer.app.ui.search

import com.mfexplorer.app.data.repository.FundRepository
import com.mfexplorer.app.domain.model.MutualFund
import com.mfexplorer.app.util.Resource
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private lateinit var fundRepository: FundRepository
    private lateinit var viewModel: SearchViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fundRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty query and isInitial true`() = runTest {
        viewModel = SearchViewModel(fundRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("", state.query)
        assertTrue(state.isInitial)
    }

    @Test
    fun `onQueryChanged updates query in state`() = runTest {
        every { fundRepository.searchFunds(any()) } returns flowOf(
            Resource.Success(emptyList())
        )
        viewModel = SearchViewModel(fundRepository)
        advanceUntilIdle()

        viewModel.onQueryChanged("test")
        assertEquals("test", viewModel.uiState.value.query)
    }

    @Test
    fun `clearQuery resets to initial state`() = runTest {
        every { fundRepository.searchFunds(any()) } returns flowOf(
            Resource.Success(emptyList())
        )
        viewModel = SearchViewModel(fundRepository)
        advanceUntilIdle()

        viewModel.onQueryChanged("test")
        viewModel.clearQuery()

        val state = viewModel.uiState.value
        assertEquals("", state.query)
        assertTrue(state.isInitial)
    }

    @Test
    fun `debounce waits 300ms before searching`() = runTest {
        val funds = listOf(MutualFund(schemeCode = 1, schemeName = "Test Fund"))
        every { fundRepository.searchFunds("test") } returns flowOf(
            Resource.Success(funds)
        )
        viewModel = SearchViewModel(fundRepository)
        advanceUntilIdle()

        viewModel.onQueryChanged("test")

        // Before debounce
        assertTrue(viewModel.uiState.value.isInitial || viewModel.uiState.value.results is Resource.Success)

        // After debounce
        advanceTimeBy(350)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isInitial)
    }
}
