package com.mpls.salattv

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.cos
import kotlin.math.sin

private val Gold = Color(0xFFE6C784)
private val Ink = Color(0xFF081C21)
private val Cream = Color(0xFFF5F0E5)
private val Muted = Color(0xFFABC0BD)

internal data class DisplayPreferences(
    val welcome: String = "Welcome to Marhaba Grill",
    val arabic: Boolean = true,
    val clock24: Boolean = false
)

@Composable
internal fun rememberDisplayPreferences(): MutableState<DisplayPreferences> {
    val context = LocalContext.current
    return remember {
        val saved = context.getSharedPreferences("marhaba_display", 0)
        mutableStateOf(DisplayPreferences(
            welcome = saved.getString("welcome", "Welcome to Marhaba Grill") ?: "Welcome to Marhaba Grill",
            arabic = saved.getBoolean("arabic", true),
            clock24 = saved.getBoolean("clock24", false)
        ))
    }
}

@Composable
internal fun PremiumBackground(day: Boolean, modifier: Modifier) {
    val transition = rememberInfiniteTransition(label = "sky ambience")
    val shimmer = transition.animateFloat(.45f, 1f,
        infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Reverse), label = "starlight")
    Canvas(modifier) {
        val w = size.width; val h = size.height
        drawRect(Brush.verticalGradient(if (day) listOf(Color(0xFF306D91), Color(0xFF174659), Color(0xFF081D28))
            else listOf(Color(0xFF0D1B39), Color(0xFF0B1628), Color(0xFF050E19))))
        val celestial = Offset(w * .68f, h * .10f)
        drawCircle(Brush.radialGradient(listOf(
            (if (day) Color(0xFFFFD793) else Color(0xFFBCD8FF)).copy(alpha = if (day) .22f else .12f),
            Color.Transparent), center = celestial, radius = w * .32f), w * .32f, celestial)
        if (day) {
            drawCircle(Brush.radialGradient(listOf(Color(0xFFFFF1BF), Color(0xFFEAC77C))), 19.dp.toPx(), celestial)
            for (i in 0 until 12) {
                val a = i * Math.PI / 6
                val inner = 25.dp.toPx(); val outer = 30.dp.toPx()
                drawLine(Gold.copy(alpha = .35f),
                    celestial + Offset(cos(a).toFloat() * inner, sin(a).toFloat() * inner),
                    celestial + Offset(cos(a).toFloat() * outer, sin(a).toFloat() * outer), 1.dp.toPx())
            }
            // Layered cloud wisps, drawn once per frame as vector paths.
            for (i in 0..2) {
                val cloud = Path().apply {
                    moveTo(w * (.40f + i * .12f), h * (.09f + i * .025f))
                    cubicTo(w * (.47f + i * .12f), h * (.04f + i * .025f),
                        w * (.51f + i * .12f), h * (.14f + i * .025f),
                        w * (.59f + i * .12f), h * (.10f + i * .025f))
                }
                drawPath(cloud, Cream.copy(alpha = .06f), style = Stroke(5.dp.toPx()))
            }
        } else {
            val r = 23.dp.toPx()
            val moon = Path().apply {
                moveTo(celestial.x + r * .45f, celestial.y - r)
                cubicTo(celestial.x - r * 1.35f, celestial.y - r,
                    celestial.x - r * 1.35f, celestial.y + r, celestial.x + r * .45f, celestial.y + r)
                cubicTo(celestial.x - r * .55f, celestial.y + r * .5f,
                    celestial.x - r * .55f, celestial.y - r * .5f, celestial.x + r * .45f, celestial.y - r)
                close()
            }
            drawPath(moon, Color(0xFFDFE8F6))
            for (i in 0 until 48) {
                val alpha = if (i % 3 == 0) .4f * shimmer.value else .22f
                drawCircle(Cream.copy(alpha = alpha), (if (i % 7 == 0) 1.2f else .65f).dp.toPx(),
                    Offset(w * ((i * 37 % 101) / 101f), h * ((i * 19 % 73) / 100f)))
            }
        }
        // Islamic geometry adds texture without competing with the live image or type.
        val step = 84.dp.toPx()
        for (row in 0..(h / step).toInt()) {
            for (col in 0..(w / step).toInt()) {
                val cx = col * step; val cy = row * step
                val star = Path()
                for (point in 0 until 16) {
                    val angle = point * Math.PI / 8
                    val radius = step * if (point % 2 == 0) .32f else .16f
                    val x = cx + cos(angle).toFloat() * radius
                    val y = cy + sin(angle).toFloat() * radius
                    if (point == 0) star.moveTo(x, y) else star.lineTo(x, y)
                }
                star.close()
                drawPath(star, Gold.copy(alpha = .035f), style = Stroke(1.dp.toPx()))
            }
        }
    }
}

@Composable
internal fun PremiumScreen(
    data: PrayerData, weather: WeatherData?, now: Date,
    isPrayerMute: Boolean, isQuranMuted: Boolean, onToggleMute: () -> Unit,
    preferences: DisplayPreferences, onPreferences: (DisplayPreferences) -> Unit
) {
    var settingsOpen by remember { mutableStateOf(false) }
    val next = computeNextPrayer(data.timings, now)
    val holiday = PrayerRepository.getHolidayMessage(data.hijriMonthNumber, data.hijriDay)
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(Modifier.fillMaxWidth().height(72.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(R.drawable.marhaba_logo), "Marhaba Grill",
                Modifier.width(150.dp).fillMaxHeight(), contentScale = ContentScale.Fit)
            Box(Modifier.padding(horizontal = 20.dp).width(1.dp).height(40.dp).background(Gold.copy(alpha = .3f)))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(holiday?.en ?: preferences.welcome, color = Cream, fontSize = 20.sp,
                    fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(if (holiday != null && preferences.arabic) holiday.ar else "MINNEAPOLIS  ·  PRAYER & QURAN",
                    color = Gold, fontSize = 10.sp, letterSpacing = 2.sp, maxLines = 1)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(SimpleDateFormat(if (preferences.clock24) "HH:mm" else "h:mm a", Locale.US).apply { timeZone = TimeZone.getTimeZone("America/Chicago") }.format(now),
                    color = Cream, fontSize = 36.sp, fontWeight = FontWeight.Light, maxLines = 1)
                Text(SimpleDateFormat("EEE, MMM d", Locale.US).apply { timeZone = TimeZone.getTimeZone("America/Chicago") }.format(now) + "  ·  ${data.hijriDay} ${data.hijriMonthEn}",
                    color = Muted, fontSize = 11.sp, maxLines = 1)
            }
        }
        Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            Column(Modifier.weight(.60f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.weight(1f)) {
                    StreamView(isPrayerMute, isQuranMuted, onToggleMute)
                }
                Row(Modifier.fillMaxWidth().height(60.dp).panel().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    WeatherIcon(weather?.kind ?: WeatherKind.CLOUD, weather?.isDay ?: true, Modifier.size(34.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(weather?.let { "${it.tempF}°F  ·  ${it.condition}" } ?: "Weather updating…",
                            color = Cream, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("MINNEAPOLIS", color = Muted, fontSize = 9.sp, letterSpacing = 1.sp)
                    }
                    SunriseIcon(Modifier.size(32.dp))
                    Spacer(Modifier.width(10.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text("SUNRISE", color = Muted, fontSize = 9.sp, letterSpacing = 1.sp)
                        Text(to12Hour(data.timings["Sunrise"]), color = Gold, fontSize = 15.sp)
                    }
                }
            }
            Column(Modifier.weight(.40f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.fillMaxWidth().weight(1.15f).panel(highlight = true)) {
                    ArchArtwork(Modifier.align(Alignment.CenterEnd).fillMaxHeight().width(140.dp))
                    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
                        Text(if (isPrayerMute) "A MOMENT FOR PRAYER" else "NEXT PRAYER", color = Gold,
                            fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(5.dp))
                        Text(if (isPrayerMute) "Prayer time" else next?.name ?: "Updating…", color = Cream,
                            fontSize = 30.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                        Text(if (isPrayerMute) "Quran resumes after prayer" else next?.let { "In ${formatCountdown(it.diffMs)}  ·  ${to12Hour(data.timings[it.name])}" } ?: "Waiting for prayer times",
                            color = Gold, fontSize = 13.sp, maxLines = 1)
                    }
                }
                Column(Modifier.fillMaxWidth().weight(2f).panel().padding(horizontal = 14.dp, vertical = 8.dp)) {
                    Row(Modifier.fillMaxWidth().padding(bottom = 5.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("TODAY’S PRAYERS", color = Muted, fontSize = 9.sp, letterSpacing = 1.4.sp)
                        Text("55408", color = Muted, fontSize = 9.sp)
                    }
                    val names = listOf("Fajr" to "الفجر", "Dhuhr" to "الظهر", "Asr" to "العصر", "Maghrib" to "المغرب", "Isha" to "العشاء")
                    names.forEach { (name, ar) ->
                        val selected = next?.name == name
                        Row(Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(8.dp))
                            .background(if (selected) Gold.copy(alpha = .12f) else Color.Transparent)
                            .padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.width(3.dp).height(18.dp).background(if (selected) Gold else Color.Transparent))
                            Spacer(Modifier.width(9.dp))
                            Text(name, color = if (selected) Gold else Cream, fontSize = 16.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f))
                            if (preferences.arabic) Text(ar, color = Muted, fontSize = 13.sp, modifier = Modifier.padding(end = 12.dp))
                            Text(to12Hour(data.timings[name]), color = if (selected) Gold else Cream,
                                fontSize = 17.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                        }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().height(32.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("MARHABA GRILL", color = Gold, fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
            Text("  /  A space to gather. A moment to reflect.", color = Muted, fontSize = 11.sp, modifier = Modifier.weight(1f))
            TextButton(onClick = { settingsOpen = true }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)) {
                Text("Customize display", color = Gold, fontSize = 11.sp)
            }
        }
    }
    if (settingsOpen) DisplaySettings(preferences, onDismiss = { settingsOpen = false }) {
        onPreferences(it)
        settingsOpen = false
    }
}

private fun Modifier.panel(highlight: Boolean = false): Modifier = clip(RoundedCornerShape(18.dp))
    .background(Brush.linearGradient(if (highlight) listOf(Color(0xFF254B48), Color(0xFF122D31)) else listOf(Color(0xFF11292E), Color(0xFF0B2026))))
    .border(1.dp, Gold.copy(alpha = if (highlight) .5f else .16f), RoundedCornerShape(18.dp))

@Composable
private fun ArchArtwork(modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width; val h = size.height
        for (i in 0..3) {
            val inset = w * (.08f + .10f * i)
            val p = Path().apply {
                moveTo(inset, h)
                lineTo(inset, h * .54f)
                cubicTo(inset, h * .30f, w * .5f, h * .18f, w * .5f, h * .08f)
                cubicTo(w * .5f, h * .18f, w - inset, h * .30f, w - inset, h * .54f)
                lineTo(w - inset, h)
            }
            drawPath(p, Gold.copy(alpha = .10f), style = Stroke(1.dp.toPx()))
        }
        drawCircle(Gold.copy(alpha = .14f), w * .1f, Offset(w * .5f, h * .42f))
    }
}

@Composable
private fun DisplaySettings(initial: DisplayPreferences, onDismiss: () -> Unit, onSave: (DisplayPreferences) -> Unit) {
    var draft by remember { mutableStateOf(initial) }
    MaterialTheme(colorScheme = darkColorScheme(primary = Gold, onPrimary = Ink, surface = Ink, onSurface = Cream)) {
        AlertDialog(onDismissRequest = onDismiss, title = { Text("Make it Marhaba") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Display preferences are saved on this TV.", color = Muted)
                    OutlinedTextField(value = draft.welcome, onValueChange = { draft = draft.copy(welcome = it.take(48)) },
                        label = { Text("Welcome message") }, singleLine = true)
                    Text("Automatic sky · Minneapolis", color = Gold)
                    Text("Daylight from sunrise to sunset. Moonlight and stars after sunset. Changes automatically throughout the year.", color = Muted, fontSize = 13.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Arabic prayer names", Modifier.weight(1f))
                        Switch(draft.arabic, { draft = draft.copy(arabic = it) })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("24-hour clock", Modifier.weight(1f))
                        Switch(draft.clock24, { draft = draft.copy(clock24 = it) })
                    }
                }
            },
            confirmButton = { TextButton(onClick = { onSave(draft.copy(welcome = draft.welcome.trim().ifBlank { "Welcome to Marhaba Grill" })) }) { Text("Save display") } },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
    }
}
