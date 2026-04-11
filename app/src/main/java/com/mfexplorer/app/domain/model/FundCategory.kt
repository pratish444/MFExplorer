package com.mfexplorer.app.domain.model

enum class FundCategory(
    val displayName: String,
    val searchQuery: String
) {
    INDEX("Index Funds", "index"),
    BLUECHIP("Bluechip Funds", "bluechip"),
    TAX_SAVER("Tax Saver (ELSS)", "tax saver"),
    LARGE_CAP("Large Cap Funds", "large cap");
}
