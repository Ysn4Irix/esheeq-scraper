package com.esheeq.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.esheeq.app.data.model.FavoriteSeries
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object FavoritesManager {

    private const val PREFS_NAME = "esheeq_favorites_prefs"
    private const val KEY_FAVORITES_LIST = "saved_favorite_series"

    private val gson = Gson()
    private var prefs: SharedPreferences? = null

    private val _favoritesState = MutableStateFlow<List<FavoriteSeries>>(emptyList())
    val favoritesState: StateFlow<List<FavoriteSeries>> = _favoritesState.asStateFlow()

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            loadFromDisk()
        }
    }

    private fun loadFromDisk() {
        val json = prefs?.getString(KEY_FAVORITES_LIST, null) ?: return
        try {
            val type = object : TypeToken<List<FavoriteSeries>>() {}.type
            val list: List<FavoriteSeries>? = gson.fromJson(json, type)
            if (list != null) {
                _favoritesState.value = list
            }
        } catch (_: Exception) {}
    }

    private fun persistToDisk(list: List<FavoriteSeries>) {
        try {
            val json = gson.toJson(list)
            prefs?.edit()?.putString(KEY_FAVORITES_LIST, json)?.apply()
            _favoritesState.value = list
        } catch (_: Exception) {}
    }

    fun isFavorite(vararg seriesIds: String?): Boolean {
        val candidates = seriesIds.filterNotNull().filter { it.isNotBlank() }
        if (candidates.isEmpty()) return false
        return _favoritesState.value.any { fav -> candidates.any { it == fav.id } }
    }

    fun addFavorite(context: Context, series: FavoriteSeries) {
        init(context)
        if (series.id.isBlank()) return

        val current = _favoritesState.value.toMutableList()
        current.removeAll { it.id == series.id }
        current.add(0, series)
        persistToDisk(current)
    }

    fun updateFavoriteId(context: Context? = null, oldId: String, newId: String) {
        if (context != null) init(context)
        if (oldId.isBlank() || newId.isBlank() || oldId == newId) return
        val current = _favoritesState.value.toMutableList()
        val index = current.indexOfFirst { it.id == oldId }
        if (index != -1) {
            val old = current[index]
            current[index] = old.copy(id = newId)
            persistToDisk(current)
        }
    }

    fun removeFavorite(context: Context, seriesId: String) {
        init(context)
        if (seriesId.isBlank()) return

        val current = _favoritesState.value.toMutableList()
        val removed = current.removeAll { it.id == seriesId }
        if (removed) {
            persistToDisk(current)
        }
    }

    fun toggleFavorite(context: Context, series: FavoriteSeries, alternateId: String? = null): Boolean {
        init(context)
        val idsToMatch = listOfNotNull(series.id, alternateId).filter { it.isNotBlank() }
        val currentlyFav = _favoritesState.value.any { fav -> idsToMatch.any { it == fav.id } }
        if (currentlyFav) {
            val current = _favoritesState.value.toMutableList()
            current.removeAll { fav -> idsToMatch.any { it == fav.id } }
            persistToDisk(current)
            return false
        } else {
            addFavorite(context, series)
            return true
        }
    }

    fun clearFavorites(context: Context) {
        init(context)
        persistToDisk(emptyList())
    }
}
