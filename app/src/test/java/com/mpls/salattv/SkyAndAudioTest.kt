package com.mpls.salattv

import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class SkyAndAudioTest {
    private val times = mapOf("Sunrise" to "07:00", "Maghrib" to "18:00")
    private fun at(h: Int, m: Int) = Calendar.getInstance(TimeZone.getTimeZone("America/Chicago")).apply {
        set(2026, Calendar.OCTOBER, 14, h, m, 0)
        set(Calendar.MILLISECOND, 0)
    }.time
    @Test fun sunriseIsGradualAndMonotonic() {
        val values = (0..75).map { skyLight(times, at(6, 15 + it)).daylight }
        assertEquals(0f, values.first(), .0001f)
        assertEquals(1f, values.last(), .0001f)
        assertTrue(values.zipWithNext().all { (a, b) -> b >= a && b - a < .03f })
        assertTrue(skyLight(times, at(7, 0)).twilight > .5f)
    }
    @Test fun sunsetFadesToNightAndMiddayIsFullDaylight() {
        assertEquals(1f, skyLight(times, at(12, 0)).daylight, .0001f)
        assertTrue(skyLight(times, at(18, 0)).daylight in .1f.. .9f)
        assertEquals(0f, skyLight(times, at(19, 0)).daylight, .0001f)
        assertEquals(0f, skyLight(times, at(3, 0)).daylight, .0001f)
    }
    @Test fun mandatedAudioLevelsStaySeparate() {
        assertEquals(.05f, QURAN_VOLUME, 0f)
        assertEquals(1f, ADHAN_VOLUME, 0f)
    }
    @Test fun nonBuildTagsCannotBecomeUpdates() {
        assertNull(UpdateChecker.releaseVersion("quran-audio"))
        assertNull(UpdateChecker.releaseVersion("quran-audio-2026"))
        assertEquals(1054, UpdateChecker.releaseVersion("1054"))
    }
}
