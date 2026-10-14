package com.mpls.salattv

import android.view.TextureView
import androidx.annotation.OptIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import kotlin.math.sin
import kotlin.random.Random

internal data class SkyLight(val daylight: Float, val twilight: Float)

internal fun skyLight(timings: Map<String, String>, now: Date): SkyLight {
    fun minutes(value: String?, fallback: Float): Float {
        val parts = value?.split(":") ?: return fallback
        val h = parts.getOrNull(0)?.toIntOrNull() ?: return fallback
        val m = parts.getOrNull(1)?.toIntOrNull() ?: return fallback
        return if (h in 0..23 && m in 0..59) h * 60f + m else fallback
    }
    fun smooth(value: Float): Float {
        val x = value.coerceIn(0f, 1f)
        return x * x * (3 - 2 * x)
    }
    val cal = Calendar.getInstance(TimeZone.getTimeZone("America/Chicago")).apply { time = now }
    val time = cal.get(Calendar.HOUR_OF_DAY) * 60f + cal.get(Calendar.MINUTE) + cal.get(Calendar.SECOND) / 60f
    val sunrise = minutes(timings["Sunrise"], 420f)
    val sunset = minutes(timings["Maghrib"], 1080f)
    // A continuous 75-minute atmospheric transition, not a theme toggle.
    val light = minOf(smooth((time - sunrise + 45) / 75), 1 - smooth((time - sunset + 30) / 75))
    return SkyLight(light, 4 * light * (1 - light))
}

private data class NaturalStar(val x: Float, val y: Float, val radius: Float,
    val brightness: Float, val speed: Float, val phase: Float, val color: Color)

@OptIn(UnstableApi::class)
@Composable
internal fun NaturalSky(light: SkyLight, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val latestLight by rememberUpdatedState(light)
    val stars = remember {
        val random = Random(8247)
        List(1200) {
            val bright = random.nextFloat()
            NaturalStar(random.nextFloat(), random.nextFloat(), if (bright > .97f) 1.2f else .25f + bright * .5f,
                .16f + bright * .65f, .7f + random.nextFloat() * 1.8f, random.nextFloat() * 6.283f,
                when (random.nextInt(9)) { 0 -> Color(0xFFBFD8FF); 1 -> Color(0xFFFFE6C2); else -> Color(0xFFEDF2FF) })
        }
    }
    val elapsed = remember { mutableFloatStateOf(0f) }
    LaunchedEffect(lifecycle) {
        val start = android.os.SystemClock.elapsedRealtime()
        while (true) {
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED) && latestLight.daylight < .999f) {
                elapsed.floatValue = (android.os.SystemClock.elapsedRealtime() - start) / 1000f
            }
            kotlinx.coroutines.delay(50L)
        }
    }
    val clouds = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri("android.resource://${context.packageName}/${R.raw.cloud_sky}"))
            repeatMode = Player.REPEAT_MODE_ONE
            volume = 0f
            playbackParameters = PlaybackParameters(.25f)
            prepare()
        }
    }
    DisposableEffect(clouds, lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) clouds.pause()
            if (event == Lifecycle.Event.ON_START) clouds.playWhenReady = latestLight.daylight > .001f
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer); clouds.release() }
    }
    LaunchedEffect(light.daylight > .001f) {
        clouds.playWhenReady = light.daylight > .001f && lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
    }
    Box(modifier) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(Brush.verticalGradient(listOf(Color(0xFF020711), Color(0xFF0B1528), Color(0xFF131E31))))
            val t = elapsed.floatValue
            val visibility = (1 - light.daylight) * (1 - light.daylight)
            val scale = size.width / 1920f
            for (star in stars) {
                // Each stationary star scintillates at its own non-synchronized frequencies.
                val scintillation = .68f + .20f * sin(t * star.speed + star.phase) + .12f * sin(t * star.speed * 2.31f + star.phase * 3)
                val alpha = (star.brightness * scintillation * visibility).coerceIn(0f, 1f)
                val center = Offset(star.x * size.width, star.y * size.height)
                if (star.radius > 1f) drawCircle(star.color.copy(alpha = alpha * .08f), star.radius * scale * 3, center)
                drawCircle(star.color.copy(alpha = alpha), star.radius * scale, center)
            }
        }
        AndroidView(factory = { ctx -> TextureView(ctx).apply {
            isOpaque = false
            clouds.setVideoTextureView(this)
        } }, update = { it.alpha = light.daylight }, modifier = Modifier.fillMaxSize())
        Canvas(Modifier.fillMaxSize()) {
            // Warm scattered light only during dawn/dusk; no drawn celestial symbols.
            drawRect(Brush.verticalGradient(listOf(Color(0xFF6A4D7C).copy(alpha = light.twilight * .18f),
                Color(0xFFEF9462).copy(alpha = light.twilight * .38f))))
            // Keep the header readable while retaining the natural cloud detail.
            drawRect(Brush.verticalGradient(listOf(Color.Black.copy(alpha = .30f * light.daylight),
                Color.Black.copy(alpha = .08f * light.daylight), Color.Black.copy(alpha = .32f * light.daylight))))
        }
    }
}
