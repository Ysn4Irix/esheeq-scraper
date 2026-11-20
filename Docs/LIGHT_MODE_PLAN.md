# Light Mode Implementation Plan

**Date:** 2026-11-20  
**Target Version:** `2.6.0` (`versionCode=220`)

## Goals
- Add full Light Mode support using a crisp GitHub Light palette paired with the existing GitHub Dark ("Terminal") palette.
- Support System Default, Light Mode, and Dark Mode via a persisted in-app selector in the top bar.
- Keep `PlayerScreen` cinema viewport and on-screen controls dark while ensuring exiting the player restores the active Light/Dark system bar styling cleanly.
- Ensure every text and surface pairing meets WCAG AA contrast (>= 4.5:1).

## Architecture
1. **Persistence (`ThemeManager.kt`)**:
   - `AppThemeMode` enum (`SYSTEM`, `LIGHT`, `DARK`) stored in `SharedPreferences` (`esheeq_prefs`), exposed via `StateFlow<AppThemeMode>`, initialized in `EsheeqApp.onCreate()`.
2. **Semantic Theme System (`Color.kt`, `Theme.kt`, `Type.kt`)**:
   - Define `EsheeqColors` (`DarkEsheeqColors` and `LightEsheeqColors`) provided via `LocalEsheeqColors` and `EsheeqTheme.colors`.
   - Wire both `MaterialTheme.colorScheme` (`darkColorScheme` / `lightColorScheme`) and `EsheeqColors`, plus dynamic status/navigation bar tinting and light/dark system bar icons.
   - Remove hardcoded color overrides from `Type.kt`.
3. **Cold-Start XML Themes (`res/values` & `res/values-night`)**:
   - Light base theme (`Theme.Material.Light.NoActionBar` + light colors) in `values/` and dark override (`Theme.Material.NoActionBar` + `#0D1117`) in `values-night/`.
4. **UI Screen & Component Migration**:
   - Migrate `MainActivity`, `TerminalTopBar`, `SearchBarComponent`, `LatestEpisodeCard`, `SeriesCard`, `FavoriteSeriesCard`, `StatusBadge`, `UpdateDialog`, `HomeScreen`, and `SeriesDetailScreen` from hardcoded `Terminal*` globals to `EsheeqTheme.colors`.
   - Preserve dark scrim overlays on poster thumbnails so overlaid text remains high-contrast.
   - Add theme mode selector menu in `HomeScreen`'s `TerminalTopBar`.
   - Update `PlayerScreen`'s `DisposableEffect` cleanup to restore the active theme's status/navigation bar colors and icon appearance.
