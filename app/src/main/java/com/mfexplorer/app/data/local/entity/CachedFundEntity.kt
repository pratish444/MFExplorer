package com.mfexplorer.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_funds")
data class CachedFundEntity(
    @PrimaryKey val id: String, // category_schemeCode to allow same fund in multiple categories
    val schemeCode: Int,
    val schemeName: String,
    val category: String,
    val latestNav: String? = null,
    val fundHouse: String? = null,
    val schemeType: String? = null,
    val cachedAt: Long = System.currentTimeMillis()
)
