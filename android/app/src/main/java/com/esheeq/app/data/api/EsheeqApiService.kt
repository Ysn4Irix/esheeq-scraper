package com.esheeq.app.data.api

import com.esheeq.app.data.model.ApiResponse
import com.esheeq.app.data.model.LatestEpisodesData
import com.esheeq.app.data.model.SearchData
import com.esheeq.app.data.model.SeriesDetails
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface EsheeqApiService {

    @GET("latest")
    suspend fun getLatestEpisodes(
        @Query("count") count: Int = 5
    ): ApiResponse<LatestEpisodesData>

    @GET("search/{query}")
    suspend fun searchSeries(
        @Path("query") query: String
    ): ApiResponse<SearchData>

    @GET("scrape/{id}")
    suspend fun getSeriesDetails(
        @Path("id") seriesId: String
    ): ApiResponse<SeriesDetails>
}

object ApiClient {
    private const val DEFAULT_BASE_URL = "https://esheeq.ysnirix.xyz/api/"

    @Volatile
    private var baseUrl: String = DEFAULT_BASE_URL

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private var retrofit: Retrofit = buildRetrofit(DEFAULT_BASE_URL)

    private fun buildRetrofit(url: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(url)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun init(context: android.content.Context) {
        val configuredUrl = loadConfigUrl(context)
        setBaseUrl(configuredUrl)
    }

    private fun loadConfigUrl(context: android.content.Context): String {
        return try {
            context.assets.open("config.json").use { stream ->
                val json = stream.bufferedReader().use { it.readText() }
                val jsonObj = org.json.JSONObject(json)
                val url = jsonObj.optString("api_base_url", DEFAULT_BASE_URL).trim()
                if (url.isNotEmpty()) url else DEFAULT_BASE_URL
            }
        } catch (_: Exception) {
            DEFAULT_BASE_URL
        }
    }

    fun setBaseUrl(newUrl: String) {
        val trimmed = newUrl.trim()
        val formatted = if (trimmed.endsWith("/")) trimmed else "$trimmed/"
        baseUrl = formatted
        retrofit = buildRetrofit(formatted)
        service = retrofit.create(EsheeqApiService::class.java)
    }

    fun getBaseUrl(): String = baseUrl

    var service: EsheeqApiService = retrofit.create(EsheeqApiService::class.java)
        private set
}
