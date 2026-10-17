# Marhaba Prayer Display

Native Kotlin / Jetpack Compose wall display for Marhaba Grill, Minneapolis 55408.

## October 2026 display upgrade

- Marhaba branding, clean prayer cards, larger Makkah view, bilingual prayer schedule, local weather and sunrise. The schedule is titled “Today’s prayer times for Minneapolis, MN 55408.”
- The settings gear starts hidden. Any screen tap or a remote direction/OK/Menu press reveals it and focuses it; a subsequent OK opens settings. It hides after eight seconds of inactivity. Clock/header spacing stays fixed; settings remains accessible while its dialog is open.
- The balanced header centers the logo over a fixed soft gold glow, with Gregorian/Hijri dates in English and Arabic on the left and a dedicated segmented digital clock and settings gear on the right. Scheduled prayer times, the next-prayer card and sunrise/sunset use the same segmented clock style and honor the 12/24-hour setting. Welcome copy and footer text have been removed, giving the panels more space. Settings saves Arabic prayer labels and the 12/24-hour clock on each TV. Cancel discards edits.
- The sky uses bundled real cloud footage (an approximately 80-second playback loop pre-rendered at 30 fps) and a stationary, naturally scattered star field with independent scintillation. There are no moving still images, repeating patterns, illustrated sun/moon icons, or decorative arches. A soft solar disc follows a continuous sunrise-to-sunset arc, with peach dawn, blue daylight and amber/violet dusk. Dawn and dusk blend continuously over 75 minutes around Minneapolis sunrise and sunset. Clock, prayer scheduling and midnight refresh use America/Chicago, independent of the TV box timezone.
- Glass-style panels use translucent tints and fixed edge reflections, without expensive real-time backdrop blur. Live video uses a separate SurfaceView with native rounded clipping, reducing competition with dashboard animation. The HD feed publishes 10–12-second segments. Playback targets 25 seconds behind live, waits for 5 seconds at startup and 10 seconds after rebuffering, and allows 45 seconds before stalled-playback recovery. Stall detection accepts backward position changes when the live playlist slides, avoiding false restart loops. Settings distinguish video loading interruptions from decoder-dropped frames and report buffered seconds. Live playback stays at 1× instead of changing speed to chase the live edge. Display & updates reports source format and player-reported dropped frames for device troubleshooting.
- Makkah video now requests the official broadcaster’s public guest HLS URL at startup, preferring its 1920×1080 rendition. Expired signed URLs are refreshed through the public endpoint, with a retry cooldown before failover. No signed playback URL is saved in source or preferences. The existing 720p and SD sources remain available for outages. 1080p was verified in the official player on October 17; no 4K rendition was offered. Stream quality is still limited by the broadcaster. An eight-second primary-feed sample on October 16 contained 200 frames with mostly 38–39 ms intervals and eight 77 ms gaps; the app cannot recover frames missing in the source. Video remains silent and fills its panel with a centered crop, without stretching or added side bars.
- A quiet display: continuous Quran playback, playlist fetching, progress writes, equalizer and mute controls have been removed. The live video audio track stays disabled. The only audible media is the scheduled adhan, or an explicit full-volume adhan test in Display & updates.
- Adhan remains a separate player at 100% with the existing supported-device boost. A small bilingual indicator appears only while adhan is playing. Prayer time keeps a reflection message visible for 15 minutes.
- A uniform, top-anchored video crop hides the broadcaster’s bottom reader-name/clock strip without stretching the camera picture. The native crop retains the separate video surface. Broadcast graphics that change location may need a different crop.
- The live panel is unobstructed except for a compact live badge. Weather shows the next sunrise or sunset.
- Next prayer means Fajr, Dhuhr, Asr, Maghrib or Isha. Sunrise has its own weather-strip display. After Isha, the next Fajr estimate uses today's Fajr until the midnight refresh.

## Source and release

Active Android sources are under `app/src/main/java/com/mpls/salattv/`.
`PremiumDisplay.kt` owns the display/settings; `MainActivity.kt` owns scheduling and playback.
The old top-level `MainActivity.kt` is not compiled by Android.

Pushes to `main` build and publish a numeric GitHub release containing `app-debug.apk`.
Keep the existing application ID and signing key for upgrade compatibility.
Devices check 15 seconds after launch and approximately every 15 minutes. Display & updates shows the installed version, download/error status, an explicit retry action, and a permission action when Android blocks installation. Downloads are checked for complete size, package identity and version before opening Android’s installer. A one-time manual install may be needed on old versions that point at a retired repository. Android still requires its own installation approval. The app does not silently install over system restrictions.

## Verification

Use JDK 17, Gradle 8.7 and Android SDK 34:

```sh
gradle :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

Unit tests cover next-prayer selection, timezone consistency, gradual sky transitions, live-playlist recovery, release tags and the required adhan 100% volume constant. Validate the display and remote navigation on a TV, including customization persistence and the full-volume adhan test. A successful build does not replace a physical speaker-volume check or a 24/7 soak test.

Cloud attribution and adaptation details: [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
