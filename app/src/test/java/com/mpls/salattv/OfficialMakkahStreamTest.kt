package com.mpls.salattv

import org.junit.Assert.*
import org.junit.Test

class OfficialMakkahStreamTest {
    @Test fun onlyOfficialQuranVideoUrlsAreAccepted() {
        assertTrue(OfficialMakkahStream.isOfficialUrl("https://live.kwikmotion.com/ksaquranlive/ksaquran.smil/playlist_dvr.m3u8?hdnts=example"))
        listOf(
            "http://live.kwikmotion.com/ksaquranlive/ksaquran.smil/playlist.m3u8",
            "https://live.kwikmotion.com.evil.test/ksaquranlive/ksaquran.smil/playlist.m3u8",
            "https://live.kwikmotion.com/sbrksaquranradiolive/srpksaquranradio/playlist.m3u8",
            "https://evil.test/playlist.m3u8",
            "not a URL"
        ).forEach { assertFalse(OfficialMakkahStream.isOfficialUrl(it)) }
    }
}
