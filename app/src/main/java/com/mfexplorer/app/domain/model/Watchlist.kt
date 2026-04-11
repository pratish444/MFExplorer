package com.mfexplorer.app.domain.model

data class Watchlist(
    val id: Long,
    val name: String,
    val fundCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
