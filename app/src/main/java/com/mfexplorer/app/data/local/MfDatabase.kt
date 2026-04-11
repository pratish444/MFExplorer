package com.mfexplorer.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mfexplorer.app.data.local.dao.CachedFundDao
import com.mfexplorer.app.data.local.dao.WatchlistDao
import com.mfexplorer.app.data.local.dao.WatchlistFundDao
import com.mfexplorer.app.data.local.entity.CachedFundEntity
import com.mfexplorer.app.data.local.entity.WatchlistEntity
import com.mfexplorer.app.data.local.entity.WatchlistFundEntity

@Database(
    entities = [
        CachedFundEntity::class,
        WatchlistEntity::class,
        WatchlistFundEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MfDatabase : RoomDatabase() {

    abstract fun cachedFundDao(): CachedFundDao
    abstract fun watchlistDao(): WatchlistDao
    abstract fun watchlistFundDao(): WatchlistFundDao

    companion object {
        const val DATABASE_NAME = "mf_explorer_db"
    }
}
