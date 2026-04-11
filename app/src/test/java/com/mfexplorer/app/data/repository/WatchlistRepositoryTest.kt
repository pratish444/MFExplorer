package com.mfexplorer.app.data.repository

import com.mfexplorer.app.data.local.dao.WatchlistDao
import com.mfexplorer.app.data.local.dao.WatchlistFundDao
import com.mfexplorer.app.data.local.entity.WatchlistEntity
import com.mfexplorer.app.data.local.entity.WatchlistFundEntity
import com.mfexplorer.app.domain.model.MutualFund
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WatchlistRepositoryTest {

    private lateinit var watchlistDao: WatchlistDao
    private lateinit var watchlistFundDao: WatchlistFundDao
    private lateinit var repository: WatchlistRepository

    @Before
    fun setup() {
        watchlistDao = mockk(relaxed = true)
        watchlistFundDao = mockk(relaxed = true)
        repository = WatchlistRepository(watchlistDao, watchlistFundDao)
    }

    @Test
    fun `createWatchlist inserts and returns id`() = runTest {
        // Given
        coEvery { watchlistDao.insert(any()) } returns 1L

        // When
        val id = repository.createWatchlist("Retirement")

        // Then
        assertEquals(1L, id)
        coVerify { watchlistDao.insert(match { it.name == "Retirement" }) }
    }

    @Test
    fun `getAllWatchlists returns mapped domain models`() = runTest {
        // Given
        every { watchlistDao.getAllWatchlists() } returns flowOf(
            listOf(
                WatchlistEntity(id = 1, name = "Retirement", createdAt = 1000L),
                WatchlistEntity(id = 2, name = "Tax Savers", createdAt = 2000L)
            )
        )

        // When
        val watchlists = repository.getAllWatchlists().first()

        // Then
        assertEquals(2, watchlists.size)
        assertEquals("Retirement", watchlists[0].name)
        assertEquals("Tax Savers", watchlists[1].name)
    }

    @Test
    fun `addFundToWatchlist inserts fund entity`() = runTest {
        // Given
        val fund = MutualFund(schemeCode = 100711, schemeName = "UTI Index Fund")
        val slot = slot<WatchlistFundEntity>()
        coEvery { watchlistFundDao.insert(capture(slot)) } returns Unit

        // When
        repository.addFundToWatchlist(1L, fund)

        // Then
        assertEquals(1L, slot.captured.watchlistId)
        assertEquals(100711, slot.captured.schemeCode)
        assertEquals("UTI Index Fund", slot.captured.schemeName)
    }

    @Test
    fun `removeFundFromWatchlist calls dao remove`() = runTest {
        // When
        repository.removeFundFromWatchlist(1L, 100711)

        // Then
        coVerify { watchlistFundDao.remove(1L, 100711) }
    }

    @Test
    fun `deleteWatchlist removes funds first then watchlist`() = runTest {
        // When
        repository.deleteWatchlist(1L)

        // Then
        coVerify(ordering = io.mockk.Ordering.ORDERED) {
            watchlistFundDao.deleteAllForWatchlist(1L)
            watchlistDao.deleteById(1L)
        }
    }

    @Test
    fun `isInAnyWatchlist delegates to dao`() = runTest {
        // Given
        every { watchlistFundDao.isInAnyWatchlist(100711) } returns flowOf(true)

        // When
        val result = repository.isInAnyWatchlist(100711).first()

        // Then
        assertTrue(result)
    }

    @Test
    fun `getFundsInWatchlist maps entities to domain models`() = runTest {
        // Given
        every { watchlistFundDao.getFundsForWatchlist(1L) } returns flowOf(
            listOf(
                WatchlistFundEntity(watchlistId = 1L, schemeCode = 100711, schemeName = "UTI Index Fund"),
                WatchlistFundEntity(watchlistId = 1L, schemeCode = 100712, schemeName = "HDFC Index Fund")
            )
        )

        // When
        val funds = repository.getFundsInWatchlist(1L).first()

        // Then
        assertEquals(2, funds.size)
        assertEquals(100711, funds[0].schemeCode)
        assertEquals(100712, funds[1].schemeCode)
    }
}
