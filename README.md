# Marhaba Adhan Quran App

Native Kotlin / Jetpack Compose wall display for Marhaba Grill, Minneapolis 55408.

## October 2026 display upgrade

- Marhaba branding, gold geometric artwork, architectural prayer card, larger Makkah view, bilingual prayer schedule, local weather and sunrise.
- **Customize display** saves the welcome message, automatic day/night or emerald/midnight-blue atmosphere, Arabic prayer labels, and 12/24-hour clock on each TV. Cancel discards edits.
- Makkah video remains silent and preserves the complete video frame.
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
Devices check approximately every six hours. Android may require an Install tap.

## Verification

Use JDK 17, Gradle 8.7 and Android SDK 34:

```sh
gradle :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

Unit tests cover next-prayer selection and the Quran-only playlist. Validate the display and remote navigation on a TV, including customization persistence and Test Adhan / Stop Test. A successful build does not replace a physical speaker-volume check or a 24/7 soak test.
