package com.mfexplorer.app.ui.navigation

sealed class Screen(val route: String) {
    object Explore : Screen("explore")
    object Watchlist : Screen("watchlist")
    object Search : Screen("search")
    object AllFunds : Screen("all_funds")
    object ViewAll : Screen("view_all/{category}") {
        fun createRoute(category: String) = "view_all/$category"
    }
    object FundDetail : Screen("fund_detail/{schemeCode}") {
        fun createRoute(schemeCode: Int) = "fund_detail/$schemeCode"
    }
    object WatchlistDetail : Screen("watchlist_detail/{watchlistId}") {
        fun createRoute(watchlistId: Long) = "watchlist_detail/$watchlistId"
    }
}
