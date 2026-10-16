# Player Landscape Orientation Support Plan

## Overview
Enable comprehensive landscape orientation support within the video player interface (`PlayerScreen.kt`), allowing automatic full-screen horizontal playback upon entering a video, an in-player orientation toggle button, and automatic restoration to portrait mode upon exiting.

---

## Architectural Decisions & User Selections

1. **Auto-Landscape on Launch:**
   - When `PlayerScreen` is launched, the screen automatically requests `ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE` to provide an immediate edge-to-edge widescreen experience.
   - Sensor landscape accommodates both standard landscape (90°) and reverse landscape (270°) to support device charging or earphone orientation.

2. **Manual Rotation Toggle Control:**
   - Add a dedicated rotation button (`Icons.Default.ScreenRotation`) to the top floating action bar in `PlayerScreen`.
   - When in landscape, tapping toggles to portrait (`ActivityInfo.SCREEN_ORIENTATION_PORTRAIT`).
   - When in portrait, tapping toggles to landscape (`ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE`).
   - Displays a brief feedback toast pill informing the user of the active orientation mode.
   - Toggles visual state with emerald accent highlight when landscape is active.

3. **Screen Lock Integration:**
   - When the user locks controls with the Lock button (`isLocked = true`), the current orientation is locked so physical tilting does not trigger unwanted rotation.
   - Upon unlocking, sensor orientation resumes.

4. **WebChromeClient Fullscreen Integration:**
   - In `onShowCustomView` (when web embed triggers fullscreen), lock to `ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE`.
   - In `onHideCustomView`, restore to `ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE`.

5. **Safe Exit & Portrait Restoration:**
   - In `safelyNavigateBack()`, `BackHandler`, and `DisposableEffect.onDispose`:
     - Explicitly reset `activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT`.
     - Guarantees that returning to `HomeScreen` or `SeriesDetailScreen` always presents a clean upright portrait interface.

6. **Manifest Default:**
   - Declare `android:screenOrientation="portrait"` on `MainActivity` in `AndroidManifest.xml` with `tools:ignore="LockedOrientationActivity"` to preserve portrait orientation across all browsing screens while allowing `PlayerScreen` to unlock orientation dynamically at runtime.

---

## Implementation Phases

### Phase 1: AndroidManifest Configuration
- Update `MainActivity` tag in [AndroidManifest.xml](file:///c:/Users/lenovo/Desktop/Projects/antigravity/esheeq-scraper/android/app/src/main/AndroidManifest.xml) to enforce `android:screenOrientation="portrait"` by default for all non-player screens.

### Phase 2: PlayerScreen Orientation Lifecycle
- In [PlayerScreen.kt](file:///c:/Users/lenovo/Desktop/Projects/antigravity/esheeq-scraper/android/app/src/main/java/com/esheeq/app/ui/player/PlayerScreen.kt):
  - Import `android.content.pm.ActivityInfo`, `android.content.res.Configuration`, and `androidx.compose.ui.platform.LocalConfiguration`.
  - Import `Icons.Default.ScreenRotation`.
  - Read `isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE`.
  - In `DisposableEffect(activity)`:
    - On enter: request `ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE`.
    - On dispose: restore `ActivityInfo.SCREEN_ORIENTATION_PORTRAIT`.
  - In `safelyNavigateBack`:
    - Restore `ActivityInfo.SCREEN_ORIENTATION_PORTRAIT`.

### Phase 3: Player Top Controls Rotation Button & HUD
- Add orientation feedback message state `var orientationToastMessage by remember { mutableStateOf<String?>(null) }`.
- Insert the rotation toggle button in the floating controls row next to the Shield and Lock buttons.
- Display an orientation toast pill when toggled.

### Phase 4: Version Bump & Verification
- Bump `versionCode=212` and `versionName=2.3.2` in [version.properties](file:///c:/Users/lenovo/Desktop/Projects/antigravity/esheeq-scraper/android/app/version.properties).
- Compile debug and release builds with Gradle.
- Run mobile design audit.
- Commit to `main` without creating branches or pushing.
