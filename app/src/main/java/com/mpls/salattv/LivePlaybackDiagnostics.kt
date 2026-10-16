package com.mpls.salattv

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

// Updated by player callbacks only; no per-frame UI work or polling.
internal object LivePlaybackDiagnostics {
    var quality by mutableStateOf("Connecting")
    var state by mutableStateOf("Connecting")
    var rebuffers by mutableIntStateOf(0)
    var bufferSeconds by mutableIntStateOf(0)
    var droppedFrames by mutableIntStateOf(0)
}
