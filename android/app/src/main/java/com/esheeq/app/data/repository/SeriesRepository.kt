package com.esheeq.app.data.repository

import com.esheeq.app.data.api.ApiClient
import com.esheeq.app.data.api.EsheeqApiService
import com.esheeq.app.data.model.LatestEpisode
import com.esheeq.app.data.model.SearchResult
import com.esheeq.app.data.model.SeriesDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SeriesRepository(
    private val apiService: EsheeqApiService = ApiClient.service
) {
    suspend fun getLatestEpisodes(count: Int = 5): Result<List<LatestEpisode>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getLatestEpisodes(count)
            if (!response.error && response.data != null) {
                Result.success(response.data.episodes)
            } else {
                Result.failure(Exception(response.message ?: "Failed to load latest episodes"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchSeries(query: String): Result<List<SearchResult>> = withContext(Dispatchers.IO) {
        try {
            val cleanQuery = query.trim()
            if (cleanQuery.isEmpty()) {
                return@withContext Result.success(emptyList())
            }
            val response = apiService.searchSeries(cleanQuery)
            if (!response.error && response.data != null) {
                Result.success(response.data.results)
            } else {
                Result.failure(Exception(response.message ?: "Search failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSeriesDetails(seriesId: String): Result<SeriesDetails> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getSeriesDetails(seriesId)
            if (!response.error && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Failed to fetch series details"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
