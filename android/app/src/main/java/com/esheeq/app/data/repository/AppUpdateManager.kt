package com.esheeq.app.data.repository

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.esheeq.app.BuildConfig
import com.esheeq.app.data.model.DownloadState
import com.esheeq.app.data.model.GithubRelease
import com.esheeq.app.data.model.UpdateInfo
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

object AppUpdateManager {
    private const val PREFS_NAME = "esheeq_app_updates"
    private const val KEY_LAST_CHECK_TIME = "last_check_timestamp"
    private const val KEY_DISMISSED_VERSION = "dismissed_version"
    private const val GITHUB_LATEST_RELEASE_URL =
        "https://api.github.com/repos/Ysn4Irix/esheeq-scraper/releases/latest"
    private const val CHECK_INTERVAL_MS = 24 * 60 * 60 * 1000L // 24 hours

    private lateinit var appContext: Context
    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    private val _latestUpdateInfo = MutableStateFlow<UpdateInfo?>(null)
    val latestUpdateInfo: StateFlow<UpdateInfo?> = _latestUpdateInfo.asStateFlow()

    fun init(context: Context) {
        appContext = context.applicationContext
        prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getCurrentVersionName(): String {
        return try {
            BuildConfig.VERSION_NAME
        } catch (e: Exception) {
            val pInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                appContext.packageManager.getPackageInfo(appContext.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                appContext.packageManager.getPackageInfo(appContext.packageName, 0)
            }
            pInfo.versionName ?: "2.0.0"
        }
    }

    suspend fun checkForUpdates(isManual: Boolean = false): Result<UpdateInfo?> = withContext(Dispatchers.IO) {
        try {
            val now = System.currentTimeMillis()
            val lastCheck = prefs.getLong(KEY_LAST_CHECK_TIME, 0L)

            // If automatic check and checked recently, skip network
            if (!isManual && (now - lastCheck < CHECK_INTERVAL_MS)) {
                return@withContext Result.success(_latestUpdateInfo.value)
            }

            val request = Request.Builder()
                .url(GITHUB_LATEST_RELEASE_URL)
                .addHeader("Accept", "application/vnd.github.v3+json")
                .addHeader("User-Agent", "Esheeq-Android-App")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("GitHub API returned HTTP ${response.code}"))
            }

            val bodyString = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response body"))
            val release = gson.fromJson(bodyString, GithubRelease::class.java)

            prefs.edit().putLong(KEY_LAST_CHECK_TIME, now).apply()

            val rawTagName = release.tagName.trim()
            val cleanVersion = rawTagName.trimStart('v', 'V')
            val currentVersion = getCurrentVersionName()
            val isNewer = isNewerVersion(cleanVersion, currentVersion)

            // Find APK asset
            val apkAsset = release.assets.firstOrNull { asset ->
                asset.name.endsWith(".apk", ignoreCase = true) ||
                asset.browserDownloadUrl.endsWith(".apk", ignoreCase = true)
            }

            val downloadUrl = apkAsset?.browserDownloadUrl ?: release.htmlUrl ?: ""
            val apkSize = apkAsset?.size ?: 0L

            val info = UpdateInfo(
                latestVersionName = cleanVersion,
                currentVersionName = currentVersion,
                isUpdateAvailable = isNewer,
                releaseTitle = release.name ?: "الإصدار $cleanVersion",
                releaseNotes = release.body ?: "لا توجد تفاصيل لهذا الإصدار.",
                apkUrl = downloadUrl,
                apkSize = apkSize,
                htmlUrl = release.htmlUrl ?: "https://github.com/Ysn4Irix/esheeq-scraper/releases"
            )

            _latestUpdateInfo.value = info
            Result.success(info)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isDismissedVersion(version: String): Boolean {
        return prefs.getString(KEY_DISMISSED_VERSION, null) == version
    }

    fun dismissVersion(version: String) {
        prefs.edit().putString(KEY_DISMISSED_VERSION, version).apply()
    }

    suspend fun downloadApk(updateInfo: UpdateInfo): Result<File> = withContext(Dispatchers.IO) {
        try {
            _downloadState.value = DownloadState.Downloading(0f, 0L, updateInfo.apkSize)

            val request = Request.Builder()
                .url(updateInfo.apkUrl)
                .addHeader("User-Agent", "Esheeq-Android-App")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val err = "Download failed: HTTP ${response.code}"
                _downloadState.value = DownloadState.Error(err)
                return@withContext Result.failure(Exception(err))
            }

            val body = response.body ?: run {
                val err = "Download failed: empty response body"
                _downloadState.value = DownloadState.Error(err)
                return@withContext Result.failure(Exception(err))
            }

            val totalBytes = if (body.contentLength() > 0) body.contentLength() else updateInfo.apkSize
            val updatesDir = File(appContext.cacheDir, "updates")
            if (!updatesDir.exists()) {
                updatesDir.mkdirs()
            }

            val apkFile = File(updatesDir, "esheeq-v${updateInfo.latestVersionName}.apk")
            if (apkFile.exists()) {
                apkFile.delete()
            }

            body.byteStream().use { input ->
                FileOutputStream(apkFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalRead = 0L

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        val progress = if (totalBytes > 0) totalRead.toFloat() / totalBytes else 0f
                        _downloadState.value = DownloadState.Downloading(progress.coerceIn(0f, 1f), totalRead, totalBytes)
                    }
                    output.flush()
                }
            }

            _downloadState.value = DownloadState.ReadyToInstall(apkFile)
            Result.success(apkFile)
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: "حدث خطأ أثناء تنزيل التحديث"
            _downloadState.value = DownloadState.Error(msg)
            Result.failure(e)
        }
    }

    fun installApk(context: Context, apkFile: File): Boolean {
        return try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun openBrowser(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    fun resetDownloadState() {
        _downloadState.value = DownloadState.Idle
    }

    fun isNewerVersion(latest: String, current: String): Boolean {
        val lClean = latest.trim().trimStart('v', 'V')
        val cClean = current.trim().trimStart('v', 'V')
        if (lClean == cClean) return false

        val lParts = lClean.split('.').mapNotNull { it.toIntOrNull() }
        val cParts = cClean.split('.').mapNotNull { it.toIntOrNull() }
        val maxLen = maxOf(lParts.size, cParts.size)
        for (i in 0 until maxLen) {
            val lVal = lParts.getOrElse(i) { 0 }
            val cVal = cParts.getOrElse(i) { 0 }
            if (lVal > cVal) return true
            if (lVal < cVal) return false
        }
        return false
    }
}
