package com.mfexplorer.app.di

import android.content.Context
import androidx.room.Room
import com.mfexplorer.app.data.local.MfDatabase
import com.mfexplorer.app.data.local.dao.CachedFundDao
import com.mfexplorer.app.data.local.dao.WatchlistDao
import com.mfexplorer.app.data.local.dao.WatchlistFundDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MfDatabase {
        return Room.databaseBuilder(
            context,
            MfDatabase::class.java,
            MfDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideCachedFundDao(db: MfDatabase): CachedFundDao = db.cachedFundDao()

    @Provides
    @Singleton
    fun provideWatchlistDao(db: MfDatabase): WatchlistDao = db.watchlistDao()

    @Provides
    @Singleton
    fun provideWatchlistFundDao(db: MfDatabase): WatchlistFundDao = db.watchlistFundDao()
}
