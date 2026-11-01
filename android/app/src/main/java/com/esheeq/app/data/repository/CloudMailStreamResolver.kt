package com.esheeq.app.data.repository

import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object CloudMailStreamResolver {

    private const val DESKTOP_USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"

    // Non-redirecting client to capture 301/302 Location header directly
    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .followRedirects(false)
        .build()

    private val PUBLIC_URL_REGEX = Regex("""(?:cloud\.mail\.ru|mail\.ru)/public/([^?&#\s]+/[^?&#\s/]+)""")

    fun isCloudMailUrl(rawUrl: String): Boolean {
        val trimmed = rawUrl.trim()
        return trimmed.contains("cloud.mail.ru/public/") || trimmed.contains("mail.ru/public/")
    }

    fun extractWeblinkSlug(rawUrl: String): String? {
        val trimmed = rawUrl.trim()
        val match = PUBLIC_URL_REGEX.find(trimmed)
        return match?.groupValues?.getOrNull(1)
    }

    /**
     * Resolves the direct streamable MP4 URL for a public cloud.mail.ru link
     * by querying the Mail.ru dispatcher API and extracting the direct streaming target.
     */
    suspend fun resolveStreamUrl(rawUrl: String): String? = withContext(Dispatchers.IO) {
        try {
            val weblink = extractWeblinkSlug(rawUrl) ?: return@withContext null
            val dispatcherUrl = "https://cloud.mail.ru/api/v2/dispatcher?weblink=$weblink"

            val dispRequest = Request.Builder()
                .url(dispatcherUrl)
                .header("User-Agent", DESKTOP_USER_AGENT)
                .header("Referer", "https://cloud.mail.ru/")
                .build()

            val candidateUrl = client.newCall(dispRequest).execute().use { response ->
                if (!response.isSuccessful) return@use null
                val bodyStr = response.body?.string() ?: return@use null
                val root = JsonParser.parseString(bodyStr).asJsonObject
                if (!root.has("body")) return@use null

                val bodyObj = root.getAsJsonObject("body")
                val baseViewUrl = when {
                    bodyObj.has("weblink_view") -> {
                        val arr = bodyObj.getAsJsonArray("weblink_view")
                        if (arr.size() > 0) arr.get(0).asJsonObject.get("url")?.asString else null
                    }
                    bodyObj.has("weblink_video") -> {
                        val arr = bodyObj.getAsJsonArray("weblink_video")
                        if (arr.size() > 0) arr.get(0).asJsonObject.get("url")?.asString else null
                    }
                    else -> null
                } ?: return@use null

                if (baseViewUrl.endsWith("/")) {
                    "$baseViewUrl$weblink"
                } else {
                    "$baseViewUrl/$weblink"
                }
            } ?: return@withContext null

            // Query candidate URL without following redirects to extract direct Location target
            val targetReq = Request.Builder()
                .url(candidateUrl)
                .header("User-Agent", DESKTOP_USER_AGENT)
                .header("Referer", "https://cloud.mail.ru/")
                .header("Range", "bytes=0-100")
                .build()

            var directStreamUrl: String? = null
            client.newCall(targetReq).execute().use { resp ->
                if (resp.code in 300..308) {
                    val location = resp.header("Location")
                    if (!location.isNullOrBlank()) {
                        directStreamUrl = location
                    }
                } else if (resp.isSuccessful) {
                    directStreamUrl = candidateUrl
                }
            }

            val finalUrl = directStreamUrl ?: candidateUrl

            // Test final stream URL with Range request and Referer header
            val verifyReq = Request.Builder()
                .url(finalUrl)
                .header("User-Agent", DESKTOP_USER_AGENT)
                .header("Referer", "https://cloud.mail.ru/")
                .header("Range", "bytes=0-100")
                .build()

            client.newCall(verifyReq).execute().use { verifyResp ->
                if (verifyResp.isSuccessful || verifyResp.code in 200..308) {
                    return@withContext finalUrl
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }
}

