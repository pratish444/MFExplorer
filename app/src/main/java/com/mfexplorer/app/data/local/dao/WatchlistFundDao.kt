package com.mfexplorer.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mfexplorer.app.data.local.entity.WatchlistFundEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistFundDao {

    @Query("SELECT * FROM watchlist_funds WHERE watchlistId = :watchlistId ORDER BY addedAt DESC")
    fun getFundsForWatchlist(watchlistId: Long): Flow<List<WatchlistFundEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fund: WatchlistFundEntity)

    @Query("DELETE FROM watchlist_funds WHERE watchlistId = :watchlistId AND schemeCode = :schemeCode")
    suspend fun remove(watchlistId: Long, schemeCode: Int)

    @Query("SELECT COUNT(*) > 0 FROM watchlist_funds WHERE schemeCode = :schemeCode")
    fun isInAnyWatchlist(schemeCode: Int): Flow<Boolean>

    @Query("""
        SELECT w.* FROM watchlists w 
        INNER JOIN watchlist_funds wf ON w.id = wf.watchlistId 
        WHERE wf.schemeCode = :schemeCode
    """)
    fun getWatchlistsContainingFund(schemeCode: Int): Flow<List<com.mfexplorer.app.data.local.entity.WatchlistEntity>>

    @Query("SELECT COUNT(*) FROM watchlist_funds WHERE watchlistId = :watchlistId")
    fun getFundCount(watchlistId: Long): Flow<Int>

    @Query("DELETE FROM watchlist_funds WHERE watchlistId = :watchlistId")
    suspend fun deleteAllForWatchlist(watchlistId: Long)
}
