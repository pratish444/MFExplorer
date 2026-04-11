package com.mfexplorer.app.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {

    private val apiDateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH)

    fun parseApiDate(dateString: String): LocalDate? {
        return try {
            LocalDate.parse(dateString, apiDateFormat)
        } catch (e: Exception) {
            null
        }
    }

    fun formatNav(nav: String?): String {
        if (nav == null) return "—"
        return try {
            val value = nav.toDouble()
            "₹%.2f".format(value)
        } catch (e: Exception) {
            "₹$nav"
        }
    }

    fun formatNavChange(currentNav: Double, previousNav: Double): Pair<String, Boolean> {
        val change = currentNav - previousNav
        val percentChange = if (previousNav != 0.0) (change / previousNav) * 100 else 0.0
        val isPositive = change >= 0
        val formatted = "${if (isPositive) "+" else ""}%.2f (%.2f%%)".format(change, percentChange)
        return Pair(formatted, isPositive)
    }
}
