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

    private val client = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val PUBLIC_URL_REGEX = Regex("""cloud\.mail\.ru/public/([a-zA-Z0-9_-]+/[a-zA-Z0-9_-]+)""")

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
     * using the public Mail.ru dispatcher API.
     */
    suspend fun resolveStreamUrl(rawUrl: String): String? = withContext(Dispatchers.IO) {
        try {
            val weblink = extractWeblinkSlug(rawUrl) ?: return@withContext null
            val dispatcherUrl = "https://cloud.mail.ru/api/v2/dispatcher?weblink=$weblink"

            val request = Request.Builder()
                .url(dispatcherUrl)
                .header("User-Agent", DESKTOP_USER_AGENT)
                .header("Referer", "https://cloud.mail.ru/")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val bodyStr = response.body?.string() ?: return@withContext null
                val root = JsonParser.parseString(bodyStr).asJsonObject
                if (!root.has("body")) return@withContext null

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
                } ?: return@withContext null

                val candidateUrl = if (baseViewUrl.endsWith("/")) {
                    "$baseViewUrl$weblink"
                } else {
                    "$baseViewUrl/$weblink"
                }

                // Verify the candidate stream URL is reachable
                val testReq = Request.Builder()
                    .url(candidateUrl)
                    .header("User-Agent", DESKTOP_USER_AGENT)
                    .header("Referer", "https://cloud.mail.ru/")
                    .header("Range", "bytes=0-100")
                    .build()

                client.newCall(testReq).execute().use { testResp ->
                    if (testResp.isSuccessful || testResp.code in 200..308) {
                        return@withContext candidateUrl
                    }
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }
}
