package com.mpls.salattv

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Activity-level input also catches taps on the native video surface. */
internal object SettingsVisibility {
    var visible by mutableStateOf(false)
    var lastInteraction by mutableLongStateOf(0L)
    var dialogOpen = false

    fun reveal() {
        lastInteraction = SystemClock.elapsedRealtime()
        visible = true
    }
}
