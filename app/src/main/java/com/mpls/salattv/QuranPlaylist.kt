package com.mpls.salattv

/** Fixed surah recordings only. Never substitute a live radio feed on failure. */
internal object QuranPlaylist {
    const val reciter = "Sheikh Mishary Al-Afasy"
    val urls: List<String> = (1..114).map {
        "https://cdn.mp3quran.net/audio/mishary-alafasy/r1/${it.toString().padStart(3, '0')}.mp3"
    }
}
