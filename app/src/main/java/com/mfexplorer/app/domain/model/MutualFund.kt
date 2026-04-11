package com.mfexplorer.app.domain.model

data class MutualFund(
    val schemeCode: Int,
    val schemeName: String,
    val latestNav: String? = null,
    val fundHouse: String? = null,
    val schemeType: String? = null
)
