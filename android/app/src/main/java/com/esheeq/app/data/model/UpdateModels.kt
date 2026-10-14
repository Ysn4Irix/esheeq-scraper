package com.esheeq.app.data.model

import com.google.gson.annotations.SerializedName
import java.io.File
import java.util.Locale

data class GithubRelease(
    @SerializedName("tag_name")
    val tagName: String = "",
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("body")
    val body: String? = null,
    @SerializedName("published_at")
    val publishedAt: String? = null,
    @SerializedName("html_url")
    val htmlUrl: String? = null,
    @SerializedName("assets")
    val assets: List<GithubAsset> = emptyList()
)

data class GithubAsset(
    @SerializedName("name")
    val name: String = "",
    @SerializedName("size")
    val size: Long = 0L,
    @SerializedName("browser_download_url")
    val browserDownloadUrl: String = "",
    @SerializedName("content_type")
    val contentType: String? = null
)

data class UpdateInfo(
    val latestVersionName: String,
    val currentVersionName: String,
    val isUpdateAvailable: Boolean,
    val releaseTitle: String,
    val releaseNotes: String,
    val apkUrl: String,
    val apkSize: Long,
    val htmlUrl: String
) {
    val formattedSize: String
        get() {
            if (apkSize <= 0) return ""
            val mb = apkSize.toDouble() / (1024 * 1024)
            return String.format(Locale.US, "%.1f MB", mb)
        }
}

sealed class DownloadState {
    object Idle : DownloadState()
    data class Downloading(
        val progress: Float,
        val bytesDownloaded: Long,
        val totalBytes: Long
    ) : DownloadState()
    data class ReadyToInstall(val apkFile: File) : DownloadState()
    data class Error(val message: String) : DownloadState()
}
