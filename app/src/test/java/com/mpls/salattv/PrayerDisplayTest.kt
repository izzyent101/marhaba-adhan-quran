package com.mpls.salattv

import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class PrayerDisplayTest {
    private val timings = mapOf("Fajr" to "05:30", "Sunrise" to "07:00", "Dhuhr" to "12:45", "Asr" to "16:00", "Maghrib" to "18:20", "Isha" to "19:40")
    private fun at(hour: Int, minute: Int) = Calendar.getInstance().apply {
        set(2026, Calendar.OCTOBER, 14, hour, minute, 0)
        set(Calendar.MILLISECOND, 0)
    }.time

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
