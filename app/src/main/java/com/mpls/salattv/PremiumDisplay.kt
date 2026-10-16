package com.mpls.salattv

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import kotlinx.coroutines.launch
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
internal fun PremiumScreen(
    data: PrayerData, weather: WeatherData?, now: Date,
    isPrayerWindow: Boolean,
    preferences: DisplayPreferences, onPreferences: (DisplayPreferences) -> Unit
) {
    var settingsOpen by remember { mutableStateOf(false) }
    var testingAdhan by remember { mutableStateOf(false) }
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
                Text(if (holiday != null && preferences.arabic) holiday.ar else "MINNEAPOLIS  ·  MAKKAH LIVE",
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
                    StreamView(isPrayerWindow, testingAdhan, onAdhanTestFinished = { testingAdhan = false })
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
                        Text(if (isRestaurantDaytime(data.timings, now)) "SUNSET" else "SUNRISE", color = Muted, fontSize = 9.sp, letterSpacing = 1.sp)
                        Text(to12Hour(data.timings[if (isRestaurantDaytime(data.timings, now)) "Maghrib" else "Sunrise"]), color = Gold, fontSize = 15.sp)
                    }
                }
            }
            Column(Modifier.weight(.40f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.fillMaxWidth().weight(1.15f).panel(highlight = true)) {
                    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
                        Text(if (isPrayerWindow) "A MOMENT FOR PRAYER" else "NEXT PRAYER", color = Gold,
                            fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(5.dp))
                        Text(if (isPrayerWindow) "Prayer time" else next?.name ?: "Updating…", color = Cream,
                            fontSize = 30.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                        Text(if (isPrayerWindow) "Take a moment to reflect · وقت الصلاة" else next?.let { "In ${formatCountdown(it.diffMs)}  ·  ${to12Hour(data.timings[it.name])}" } ?: "Waiting for prayer times",
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
            Text("A space to gather. A moment to reflect.", color = Cream, fontSize = 11.sp)
            Text("مساحةٌ للّقاء، ولحظةٌ للتأمّل", color = Gold, fontSize = 13.sp,
                modifier = Modifier.weight(1f).padding(start = 20.dp), maxLines = 1)
            TextButton(onClick = { settingsOpen = true }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)) {
                Text(if (UpdateChecker.needsPermission) "Allow app updates" else "Display & updates", color = Gold, fontSize = 11.sp)
            }
        }
    }
    if (settingsOpen) DisplaySettings(preferences, testingAdhan,
        onToggleAdhanTest = { testingAdhan = !testingAdhan }, onDismiss = { settingsOpen = false }) {
        onPreferences(it)
        settingsOpen = false
    }
}

// Translucent glass tint and fixed edge reflections keep the sky visible without
// a costly full-screen backdrop blur on TV hardware.
private fun Modifier.panel(highlight: Boolean = false): Modifier = clip(RoundedCornerShape(22.dp))
    .background(Brush.linearGradient(listOf(
        Color(0xFF35494E).copy(alpha = if (highlight) .68f else .58f),
        Color(0xFF10242C).copy(alpha = .78f),
        Color(0xFF142830).copy(alpha = .65f))))
    .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = .10f),
        Color.Transparent, Color.White.copy(alpha = .025f))))
    .border(1.dp, Brush.linearGradient(listOf(
        Color.White.copy(alpha = if (highlight) .55f else .35f),
        Color.White.copy(alpha = .06f), Color.White.copy(alpha = .22f))), RoundedCornerShape(22.dp))

@Composable
private fun DisplaySettings(initial: DisplayPreferences, testingAdhan: Boolean,
    onToggleAdhanTest: () -> Unit, onDismiss: () -> Unit, onSave: (DisplayPreferences) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var draft by remember { mutableStateOf(initial) }
    MaterialTheme(colorScheme = darkColorScheme(primary = Gold, onPrimary = Ink, surface = Ink, onSurface = Cream)) {
        AlertDialog(onDismissRequest = onDismiss, title = { Text("Make it Marhaba") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Preferences are saved on this TV.", color = Muted)
                    OutlinedTextField(value = draft.welcome, onValueChange = { draft = draft.copy(welcome = it.take(48)) },
                        label = { Text("Welcome message") }, singleLine = true)
                    Text("App version ${BuildConfig.VERSION_CODE}", color = Gold)
                    Text("Live video: ${LivePlaybackDiagnostics.quality} · ${LivePlaybackDiagnostics.state}", fontSize = 12.sp)
                    Text("${LivePlaybackDiagnostics.bufferSeconds}s video ready · ${LivePlaybackDiagnostics.rebuffers} loading interruptions", fontSize = 12.sp)
                    Text("${LivePlaybackDiagnostics.droppedFrames} dropped frames reported this session", fontSize = 12.sp)
                    Text(UpdateChecker.status, color = Muted, fontSize = 13.sp)
                    OutlinedButton(enabled = !UpdateChecker.busy, onClick = {
                        if (UpdateChecker.needsPermission) UpdateChecker.openInstallPermission(context)
                        else scope.launch { UpdateChecker.checkAndInstall(context) }
                    }) { Text(if (UpdateChecker.needsPermission) "Allow app updates" else "Check for update") }
                    Text("Quiet display · Adhan at prayer times", color = Gold, fontSize = 12.sp)
                    Text("Live video is silent. The scheduled adhan plays at 100% in-app volume.", color = Muted, fontSize = 12.sp)
                    OutlinedButton(onClick = onToggleAdhanTest) {
                        Text(if (testingAdhan) "Stop adhan test" else "Test adhan · Full volume")
                    }
                    Text("Automatic sky · Minneapolis", color = Gold)
                    Text("Real cloud footage by day. Independently twinkling stars at night. Gradual dawn and dusk follow Minneapolis sunrise and sunset.", color = Muted, fontSize = 13.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Arabic prayer names", Modifier.weight(1f))
                        Switch(draft.arabic, { draft = draft.copy(arabic = it) })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("24-hour clock", Modifier.weight(1f))
                        Switch(draft.clock24, { draft = draft.copy(clock24 = it) })
                    }
                    Text("Cloud footage: Editor · CC BY 3.0. Looped, slowed and color blended for this display.", color = Muted, fontSize = 10.sp)
                    TextButton(onClick = { context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse("https://commons.wikimedia.org/wiki/File:Clouds_(time_lapse).webm"))) }) { Text("Footage source & license", fontSize = 11.sp) }
                }
            },
            confirmButton = { TextButton(onClick = { onSave(draft.copy(welcome = draft.welcome.trim().ifBlank { "Welcome to Marhaba Grill" })) }) { Text("Save display") } },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
    }
}
