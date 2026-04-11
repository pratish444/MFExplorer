package com.mfexplorer.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FundDetailDto(
    @SerializedName("meta") val meta: MetaDto,
    @SerializedName("data") val data: List<NavDataDto>,
    @SerializedName("status") val status: String
)

data class MetaDto(
    @SerializedName("fund_house") val fundHouse: String,
    @SerializedName("scheme_type") val schemeType: String,
    @SerializedName("scheme_category") val schemeCategory: String,
    @SerializedName("scheme_code") val schemeCode: Int,
    @SerializedName("scheme_name") val schemeName: String,
    @SerializedName("isin_growth") val isinGrowth: String?,
    @SerializedName("isin_div_reinvestment") val isinDivReinvestment: String?
)

data class NavDataDto(
    @SerializedName("date") val date: String,
    @SerializedName("nav") val nav: String
)
