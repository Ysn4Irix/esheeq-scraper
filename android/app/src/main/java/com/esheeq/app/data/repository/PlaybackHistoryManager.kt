package com.esheeq.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class WatchProgress(
    val episodeUrl: String,
    val seriesId: String? = null,
    val episodeNumber: String? = null,
    val title: String? = null,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val lastWatchedTimestamp: Long = System.currentTimeMillis()
) {
    val progressFraction: Float
        get() = if (durationMs > 0) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    val isCompleted: Boolean
        get() = progressFraction >= 0.92f

    val isResumeEligible: Boolean
        get() = positionMs >= 10_000L && !isCompleted
}

object PlaybackHistoryManager {
    private const val PREFS_NAME = "esheeq_watch_progress"
    private const val KEY_PROGRESS_MAP = "progress_map"

    private val gson = Gson()
    private var prefs: SharedPreferences? = null

    private val _historyState = MutableStateFlow<Map<String, WatchProgress>>(emptyMap())
    val historyState: StateFlow<Map<String, WatchProgress>> = _historyState.asStateFlow()

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            loadFromDisk()
        }
    }

    private fun loadFromDisk() {
        val json = prefs?.getString(KEY_PROGRESS_MAP, null) ?: return
        try {
            val type = object : TypeToken<Map<String, WatchProgress>>() {}.type
            val map: Map<String, WatchProgress>? = gson.fromJson(json, type)
            if (map != null) {
                _historyState.value = map
            }
        } catch (_: Exception) {}
    }

    private fun persistToDisk(map: Map<String, WatchProgress>) {
        try {
            val json = gson.toJson(map)
            prefs?.edit()?.putString(KEY_PROGRESS_MAP, json)?.apply()
        } catch (_: Exception) {}
    }

    fun saveProgress(
        url: String,
        positionMs: Long,
        durationMs: Long,
        seriesId: String? = null,
        episodeNumber: String? = null,
        title: String? = null
    ) {
        if (url.isBlank() || positionMs < 0) return
        val current = _historyState.value.toMutableMap()
        val existing = current[url]
        val updated = WatchProgress(
            episodeUrl = url,
            seriesId = seriesId ?: existing?.seriesId,
            episodeNumber = episodeNumber ?: existing?.episodeNumber,
            title = title ?: existing?.title,
            positionMs = positionMs,
            durationMs = if (durationMs > 0) durationMs else (existing?.durationMs ?: 0L),
            lastWatchedTimestamp = System.currentTimeMillis()
        )
        current[url] = updated
        _historyState.value = current
        persistToDisk(current)
    }

    fun getProgress(url: String): WatchProgress? {
        return _historyState.value[url]
    }

    fun resetToBeginning(url: String) {
        val current = _historyState.value.toMutableMap()
        val existing = current[url] ?: return
        current[url] = existing.copy(positionMs = 0L)
        _historyState.value = current
        persistToDisk(current)
    }

    fun clearProgress(url: String) {
        val current = _historyState.value.toMutableMap()
        if (current.remove(url) != null) {
            _historyState.value = current
            persistToDisk(current)
        }
    }

    fun formatTime(millis: Long): String {
        val totalSeconds = (millis / 1000).coerceAtLeast(0)
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }
}
