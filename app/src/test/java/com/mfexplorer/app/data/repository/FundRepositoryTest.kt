package com.mfexplorer.app.data.repository

import com.mfexplorer.app.data.local.dao.CachedFundDao
import com.mfexplorer.app.data.local.entity.CachedFundEntity
import com.mfexplorer.app.data.remote.MfApi
import com.mfexplorer.app.data.remote.dto.FundDetailDto
import com.mfexplorer.app.data.remote.dto.MetaDto
import com.mfexplorer.app.data.remote.dto.NavDataDto
import com.mfexplorer.app.data.remote.dto.SearchResultDto
import com.mfexplorer.app.domain.model.FundCategory
import com.mfexplorer.app.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FundRepositoryTest {

    private lateinit var api: MfApi
    private lateinit var cachedFundDao: CachedFundDao
    private lateinit var repository: FundRepository

    @Before
    fun setup() {
        api = mockk()
        cachedFundDao = mockk(relaxed = true)
        repository = FundRepository(api, cachedFundDao)
    }

    @Test
    fun `searchFunds returns success with mapped funds`() = runTest {
        // Given
        val query = "index"
        val apiResults = listOf(
            SearchResultDto(schemeCode = 100711, schemeName = "UTI Index Fund"),
            SearchResultDto(schemeCode = 100712, schemeName = "HDFC Index Fund")
        )
        coEvery { api.searchFunds(query) } returns apiResults

        // When
        val results = repository.searchFunds(query).toList()

        // Then
        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Success)
        val funds = (results[1] as Resource.Success).data!!
        assertEquals(2, funds.size)
        assertEquals(100711, funds[0].schemeCode)
        assertEquals("UTI Index Fund", funds[0].schemeName)
    }

    @Test
    fun `searchFunds returns error on exception`() = runTest {
        // Given
        val query = "invalid"
        coEvery { api.searchFunds(query) } throws RuntimeException("Network error")

        // When
        val results = repository.searchFunds(query).toList()

        // Then
        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Error)
        assertEquals("Network error", (results[1] as Resource.Error).message)
    }

    @Test
    fun `getFundDetail returns mapped fund detail`() = runTest {
        // Given
        val schemeCode = 120503
        val apiResponse = FundDetailDto(
            meta = MetaDto(
                fundHouse = "Axis Mutual Fund",
                schemeType = "Open Ended Schemes",
                schemeCategory = "Equity Scheme - ELSS",
                schemeCode = 120503,
                schemeName = "Axis ELSS Tax Saver Fund",
                isinGrowth = "INF846K01EW2",
                isinDivReinvestment = null
            ),
            data = listOf(
                NavDataDto(date = "09-04-2026", nav = "102.55"),
                NavDataDto(date = "08-04-2026", nav = "103.28"),
                NavDataDto(date = "07-04-2026", nav = "99.39")
            ),
            status = "SUCCESS"
        )
        coEvery { api.getFundDetail(schemeCode) } returns apiResponse

        // When
        val results = repository.getFundDetail(schemeCode).toList()

        // Then
        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Success)
        val detail = (results[1] as Resource.Success).data!!
        assertEquals("Axis Mutual Fund", detail.fundHouse)
        assertEquals("102.55", detail.latestNav)
        assertEquals(3, detail.navHistory.size)
    }

    @Test
    fun `getFundsForCategory uses cache on API failure`() = runTest {
        // Given
        val category = FundCategory.INDEX
        coEvery { api.searchFunds(category.searchQuery) } throws RuntimeException("Offline")
        coEvery { cachedFundDao.getByCategory(category.name) } returns listOf(
            CachedFundEntity(
                id = "INDEX_100711",
                schemeCode = 100711,
                schemeName = "UTI Index Fund",
                category = "INDEX",
                latestNav = "150.00"
            )
        )

        // When
        val results = repository.getFundsForCategory(category).toList()

        // Then
        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Error)
        val cachedFunds = (results[1] as Resource.Error).data!!
        assertEquals(1, cachedFunds.size)
        assertEquals("UTI Index Fund", cachedFunds[0].schemeName)
    }
}
