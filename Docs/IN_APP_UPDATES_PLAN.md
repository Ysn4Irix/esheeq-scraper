# In-App Updates Implementation Plan

> **Branch:** `feature/in-app-updates`  
> **Status:** In Progress  
> **Target Version:** `2.1.0` (`versionCode: 206`)  
> **Architecture Decisions:** Based on Socratic Gate consensus (`1. A, 2. A, 3. A, 4. A`).

---

## 1. Overview & Selected Architecture

The Esheeq Android application will include a native in-app update mechanism:
1. **Detection Source (Option A):** GitHub Releases API (`https://api.github.com/repos/Ysn4Irix/esheeq-scraper/releases/latest`). Inspects latest tagged release, release notes, and finds the attached APK asset (`esheeq-vX.Y.Z.apk`).
2. **Download & Installation (Option A):** In-app download with real-time download percentage progress via OkHttp streaming into the app's cache directory, then launching the Android package installer via `FileProvider` (`content://` URI with `ACTION_VIEW`). Fallback to external browser if unknown sources permission is denied.
3. **Check Frequency & Behavior (Option A):** Automated check on launch (once per session/day via timestamp comparison) + manual check button in `TerminalTopBar` on `HomeScreen`.
4. **UI & Changelog (Option A):** Terminal-themed modal dialog with Arabic RTL layout, version delta chip (`v2.0.5` -> `v2.1.0`), release notes markdown summary, APK size badge, live progress bar during download, and "تحديث الآن" / "لاحقاً" CTAs.

---

## 2. Technical Components & Responsibilities

### A. Data Layer
- **`UpdateModels.kt`:**
  - `GithubRelease`: Models GitHub Releases API response (`tag_name`, `name`, `body`, `published_at`, `assets`).
  - `GithubAsset`: Models attached release files (`name`, `size`, `browser_download_url`).
  - `UpdateInfo`: Clean presentation model (`latestVersionName`, `currentVersionName`, `isUpdateAvailable`, `releaseNotes`, `apkUrl`, `apkSize`, `publishedAt`).
  - `DownloadState`: Sealed class (`Idle`, `Downloading(progress, bytesRead, totalBytes)`, `ReadyToInstall(apkFile)`, `Error(message)`).

### B. Business Logic & Repository Layer
- **`AppUpdateManager.kt`:**
  - `checkForUpdate(isManual: Boolean)`: Fetches latest release from GitHub API, parses SemVer, compares against `BuildConfig.VERSION_NAME` and `BuildConfig.VERSION_CODE`.
  - `downloadApk(apkUrl: String, fileName: String, onProgress: (Float, Long, Long) -> Unit): Result<File>`: Streams APK using OkHttp client with streaming response body and progress callbacks.
  - `installApk(context: Context, apkFile: File)`: Creates `FileProvider` URI, checks `canRequestPackageInstalls()` on Android 8.0+, grants read URI permission, and starts `ACTION_VIEW` intent.
  - Session-based check throttle: Stores `last_update_check_time` in `SharedPreferences` to avoid spamming GitHub API on rapid app relaunches.

### C. Android Manifest & Permissions
- **`AndroidManifest.xml`:**
  - Add `<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />`.
  - Declare `androidx.core.content.FileProvider` with authority `${applicationId}.fileprovider`.
- **`res/xml/file_paths.xml`:**
  - Define `<cache-path name="cache_updates" path="updates/" />` and `<external-cache-path name="ext_cache_updates" path="updates/" />`.

### D. UI Presentation Layer
- **`UpdateDialog.kt`:**
  - Beautiful Terminal-themed Compose Dialog matching `#08090C`, `#00FF9D` (TerminalEmerald), `#0070F3` (TerminalBlue), and Monospace typography.
  - Shows current version vs new version badge.
  - Formats release notes cleanly with scroll support.
  - Displays download progress percentage and progress bar while actively downloading.
  - Action buttons: "تحديث الآن" (Update Now), "تثبيت" (Install if downloaded), "إلغاء / لاحقاً" (Dismiss).
- **`HomeScreen.kt` & `TerminalTopBar.kt`:**
  - Expose actions slot in `TerminalTopBar`.
  - Add update check icon button with tooltip or badge if update is available.
  - Trigger launch check in `LaunchedEffect(Unit)`.

---

## 3. Implementation Steps

1. [ ] **Manifest & FileProvider:**
   - Create `android/app/src/main/res/xml/file_paths.xml`.
   - Update `android/app/src/main/AndroidManifest.xml` with permissions and `FileProvider`.
2. [ ] **Data Models:**
   - Create `android/app/src/main/java/com/esheeq/app/data/model/UpdateModels.kt`.
3. [ ] **Update Manager & Downloader:**
   - Create `android/app/src/main/java/com/esheeq/app/data/repository/AppUpdateManager.kt`.
4. [ ] **Compose Dialog:**
   - Create `android/app/src/main/java/com/esheeq/app/ui/components/UpdateDialog.kt`.
5. [ ] **TopBar & HomeScreen Integration:**
   - Update `TerminalTopBar.kt` to support action icons.
   - Update `HomeScreen.kt` to include update check trigger and state.
6. [ ] **Version Bump & Build Verification:**
   - Update `version.properties` to `versionCode=206`, `versionName=2.1.0`.
   - Verify `:app:assembleDebug` and `:app:assembleRelease`.
   - Local git commit on branch `feature/in-app-updates`.
