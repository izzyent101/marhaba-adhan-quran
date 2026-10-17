package com.mpls.salattv

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** A true segmented clock face, drawn sharply at the TV's native UI resolution. */
@Composable
internal fun DigitalClock(now: Date, clock24: Boolean) {
    val zone = TimeZone.getTimeZone("America/Chicago")
    fun format(pattern: String) = SimpleDateFormat(pattern, Locale.US).apply { timeZone = zone }.format(now)
    SegmentedTime(format(if (clock24) "HH:mm" else "h:mm a"))
}

@Composable
internal fun SegmentedTime(value: String, height: Dp = 43.dp, active: Color = Color(0xFFF5F0E5)) {
    val time = value.substringBefore(' ')
    if (time.isEmpty() || time.any { !it.isDigit() && it != ':' }) {
        Text("—", color = active, fontSize = 16.sp)
        return
    }
    val period = value.substringAfter(' ', "")
    val ratio = height.value / 43f
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy((8 * ratio).dp),
        modifier = Modifier.semantics { contentDescription = value }) {
        Canvas(Modifier.width(height * ((time.count { it != ':' } * 25 + 10) / 43f)).height(height)) {
            val scale = size.height / 43f
            val patterns = arrayOf("abcdef", "bc", "abdeg", "abcdg", "bcfg", "acdfg", "acdefg", "abc", "abcdefg", "abcdfg")
            var x = 0f
            for (c in time) {
                if (c == ':') {
                    drawCircle(active.copy(alpha = .75f), 1.6f * scale, Offset((x + 3) * scale, 15 * scale))
                    drawCircle(active.copy(alpha = .75f), 1.6f * scale, Offset((x + 3) * scale, 29 * scale))
                    x += 10
                } else {
                    val lit = patterns[c.digitToInt()]
                    fun segment(id: Char, vararg points: Float) {
                        val p = Path()
                        for (i in points.indices step 2) {
                            val px = (x + points[i]) * scale
                            val py = points[i + 1] * scale
                            if (i == 0) p.moveTo(px, py) else p.lineTo(px, py)
                        }
                        p.close()
                        drawPath(p, active.copy(alpha = if (id in lit) .96f else .035f))
                    }
                    segment('a', 4f,1f, 19f,1f, 21f,3f, 18f,5f, 5f,5f, 2f,3f)
                    segment('g', 5f,19f, 18f,19f, 21f,21f, 18f,23f, 5f,23f, 2f,21f)
                    segment('d', 5f,37f, 18f,37f, 21f,39f, 19f,41f, 4f,41f, 2f,39f)
                    segment('f', 1f,5f, 3f,4f, 5f,6f, 5f,17f, 2f,20f, 1f,18f)
                    segment('b', 20f,4f, 22f,5f, 22f,18f, 21f,20f, 18f,17f, 18f,6f)
                    segment('e', 2f,22f, 5f,25f, 5f,36f, 3f,38f, 1f,37f, 1f,24f)
                    segment('c', 21f,22f, 22f,24f, 22f,37f, 20f,38f, 18f,36f, 18f,25f)
                    x += 25
                }
            }
        }
        if (period.isNotEmpty()) Text(period, color = active.copy(alpha = .85f), fontSize = maxOf(8f, 10 * ratio).sp,
            fontWeight = FontWeight.Medium, letterSpacing = .5.sp, modifier = Modifier.padding(bottom = (3 * ratio).dp))
    }
}

internal fun formatDisplayTime(time24: String?, clock24: Boolean): String {
    val parts = time24?.split(":") ?: return "—"
    if (parts.size != 2) return "—"
    val hour = parts[0].toIntOrNull() ?: return "—"
    val minute = parts[1].toIntOrNull() ?: return "—"
    if (hour !in 0..23 || minute !in 0..59) return "—"
    return if (clock24) String.format(Locale.US, "%02d:%02d", hour, minute)
    else String.format(Locale.US, "%d:%02d %s", if (hour % 12 == 0) 12 else hour % 12,
        minute, if (hour < 12) "AM" else "PM")
}
