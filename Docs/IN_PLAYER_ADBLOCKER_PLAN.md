# In-Player AdBlocker & Stream Shield - Implementation Plan

## Overview
This document specifies the architecture and execution plan for integrating an integrated ad-blocker and stream shield into the built-in video player of the Esheeq Android application. The objective is to eliminate pop-ups, malicious redirects, click-jacking overlays, and tracker scripts during playback, while providing direct native ExoPlayer stream resolution for supported hosts (Dailymotion) with an automatic ad-blocked WebView fallback.

---

## 1. Architectural Components

### 1.1 AdBlockManager (`AdBlockManager.kt`)
- **Package**: `com.esheeq.app.data.repository`
- **Responsibilities**:
  - Maintain a curated, high-performance in-memory HashSet of video ad networks, popup engines, and telemetry domains:
    - Pop-up / Pop-under networks: PopAds, Adsterra, PropellerAds, ExoClick, Monetag, HilltopAds, Clickadu, etc.
    - Tracking & Ad servers: DoubleClick, Google Syndication, Yandex Ads, Taboola, Outbrain, Criteo, Pubmatic, etc.
    - Deceptive heuristic keyword blockers on path/query: `popunder`, `adframe`, `adserver`, `/ads/`, `/adx/`, etc.
  - Domain Whitelisting:
    - Ensure legitimate streaming domains, media CDNs, and essential video player scripts are never intercepted (`dailymotion.com`, `dmcdn.net`, `dmxleo.com`, `ok.ru`, `mail.ru`, `vk.com`, `vidshar`, `qeseh`, `qesen`).
  - Request Interception:
    - `fun isAd(uri: Uri): Boolean` checks URLs against whitelist, blacklist, and heuristics.
    - `fun getEmptyResponse(): WebResourceResponse` provides a zero-byte HTTP 200 response to silence blocked network requests without throwing network errors.
  - Cosmetic Element Hiding & Defusers:
    - Provide CSS injection rules (`display: none !important; opacity: 0 !important; pointer-events: none !important;`) targeting floating banners, transparent click-jackers, and modal overlays.
    - Provide JavaScript defuser script overriding `window.open`, `window.alert`, and stopping click-hijacking before pages render.
  - Preference Persistence:
    - Persist user toggle state via `SharedPreferences` (`isAdBlockEnabled: Boolean`, defaults to `true`).

### 1.2 Dailymotion Stream Resolver (`DailymotionStreamResolver.kt`)
- **Package**: `com.esheeq.app.data.repository`
- **Responsibilities**:
  - Interrogate Dailymotion player metadata API: `https://www.dailymotion.com/player/metadata/video/{videoId}`.
  - Parse the master `.m3u8` HLS manifest URL from `qualities.auto[0].url`.
  - Provide safe fallback: if network fails or API changes, return `null` so `PlayerScreen` seamlessly uses the ad-blocked WebView embed.

### 1.3 WebView Hardening & Anti-Popup Shield (`PlayerScreen.kt`)
- **Settings Hardening**:
  - `setSupportMultipleWindows(false)` (blocks target="_blank" window creations).
  - `javaScriptCanOpenWindowsAutomatically = false` (blocks unprompted popup scripts).
- **WebViewClient Interceptions**:
  - `shouldInterceptRequest(view, request)`: Intercept and drop ad requests via `AdBlockManager`.
  - `shouldOverrideUrlLoading(view, request)`: Enforce strict navigation policy:
    - Block non-http/https schemes (`intent://`, `market://`, etc.).
    - Block any navigation away from the primary streaming host or main frame.
  - `onPageStarted` & `onPageFinished`: Inject defuser JS and cosmetic hiding CSS.
- **WebChromeClient Hardening**:
  - `onCreateWindow`: Always return `false` to reject popup windows.
  - `onJsAlert`, `onJsConfirm`: Suppress intrusive ad alert dialogs.

### 1.4 Player UI & Shield Toggle (`PlayerScreen.kt`)
- **AdBlock Shield Button**:
  - Added to the top bar controls in `PlayerScreen.kt`.
  - Visual status: Active Shield icon with Terminal Emerald glow when active; muted gray when disabled.
  - Tapping toggles shield state, persists to `AdBlockManager`, triggers an informative feedback pill ("درع الإعلانات: مفعل" / "درع الإعلانات: معطل"), and reloads embed if necessary.

---

## 2. Implementation Steps

1. **Create `AdBlockManager.kt`**:
   - Curated streaming ad/popup domains.
   - Resource interception response helpers.
   - Cosmetic CSS and JS defuser strings.
   - Toggle persistence via `SharedPreferences`.

2. **Create `DailymotionStreamResolver.kt`**:
   - Asynchronous metadata fetch and master `.m3u8` extraction.

3. **Enhance `PlayerScreen.kt`**:
   - Integrate Dailymotion direct stream resolution into `LaunchedEffect(videoUrl)`.
   - Update `WebView` configuration (`shouldInterceptRequest`, `shouldOverrideUrlLoading`, `onCreateWindow`, `settings`).
   - Add Shield toggle button to the top player bar and show status feedback.

4. **Update `version.properties`**:
   - Bump to `versionCode=209` and `versionName=2.2.0` (new feature `feat(android)`).

5. **Build & Quality Verification**:
   - Run `:app:assembleDebug` and `:app:assembleRelease`.
   - Verify `output-metadata.json`.
   - Run mobile design audit script.
   - Commit directly to `main`.
