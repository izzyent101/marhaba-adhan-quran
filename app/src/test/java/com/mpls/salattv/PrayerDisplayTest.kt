package com.mpls.salattv

import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class PrayerDisplayTest {
    private val timings = mapOf("Fajr" to "05:30", "Sunrise" to "07:00", "Dhuhr" to "12:45", "Asr" to "16:00", "Maghrib" to "18:20", "Isha" to "19:40")
    private fun at(hour: Int, minute: Int) = Calendar.getInstance(TimeZone.getTimeZone("America/Chicago")).apply {
        set(2026, Calendar.OCTOBER, 14, hour, minute, 0)
        set(Calendar.MILLISECOND, 0)
    }.time

    @Test fun daylightStartsAtSunrise() {
        assertFalse(isRestaurantDaytime(timings, at(6, 59)))
        assertTrue(isRestaurantDaytime(timings, at(7, 0)))
    }
    @Test fun nightStartsAtSunset() {
        assertTrue(isRestaurantDaytime(timings, at(18, 19)))
        assertFalse(isRestaurantDaytime(timings, at(18, 20)))
        assertFalse(isRestaurantDaytime(timings, at(1, 0)))
    }
    @Test fun deviceTimezoneCannotChangeRestaurantSky() {
        val original = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"))
            assertTrue(isRestaurantDaytime(timings, at(12, 0)))
            assertEquals("Dhuhr", computeNextPrayer(timings, at(12, 0))?.name)
        } finally { TimeZone.setDefault(original) }
    }
    @Test fun sunriseDoesNotReplaceNextPrayer() {
        val next = computeNextPrayer(timings, at(6, 0))!!
        assertEquals("Dhuhr", next.name)
        assertEquals(6 * 3600000L + 45 * 60000L, next.diffMs)
    }
    @Test fun eveningRollsOverToFajr() {
        val next = computeNextPrayer(timings, at(23, 30))!!
        assertEquals("Fajr", next.name)
        assertEquals(6 * 3600000L, next.diffMs)
    }
    @Test fun morningFindsFajr() {
        assertEquals("Fajr", computeNextPrayer(timings, at(4, 0))?.name)
    }
    @Test fun emptyTimesHaveNoInventedPrayer() {
        assertNull(computeNextPrayer(emptyMap(), at(10, 0)))
    }
}
