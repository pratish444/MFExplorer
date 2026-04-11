package com.mfexplorer.app.data.repository

import com.mfexplorer.app.data.local.dao.CachedFundDao
import com.mfexplorer.app.data.local.entity.CachedFundEntity
import com.mfexplorer.app.data.remote.MfApi
import com.mfexplorer.app.domain.model.FundCategory
import com.mfexplorer.app.domain.model.FundDetail
import com.mfexplorer.app.domain.model.MutualFund
import com.mfexplorer.app.domain.model.NavEntry
import com.mfexplorer.app.util.DateUtils
import com.mfexplorer.app.util.Resource
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FundRepository @Inject constructor(
    private val api: MfApi,
    private val cachedFundDao: CachedFundDao
) {

    fun getExploreCategories(): Flow<Resource<Map<FundCategory, List<MutualFund>>>> = flow {
        emit(Resource.Loading())

        // Try to load cached data first
        val cachedData = mutableMapOf<FundCategory, List<MutualFund>>()
        FundCategory.entries.forEach { category ->
            val cached = cachedFundDao.getByCategory(category.name)
            if (cached.isNotEmpty()) {
                cachedData[category] = cached.map { it.toDomain() }
            }
        }
        if (cachedData.isNotEmpty()) {
            emit(Resource.Loading(cachedData))
        }

        // Fetch fresh data from API
        try {
            val result = mutableMapOf<FundCategory, List<MutualFund>>()
            coroutineScope {
                val jobs = FundCategory.entries.map { category ->
                    async {
                        try {
                            val searchResults = api.searchFunds(category.searchQuery)
                            val funds = searchResults.map { dto ->
                                MutualFund(
                                    schemeCode = dto.schemeCode,
                                    schemeName = dto.schemeName
                                )
                            }
                            // Cache the results
                            cachedFundDao.deleteByCategory(category.name)
                            cachedFundDao.insertAll(funds.map { fund ->
                                CachedFundEntity(
                                    id = "${category.name}_${fund.schemeCode}",
                                    schemeCode = fund.schemeCode,
                                    schemeName = fund.schemeName,
                                    category = category.name
                                )
                            })
                            category to funds
                        } catch (e: Exception) {
                            // On error, use cached data for this category
                            val cached = cachedFundDao.getByCategory(category.name)
                            category to cached.map { it.toDomain() }
                        }
                    }
                }
                jobs.awaitAll().forEach { (category, funds) ->
                    result[category] = funds
                }
            }

            // Fetch NAV for top 4 funds per category
            val detailedResult = mutableMapOf<FundCategory, List<MutualFund>>()
            coroutineScope {
                val categoryJobs = result.map { (category, funds) ->
                    async {
                        val topFunds = funds.take(4)
                        val detailedFunds = topFunds.map { fund ->
                            async {
                                try {
                                    val detail = api.getFundDetail(fund.schemeCode)
                                    val latestNav = detail.data.firstOrNull()?.nav
                                    fund.copy(
                                        latestNav = latestNav,
                                        fundHouse = detail.meta.fundHouse,
                                        schemeType = detail.meta.schemeType
                                    )
                                } catch (e: Exception) {
                                    fund
                                }
                            }
                        }.awaitAll()
                        // Update cache with NAV data
                        detailedFunds.forEach { fund ->
                            if (fund.latestNav != null) {
                                cachedFundDao.insertAll(listOf(
                                    CachedFundEntity(
                                        id = "${category.name}_${fund.schemeCode}",
                                        schemeCode = fund.schemeCode,
                                        schemeName = fund.schemeName,
                                        category = category.name,
                                        latestNav = fund.latestNav,
                                        fundHouse = fund.fundHouse,
                                        schemeType = fund.schemeType
                                    )
                                ))
                            }
                        }
                        // Keep remaining funds without NAV
                        category to (detailedFunds + funds.drop(4))
                    }
                }
                categoryJobs.awaitAll().forEach { (category, funds) ->
                    detailedResult[category] = funds
                }
            }
            emit(Resource.Success(detailedResult))
        } catch (e: Exception) {
            // Return cached data on complete failure
            if (cachedData.isNotEmpty()) {
                emit(Resource.Error("Network error. Showing cached data.", cachedData))
            } else {
                emit(Resource.Error(e.message ?: "Unknown error occurred"))
            }
        }
    }

    fun getFundsForCategory(category: FundCategory): Flow<Resource<List<MutualFund>>> = flow {
        emit(Resource.Loading())
        try {
            val searchResults = api.searchFunds(category.searchQuery)
            val funds = searchResults.map { dto ->
                MutualFund(
                    schemeCode = dto.schemeCode,
                    schemeName = dto.schemeName
                )
            }
            emit(Resource.Success(funds))
        } catch (e: Exception) {
            val cached = cachedFundDao.getByCategory(category.name)
            if (cached.isNotEmpty()) {
                emit(Resource.Error("Network error. Showing cached data.", cached.map { it.toDomain() }))
            } else {
                emit(Resource.Error(e.message ?: "Failed to load funds"))
            }
        }
    }

    fun getFundDetail(schemeCode: Int): Flow<Resource<FundDetail>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getFundDetail(schemeCode)
            val navHistory = response.data.mapNotNull { navData ->
                val date = DateUtils.parseApiDate(navData.date)
                val nav = navData.nav.toDoubleOrNull()
                if (date != null && nav != null && nav > 0.0) {
                    NavEntry(date = date, nav = nav)
                } else null
            }.sortedBy { it.date }

            val detail = FundDetail(
                schemeCode = response.meta.schemeCode,
                schemeName = response.meta.schemeName,
                fundHouse = response.meta.fundHouse,
                schemeType = response.meta.schemeType,
                schemeCategory = response.meta.schemeCategory,
                isinGrowth = response.meta.isinGrowth,
                latestNav = response.data.firstOrNull()?.nav ?: "0",
                navHistory = navHistory
            )
            emit(Resource.Success(detail))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load fund details"))
        }
    }

    fun searchFunds(query: String): Flow<Resource<List<MutualFund>>> = flow {
        emit(Resource.Loading())
        try {
            val results = api.searchFunds(query)
            val funds = results.map { dto ->
                MutualFund(
                    schemeCode = dto.schemeCode,
                    schemeName = dto.schemeName
                )
            }
            emit(Resource.Success(funds))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Search failed"))
        }
    }

    private fun CachedFundEntity.toDomain() = MutualFund(
        schemeCode = schemeCode,
        schemeName = schemeName,
        latestNav = latestNav,
        fundHouse = fundHouse,
        schemeType = schemeType
    )
}
