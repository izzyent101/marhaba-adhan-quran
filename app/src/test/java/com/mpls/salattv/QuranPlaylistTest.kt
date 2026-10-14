package com.mpls.salattv

import org.junit.Assert.*
import org.junit.Test

class QuranPlaylistTest {
    @Test fun onlyCompleteOrderedSurahRecordingsAreAllowed() {
        assertEquals(114, QuranPlaylist.urls.size)
        assertEquals(114, QuranPlaylist.urls.toSet().size)
        QuranPlaylist.urls.forEachIndexed { index, url ->
            assertEquals("https://cdn.mp3quran.net/audio/mishary-alafasy/r1/${(index + 1).toString().padStart(3, '0')}.mp3", url)
            assertFalse(url.contains("radio"))
            assertFalse(url.contains("adhan"))
        }
    }
}
