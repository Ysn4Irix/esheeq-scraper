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
    private const val PREFS_NAME = "esheeq_prefs"
    private const val KEY_BASE_URL = "base_url"
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
        val prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
        setCustomBaseUrl(context, saved, saveToPrefs = false)
    }

    fun setCustomBaseUrl(context: android.content.Context? = null, newUrl: String, saveToPrefs: Boolean = true) {
        val trimmed = newUrl.trim()
        val formatted = if (trimmed.endsWith("/")) trimmed else "$trimmed/"
        baseUrl = formatted
        retrofit = buildRetrofit(formatted)
        service = retrofit.create(EsheeqApiService::class.java)

        if (saveToPrefs && context != null) {
            val prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_BASE_URL, formatted).apply()
        }
    }

    fun getBaseUrl(): String = baseUrl

    var service: EsheeqApiService = retrofit.create(EsheeqApiService::class.java)
        private set
}
