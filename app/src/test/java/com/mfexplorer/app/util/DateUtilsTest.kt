package com.mfexplorer.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class DateUtilsTest {

    @Test
    fun `parseApiDate parses valid date string`() {
        val result = DateUtils.parseApiDate("09-04-2026")
        assertNotNull(result)
        assertEquals(2026, result!!.year)
        assertEquals(4, result.monthValue)
        assertEquals(9, result.dayOfMonth)
    }

    @Test
    fun `parseApiDate returns null for invalid date`() {
        val result = DateUtils.parseApiDate("invalid-date")
        assertNull(result)
    }

    @Test
    fun `formatNav formats valid nav value`() {
        val result = DateUtils.formatNav("102.54940")
        assertEquals("₹102.55", result)
    }

    @Test
    fun `formatNav returns dash for null`() {
        val result = DateUtils.formatNav(null)
        assertEquals("—", result)
    }

    @Test
    fun `formatNavChange calculates positive change`() {
        val (text, isPositive) = DateUtils.formatNavChange(105.0, 100.0)
        assertEquals(true, isPositive)
        assert(text.contains("+5.00"))
    }

    @Test
    fun `formatNavChange calculates negative change`() {
        val (text, isPositive) = DateUtils.formatNavChange(95.0, 100.0)
        assertEquals(false, isPositive)
        assert(text.contains("-5.00"))
    }
}
