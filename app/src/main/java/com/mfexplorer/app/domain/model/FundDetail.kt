package com.mfexplorer.app.domain.model

data class FundDetail(
    val schemeCode: Int,
    val schemeName: String,
    val fundHouse: String,
    val schemeType: String,
    val schemeCategory: String,
    val isinGrowth: String?,
    val latestNav: String,
    val navHistory: List<NavEntry>
)
