package com.mpls.salattv

import org.junit.Assert.*
import org.junit.Test

class DigitalTimeTest {
    @Test fun midnightNoonAndEveningHaveCorrectPeriods() {
        assertEquals("12:00 AM", formatDisplayTime("00:00", false))
        assertEquals("12:59 PM", formatDisplayTime("12:59", false))
        assertEquals("7:47 PM", formatDisplayTime("19:47", false))
    }
    @Test fun twentyFourHourDisplayKeepsLeadingZeroes() {
        assertEquals("06:10", formatDisplayTime("06:10", true))
        assertEquals("19:47", formatDisplayTime("19:47", true))
    }
    @Test fun missingOrInvalidScheduleShowsPlaceholder() {
        listOf(null, "", "24:00", "12:60", "not a time").forEach {
            assertEquals("—", formatDisplayTime(it, true))
        }
    }
}
