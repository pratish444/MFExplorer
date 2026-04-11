package com.mfexplorer.app.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "watchlist_funds",
    primaryKeys = ["watchlistId", "schemeCode"]
)
data class WatchlistFundEntity(
    val watchlistId: Long,
    val schemeCode: Int,
    val schemeName: String,
    val addedAt: Long = System.currentTimeMillis()
)
