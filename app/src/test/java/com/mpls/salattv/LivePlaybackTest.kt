package com.mpls.salattv

import org.junit.Assert.*
import org.junit.Test

class LivePlaybackTest {
    @Test fun advancingSlidingPlaylistIsNotMistakenForAStall() {
        val positions = listOf(18000L, 22000L, 16000L, 20000L, 14000L, 18000L)
        assertTrue(positions.zipWithNext().all { (before, after) -> livePositionChanged(before, after) })
    }
    @Test fun frozenPositionAndMinorJitterDoNotResetStallTimer() {
        assertFalse(livePositionChanged(18000L, 18000L))
        assertFalse(livePositionChanged(18000L, 18050L))
        assertFalse(livePositionChanged(18000L, 17950L))
    }
}
