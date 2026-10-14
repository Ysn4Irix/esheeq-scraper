package com.esheeq.app.data.repository

import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object DailymotionStreamResolver {

    private val client = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    /**
     * Attempts to resolve the master HLS stream URL for a given Dailymotion video ID.
     * Returns null if metadata retrieval fails or stream requires browser session authentication.
     */
    suspend fun resolveStreamUrl(videoId: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = "https://www.dailymotion.com/player/metadata/video/$videoId"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                .header("Referer", "https://www.dailymotion.com/")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string() ?: return@withContext null
                val root = JsonParser.parseString(body).asJsonObject

                if (root.has("qualities")) {
                    val qualities = root.getAsJsonObject("qualities")
                    if (qualities.has("auto")) {
                        val autoArr = qualities.getAsJsonArray("auto")
                        for (i in 0 until autoArr.size()) {
                            val streamObj = autoArr.get(i).asJsonObject
                            val streamUrl = streamObj.get("url")?.asString
                            if (!streamUrl.isNullOrBlank() && streamUrl.contains(".m3u8")) {
                                // Test if the manifest is directly reachable without 403
                                val testReq = Request.Builder()
                                    .url(streamUrl)
                                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                                    .header("Referer", "https://www.dailymotion.com/")
                                    .build()
                                client.newCall(testReq).execute().use { testResp ->
                                    if (testResp.isSuccessful) {
                                        return@withContext streamUrl
                                    }
                                }
                            }
                        }
                    }
                }
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}
