package com.esheeq.app.data.model

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("message") val message: String? = null,
    @SerializedName("error") val error: Boolean = false,
    @SerializedName("code") val code: Int = 200,
    @SerializedName("data") val data: T? = null
)

data class LatestEpisodesData(
    @SerializedName("episodes") val episodes: List<LatestEpisode> = emptyList()
)

data class LatestEpisode(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("seriesTitle") val seriesTitle: String,
    @SerializedName("episode") val episode: String,
    @SerializedName("url") val url: String,
    @SerializedName("thumb") val thumb: String,
    @SerializedName("streamLink") val streamLink: String? = null
)

data class SearchData(
    @SerializedName("results") val results: List<SearchResult> = emptyList()
)

data class SearchResult(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("cover") val cover: String,
    @SerializedName("url") val url: String
)

data class SeriesDetails(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("cover") val cover: String,
    @SerializedName("description") val description: String,
    @SerializedName("actors") val actors: List<String> = emptyList(),
    @SerializedName("dailyLinks") val dailyLinks: List<DailyLink> = emptyList()
)

data class DailyLink(
    @SerializedName("episode") val episode: String,
    @SerializedName("link") val link: String? = null,
    @SerializedName("retryable") val retryable: Boolean? = null
)

data class FavoriteSeries(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("cover") val cover: String,
    @SerializedName("url") val url: String = "",
    @SerializedName("addedAt") val addedAt: Long = System.currentTimeMillis()
)

fun SeriesDetails.toFavorite(seriesId: String = "", url: String = ""): FavoriteSeries {
    val effectiveId = when {
        seriesId.isNotBlank() && seriesId.none { it.code > 127 } -> seriesId
        id.none { it.code > 127 } -> id
        seriesId.isNotBlank() -> seriesId
        else -> id
    }
    return FavoriteSeries(id = effectiveId, title = title, cover = cover, url = url)
}

fun SearchResult.toFavorite(): FavoriteSeries =
    FavoriteSeries(id = id, title = title, cover = cover, url = url)
