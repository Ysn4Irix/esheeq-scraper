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
        val trimmedId = seriesId.trim()
        if (trimmedId.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Series ID cannot be empty"))
        }

        // Check if ID contains non-ASCII characters (e.g. Arabic slug) which the scrape endpoint rejects
        val hasNonAscii = trimmedId.any { it.code > 127 }
        if (!hasNonAscii) {
            try {
                val response = apiService.getSeriesDetails(trimmedId)
                if (!response.error && response.data != null) {
                    val details = response.data.copy(id = trimmedId)
                    // If previously saved under an Arabic slug, update it in favorites
                    if (FavoritesManager.isFavorite(response.data.id)) {
                        FavoritesManager.updateFavoriteId(oldId = response.data.id, newId = trimmedId)
                    }
                    return@withContext Result.success(details)
                }
            } catch (_: Exception) {
                // Endpoint failed or returned 500; proceed to fallback resolution
            }
        }

        // Fallback / Resolution: Search for the title to obtain the valid English slug
        try {
            val query = trimmedId
                .replace("-", " ")
                .replace("مسلسل", "")
                .trim()
                .ifEmpty { trimmedId.replace("-", " ").trim() }

            val searchResult = searchSeries(query).getOrNull()
            val resolvedId = searchResult?.firstOrNull()?.id
            if (!resolvedId.isNullOrBlank() && resolvedId != trimmedId) {
                val response = apiService.getSeriesDetails(resolvedId)
                if (!response.error && response.data != null) {
                    // Update any existing favorite with the repaired slug
                    FavoritesManager.updateFavoriteId(oldId = trimmedId, newId = resolvedId)
                    val details = response.data.copy(id = resolvedId)
                    return@withContext Result.success(details)
                }
            }
            Result.failure(Exception("Failed to fetch series details"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
