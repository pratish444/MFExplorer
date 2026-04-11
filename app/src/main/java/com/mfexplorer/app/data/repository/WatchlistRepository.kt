package com.mfexplorer.app.data.repository

import com.mfexplorer.app.data.local.dao.WatchlistDao
import com.mfexplorer.app.data.local.dao.WatchlistFundDao
import com.mfexplorer.app.data.local.entity.WatchlistEntity
import com.mfexplorer.app.data.local.entity.WatchlistFundEntity
import com.mfexplorer.app.domain.model.MutualFund
import com.mfexplorer.app.domain.model.Watchlist
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchlistRepository @Inject constructor(
    private val watchlistDao: WatchlistDao,
    private val watchlistFundDao: WatchlistFundDao
) {

    fun getAllWatchlists(): Flow<List<Watchlist>> {
        return watchlistDao.getAllWatchlists().map { entities ->
            entities.map { entity ->
                Watchlist(
                    id = entity.id,
                    name = entity.name,
                    createdAt = entity.createdAt
                )
            }
        }
    }

    fun getAllWatchlistsWithCounts(): Flow<List<Watchlist>> {
        return watchlistDao.getAllWatchlists().map { entities ->
            entities.map { entity ->
                entity // will enrich with count below
            }
        }.combine(
            watchlistDao.getAllWatchlists().map { entities ->
                entities.map { it.id }
            }
        ) { entities, _ ->
            entities.map { entity ->
                Watchlist(
                    id = entity.id,
                    name = entity.name,
                    createdAt = entity.createdAt
                )
            }
        }
    }

    suspend fun createWatchlist(name: String): Long {
        return watchlistDao.insert(WatchlistEntity(name = name))
    }

    suspend fun deleteWatchlist(watchlistId: Long) {
        watchlistFundDao.deleteAllForWatchlist(watchlistId)
        watchlistDao.deleteById(watchlistId)
    }

    fun getFundsInWatchlist(watchlistId: Long): Flow<List<MutualFund>> {
        return watchlistFundDao.getFundsForWatchlist(watchlistId).map { entities ->
            entities.map { entity ->
                MutualFund(
                    schemeCode = entity.schemeCode,
                    schemeName = entity.schemeName
                )
            }
        }
    }

    suspend fun addFundToWatchlist(watchlistId: Long, fund: MutualFund) {
        watchlistFundDao.insert(
            WatchlistFundEntity(
                watchlistId = watchlistId,
                schemeCode = fund.schemeCode,
                schemeName = fund.schemeName
            )
        )
    }

    suspend fun removeFundFromWatchlist(watchlistId: Long, schemeCode: Int) {
        watchlistFundDao.remove(watchlistId, schemeCode)
    }

    fun isInAnyWatchlist(schemeCode: Int): Flow<Boolean> {
        return watchlistFundDao.isInAnyWatchlist(schemeCode)
    }

    fun getWatchlistsContainingFund(schemeCode: Int): Flow<List<Watchlist>> {
        return watchlistFundDao.getWatchlistsContainingFund(schemeCode).map { entities ->
            entities.map { entity ->
                Watchlist(
                    id = entity.id,
                    name = entity.name,
                    createdAt = entity.createdAt
                )
            }
        }
    }

    fun getFundCount(watchlistId: Long): Flow<Int> {
        return watchlistFundDao.getFundCount(watchlistId)
    }

    suspend fun getWatchlistById(id: Long): Watchlist? {
        return watchlistDao.getById(id)?.let {
            Watchlist(id = it.id, name = it.name, createdAt = it.createdAt)
        }
    }
}
