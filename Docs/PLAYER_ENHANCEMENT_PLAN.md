# Enhanced Video Player & Playback System - Implementation Plan

## Overview
This document outlines the architecture and execution plan for augmenting the built-in video player in the Esheeq Android application with resume watching, app-wide progress indicators, screen lock mode, gesture controls (volume, brightness, seek), double-tap seek, and episode navigation.

---

## 1. Feature Specifications

### 1.1 Resume Watching & Hybrid Parity Engine
- **Data Persistence**: `PlaybackHistoryManager` using `SharedPreferences` with Gson serialization, exposing reactive `StateFlow<Map<String, WatchProgress>>`.
- **Data Model**:
  ```kotlin
  data class WatchProgress(
      val episodeUrl: String,
      val seriesId: String? = null,
      val episodeNumber: String? = null,
      val title: String? = null,
      val positionMs: Long = 0L,
      val durationMs: Long = 0L,
      val lastWatchedTimestamp: Long = System.currentTimeMillis()
  ) {
      val progressFraction: Float
          get() = if (durationMs > 0) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

      val isCompleted: Boolean
          get() = progressFraction >= 0.92f

      val isResumeEligible: Boolean
          get() = positionMs >= 10_000L && !isCompleted
  }
  ```
- **Engine Handling**:
  - **ExoPlayer**: Read `exoPlayer.currentPosition` and `exoPlayer.duration` periodically (every 1000ms) and on pause/stop. Auto-seek to saved position on launch if eligible.
  - **Dailymotion Embed**: Inject postMessage JavaScript listener into WebView to track `timeupdate` and `durationchange`. Dispatch `{ "command": "seek", "parameters": [seconds] }` on player start.
  - **Generic Embeds**: Periodic HTML5 video element probe (`video.currentTime` and `video.duration`) via `evaluateJavascript` to sync position.
- **Resume Toast / Snackbar**:
  - When auto-resumed, display a discreet floating pill: `"تم الاستئناف من MM:SS [البدء من البداية]"` with auto-dismiss after 4 seconds. Tapping "البدء من البداية" seeks back to `0:00`.

### 1.2 App-Wide Progress Indicators
- **Series Detail Screen (`SeriesDetailScreen.kt`)**:
  - Each `EpisodeRowItem` observes `PlaybackHistoryManager`.
  - Displays a slim progress bar along the bottom border of the episode row (Terminal Emerald for in-progress, or a "تمت المشاهدة" tag when completed >= 92%).
- **Home Screen (`LatestEpisodeCard.kt`)**:
  - Displays a subtle progress bar at the bottom of the episode thumbnail when partially watched.

### 1.3 Full Screen Lock Mode
- **Lock Button**: Added to player controls bar (lock padlock icon).
- **Behavior When Locked**:
  - Absorbs all pointer input via an overlay `Box` preventing touches from reaching player controls or WebView.
  - Intercepts system back navigation via `BackHandler` so accidental back gestures do not exit playback.
  - Any tap on the screen displays a floating unlock button pill in the corner: `"🔒 انقر لإلغاء القفل"` with a 3.5s auto-hide timer.
  - Tapping the unlock pill unlocks the player and restores interactive controls.

### 1.4 Double-Tap to Seek
- **Touch Zones**:
  - Left 35% of the screen: Double-tap rewinds 10 seconds.
  - Right 35% of the screen: Double-tap fast-forwards 10 seconds.
  - Center 30%: Single tap toggles floating controls.
- **Visual Feedback**:
  - Animated ripple badge overlay displaying `"-10 ثانية"` or `"+10 ثانية"` with quick fade-out.

### 1.5 Gesture Controls (Volume, Brightness, Scrubbing)
- **Vertical Swipe (Left Side)**: Adjusts window screen brightness (`WindowManager.LayoutParams.screenBrightness`).
- **Vertical Swipe (Right Side)**: Adjusts system media volume (`AudioManager.STREAM_MUSIC`).
- **On-Screen HUD**: Minimalist HUD indicator showing current brightness/volume icon and percentage bar that auto-dismisses upon release.

### 1.6 Next / Previous Episode Navigation
- **Navigation Arguments**: Update `NavRoutes.PLAYER` to pass `seriesId` and current `episode` index.
- **Player Controls**:
  - Add previous episode button (`⏮`) and next episode button (`⏭`).
  - Automatically queries episode list from `SeriesRepository` or navigation arguments to load the adjacent episode stream without returning to the series detail screen.

---

## 2. File Modification Roadmap

| Component | Target File | Description |
|---|---|---|
| **Data Layer** | `data/repository/PlaybackHistoryManager.kt` | Singleton persistence and reactive StateFlow for episode playback timestamps and progress |
| **Navigation** | `ui/navigation/NavGraph.kt` | Extend `NavRoutes.PLAYER` with optional `seriesId` and `episode` arguments |
| **Player UI** | `ui/player/PlayerScreen.kt` | Integrate lock mode, double-tap seek, gestures (brightness/volume), resume toast, and next/prev controls |
| **Series UI** | `ui/series/SeriesDetailScreen.kt` | Display watch progress bars and completed badges on episode rows |
| **Home UI** | `ui/components/LatestEpisodeCard.kt` | Display watch progress on recent episode cards |
| **Version** | `android/app/version.properties` | Bump SemVer `versionCode` and `versionName` with changes |

---

## 3. Verification & Testing Steps
1. Verify unit / build compilation via `./gradlew.bat :app:assembleDebug` and `./gradlew.bat :app:assembleRelease`.
2. Test resume watching: Play an episode for 30s, exit, re-enter, verify auto-resume to 30s with "Start Over" toast.
3. Test lock mode: Tap lock, tap screen, verify only unlock pill shows, unlock, verify controls return.
4. Test double-tap seek: Double tap left/right, verify 10s jump and visual indicator.
5. Test gestures: Swipe left for brightness, swipe right for volume, verify HUD.
6. Test next/previous: Verify clicking next episode switches to the next episode directly.
