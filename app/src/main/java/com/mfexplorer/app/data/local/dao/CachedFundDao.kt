package com.mfexplorer.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mfexplorer.app.data.local.entity.CachedFundEntity

@Dao
interface CachedFundDao {

    @Query("SELECT * FROM cached_funds WHERE category = :category ORDER BY schemeName ASC")
    suspend fun getByCategory(category: String): List<CachedFundEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(funds: List<CachedFundEntity>)

    @Query("DELETE FROM cached_funds WHERE category = :category")
    suspend fun deleteByCategory(category: String)

    @Query("DELETE FROM cached_funds")
    suspend fun clearAll()
}
