# Marhaba Adhan Quran App

Native Kotlin / Jetpack Compose wall display for Marhaba Grill, Minneapolis 55408.

## October 2026 display upgrade

- Marhaba branding, clean prayer cards, larger Makkah view, bilingual prayer schedule, local weather and sunrise.
- **Customize display** saves the welcome message, Arabic prayer labels, and 12/24-hour clock on each TV. Cancel discards edits.
- The sky uses bundled real cloud footage (an approximately 80-second playback loop pre-rendered at 30 fps) and a stationary, naturally scattered star field with independent scintillation. There are no moving still images, repeating patterns, illustrated sun/moon icons, or decorative arches. A soft solar disc follows a continuous sunrise-to-sunset arc, with peach dawn, blue daylight and amber/violet dusk. The footer pairs “A space to gather. A moment to reflect.” with Arabic. Dawn and dusk blend continuously over 75 minutes around Minneapolis sunrise and sunset. Clock, prayer scheduling and midnight refresh use America/Chicago, independent of the TV box timezone.
- Glass-style panels use translucent tints and fixed edge reflections, without expensive real-time backdrop blur. Live video uses a separate SurfaceView with native rounded clipping, reducing competition with dashboard animation. The HD feed publishes 10–12-second segments. Playback targets 25 seconds behind live, waits for 5 seconds at startup and 10 seconds after rebuffering, and allows 45 seconds before stalled-playback recovery. Stall detection accepts backward position changes when the live playlist slides, avoiding false restart loops. Settings distinguish video loading interruptions from decoder-dropped frames and report buffered seconds. Live playback stays at 1× instead of changing speed to chase the live edge. Display & updates reports source format and player-reported dropped frames for device troubleshooting.
- Makkah video prefers a verified 1280×720 source, with the old 480×384 feed retained only for failover. Stream quality is still limited by the broadcaster. An eight-second primary-feed sample on October 16 contained 200 frames with mostly 38–39 ms intervals and eight 77 ms gaps; the app cannot recover frames missing in the source. Video remains silent and fills its panel with a centered crop, without stretching or added side bars.
- Quran audio uses 114 individual [Mishary Al-Afasy Quran recordings](https://www.mp3quran.net/eng/afs/downloads), played sequentially and repeated. **There are no live radio stations or radio fallbacks.** Recording availability still requires an internet connection.
- Quran plays at 5%, pauses during the app's scheduled adhan and 15-minute prayer window, and resumes from its position. Playback progress is saved every 30 seconds and when the player is disposed.
- Adhan remains a separate player at 100% with the existing supported-device boost. Test Adhan pauses Quran; Stop Test or test completion restores playback unless a prayer/user mute is active.
- Next prayer means Fajr, Dhuhr, Asr, Maghrib or Isha. Sunrise has its own weather-strip display. After Isha, the next Fajr estimate uses today's Fajr until the midnight refresh.

## Source and release

Active Android sources are under `app/src/main/java/com/mpls/salattv/`.
`PremiumDisplay.kt` owns the display/settings; `QuranPlaylist.kt` owns the recording allowlist; `MainActivity.kt` owns scheduling and playback.
The old top-level `MainActivity.kt` is not compiled by Android.

Pushes to `main` build and publish a numeric GitHub release containing `app-debug.apk`.
Keep the existing application ID and signing key for upgrade compatibility.
Devices check 15 seconds after launch and approximately every 15 minutes. Display & updates shows the installed version, download/error status, an explicit retry action, and a permission action when Android blocks installation. Downloads are checked for complete size, package identity and version before opening Android’s installer. A one-time manual install may be needed on old versions that point at a retired repository. Android still requires its own installation approval. The app does not silently install over system restrictions.

## Verification

Use JDK 17, Gradle 8.7 and Android SDK 34:

```sh
gradle :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

Unit tests cover next-prayer selection, timezone consistency, gradual sky transitions, the Quran-only playlist, release tags and the required Quran 5% / adhan 100% volume constants. Validate the display and remote navigation on a TV, including customization persistence and Test Adhan / Stop Test. A successful build does not replace a physical speaker-volume check or a 24/7 soak test.

Cloud attribution and adaptation details: [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
