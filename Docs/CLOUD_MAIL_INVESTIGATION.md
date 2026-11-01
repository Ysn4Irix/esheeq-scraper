# Cloud.mail.ru Video Playback Investigation

**Date:** 2026-11-01  
**Status:** Completed  
**Scope:** [PlayerScreen.kt](file:///c:/Users/lenovo/Desktop/Projects/antigravity/esheeq-scraper/android/app/src/main/java/com/esheeq/app/ui/player/PlayerScreen.kt), [DailymotionStreamResolver.kt](file:///c:/Users/lenovo/Desktop/Projects/antigravity/esheeq-scraper/android/app/src/main/java/com/esheeq/app/data/repository/DailymotionStreamResolver.kt), [AdBlockManager.kt](file:///c:/Users/lenovo/Desktop/Projects/antigravity/esheeq-scraper/android/app/src/main/java/com/esheeq/app/data/repository/AdBlockManager.kt)

---

## 1. Problem Overview

When playing videos sourced from `cloud.mail.ru/public/...`:
- The built-in player buttons (play/pause, seek, timeline scrubbing, fullscreen, and double-tap gestures) fail to respond or behave erratically.
- Watch progress and resume position are never saved.
- Users are trapped interacting with an unoptimized desktop cloud storage web page inside the WebView rather than a dedicated media player.

---

## 2. Root Cause Analysis

### 2.1 Engine Fallback to WEB_EMBED Instead of Native ExoPlayer
In [PlayerScreen.kt](file:///c:/Users/lenovo/Desktop/Projects/antigravity/esheeq-scraper/android/app/src/main/java/com/esheeq/app/ui/player/PlayerScreen.kt#L314-L320):
```kotlin
val activeVideoUrl = resolvedDirectStreamUrl ?: videoUrl
val isDirectMedia = activeVideoUrl.contains(".m3u8", ignoreCase = true) ||
        activeVideoUrl.contains(".mp4", ignoreCase = true)

val currentEngine = remember(activeVideoUrl, isDirectMedia) {
    if (isDirectMedia) PlayerEngine.EXO_PLAYER else PlayerEngine.WEB_EMBED
}
```
1. While Dailymotion videos are resolved into direct `.m3u8` streams via [DailymotionStreamResolver.kt](file:///c:/Users/lenovo/Desktop/Projects/antigravity/esheeq-scraper/android/app/src/main/java/com/esheeq/app/data/repository/DailymotionStreamResolver.kt), no resolver existed for `cloud.mail.ru`.
2. Because public URLs (such as `https://cloud.mail.ru/public/kpyd/EZJ88UkzX`) do not have `.mp4` or `.m3u8` file extensions, `isDirectMedia` evaluates to `false`.
3. The player defaults to `PlayerEngine.WEB_EMBED`, loading the raw web page in a `WebView`.
4. In `PlayerEngine.WEB_EMBED`, the native ExoPlayer `PlayerView` (which hosts all built-in play/pause, seek, timeline, and buffering controls) is not instantiated at all.

### 2.2 Desktop User Agent Forces Incompatible Desktop Interface
In [PlayerScreen.kt](file:///c:/Users/lenovo/Desktop/Projects/antigravity/esheeq-scraper/android/app/src/main/java/com/esheeq/app/ui/player/PlayerScreen.kt#L122):
- The WebView injects a desktop Chrome User-Agent (`Mozilla/5.0 (Windows NT 10.0; Win64; x64)...`).
- `cloud.mail.ru` serves its desktop file manager interface, requiring mouse-hover states to reveal playback bars, volume sliders, and settings.
- On touchscreens, hover-based controls do not trigger properly, making the video controls unresponsive or invisible.

### 2.3 Multiple Native Gesture Layers Intercepting Web Touches
In [PlayerScreen.kt](file:///c:/Users/lenovo/Desktop/Projects/antigravity/esheeq-scraper/android/app/src/main/java/com/esheeq/app/ui/player/PlayerScreen.kt#L916-L1053):
1. **Vertical Drag Gesture Zones**:
   - The left 14% of the screen consumes all touch input for brightness adjustment.
   - The right 14% of the screen consumes all touch input for volume adjustment.
   - Web controls located near screen boundaries (such as play/pause on the bottom-left or fullscreen on the bottom-right) cannot receive touch events.
2. **GestureDetector Conflicts**:
   - `webGestureDetector` intercepts taps and double-taps over the WebView.
   - Double-tap seeks cover the outer 35% on left and right, swallowing double-clicks intended for the web player.
   - Single-tap listener toggles floating controls instead of delivering clicks cleanly to web elements.

### 2.4 Broken JavaScript Bridge & Progress Tracking
- In `PlayerEngine.WEB_EMBED`, double-tap seek and "Start from beginning" execute:
  `document.querySelector('video').currentTime += deltaSec;`
- In `cloud.mail.ru`, the video element is not immediately attached to the DOM until after user interaction with the preview thumbnail, so initial JavaScript evaluations fail silently.
- Time update bridge callbacks (`window.AndroidBridge.onTimeUpdate`) are only injected into Dailymotion HTML templates, leaving `cloud.mail.ru` with zero playback state reporting.

---

## 3. Discovery: Direct Stream Resolution via Mail.ru Dispatcher API

Empirical testing confirmed that `cloud.mail.ru` provides a public, unauthenticated dispatcher API that returns direct, high-speed MP4 streaming URLs.

### 3.1 Step-by-Step Resolution Mechanism
1. Given a public URL: `https://cloud.mail.ru/public/kpyd/EZJ88UkzX`
2. Extract the weblink path: `kpyd/EZJ88UkzX`
3. Call the public dispatcher endpoint:
   ```http
   GET https://cloud.mail.ru/api/v2/dispatcher?weblink=kpyd/EZJ88UkzX
   ```
4. Parse the dispatcher response JSON:
   ```json
   {
     "body": {
       "weblink_view": [
         { "count": "50", "url": "https://cloclo53.cloud.mail.ru/weblink/view/" }
       ]
     },
     "status": 200
   }
   ```
5. Construct the direct streaming URL:
   `https://cloclo53.cloud.mail.ru/weblink/view/kpyd/EZJ88UkzX`

### 3.2 Live Verification Evidence
Testing the resolved URL with HTTP byte-range requests demonstrates native MP4 streaming:
```http
GET https://cloclo53.cloud.mail.ru/weblink/view/kpyd/EZJ88UkzX
HTTP/1.1 301 Moved Permanently
Location: https://cloclo53.datacloudmail.ru/public/view/.../Deniz%2033.mp4

HTTP/1.1 206 Partial Content
Content-Type: video/mp4
Accept-Ranges: bytes
Content-Range: bytes 0-100/4332083536
```
The endpoint supports full range seeking, byte-level scrubbing, and ExoPlayer direct streaming without requiring cookies or user authentication.

---

## 4. Proposed Solution

1. **Create `CloudMailStreamResolver`**:
   - Extract the public slug from `cloud.mail.ru/public/{folder}/{file}`.
   - Query `https://cloud.mail.ru/api/v2/dispatcher?weblink={slug}` asynchronously.
   - Return the direct `https://cloclo*.cloud.mail.ru/weblink/view/{slug}` stream URL.
2. **Integrate into [PlayerScreen.kt](file:///c:/Users/lenovo/Desktop/Projects/antigravity/esheeq-scraper/android/app/src/main/java/com/esheeq/app/ui/player/PlayerScreen.kt)**:
   - In the stream resolution block, check if `videoUrl` contains `cloud.mail.ru` or `mail.ru`.
   - Resolve to direct MP4 URL and assign to `resolvedDirectStreamUrl`.
   - `isDirectMedia` will automatically evaluate to `true` (or treat resolved Mail.ru streams as direct media).
   - `currentEngine` will switch to `PlayerEngine.EXO_PLAYER`.
3. **Benefits**:
   - The user gets full native ExoPlayer controls (Play/Pause, Rewind 10s, FastForward 10s, Timeline scrubbing bar, Fullscreen, gesture brightness/volume).
   - Complete elimination of WebView layout problems, desktop hover issues, and touch interception bugs on `cloud.mail.ru`.
   - Full watch progress and resume position persistence via [PlaybackHistoryManager.kt](file:///c:/Users/lenovo/Desktop/Projects/antigravity/esheeq-scraper/android/app/src/main/java/com/esheeq/app/data/repository/PlaybackHistoryManager.kt).

---

## 5. Follow-Up: Resolving the 00:00 / 00:00 Playback Failure

### 5.1 Root Cause
When `CloudMailStreamResolver` initially returned the candidate URL (`https://cloclo*.cloud.mail.ru/weblink/view/{slug}`):
1. ExoPlayer's `DefaultHttpDataSource` (backed by `HttpURLConnection`) sent the initial request to `cloud.mail.ru`.
2. The server answered with `HTTP 301 Moved Permanently` pointing to `https://cloclo*.datacloudmail.ru/public/view/...`.
3. Standard Java `HttpURLConnection` strips custom request headers (including `Referer`) when automatically following cross-domain redirects (`cloud.mail.ru` -> `datacloudmail.ru`).
4. Without `Referer: https://cloud.mail.ru/`, `datacloudmail.ru` immediately rejected the stream with `HTTP 403 Forbidden`.
5. Because ExoPlayer received an unhandled 403 error, the player stalled in `STATE_IDLE` with 0 duration, leaving the UI showing `00:00 . 00:00`.

### 5.2 Resolution
1. **Direct Location Resolution**: Configured `CloudMailStreamResolver` with `followRedirects(false)` to capture the final `Location` redirect URL (`https://cloclo*.datacloudmail.ru/.../{file}.mp4`) directly.
2. **Pre-Verified Direct Stream**: ExoPlayer receives the destination MP4 URL directly, avoiding any cross-host redirects and ensuring `Referer` and `Origin` headers remain attached.
3. **MIME Type Enforcement**: Added explicit `MimeTypes.APPLICATION_MP4` to `MediaItem.Builder` for `.mp4` URLs.
4. **Playback Error Fallback**: Added a `Player.Listener` to `exoPlayer` that automatically switches `currentEngine` back to `PlayerEngine.WEB_EMBED` upon any playback error, ensuring users are never trapped on a blank `00:00` screen.

