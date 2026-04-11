package com.mfexplorer.app.data.remote

import com.mfexplorer.app.data.remote.dto.FundDetailDto
import com.mfexplorer.app.data.remote.dto.SearchResultDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MfApi {

    @GET("mf/search")
    suspend fun searchFunds(@Query("q") query: String): List<SearchResultDto>

    @GET("mf/{schemeCode}")
    suspend fun getFundDetail(@Path("schemeCode") schemeCode: Int): FundDetailDto

    companion object {
        const val BASE_URL = "https://api.mfapi.in/"
    }
}
