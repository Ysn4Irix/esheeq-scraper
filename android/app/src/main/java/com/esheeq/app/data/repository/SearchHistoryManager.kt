package com.esheeq.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SearchHistoryManager {

    private const val PREFS_NAME = "esheeq_search_history"
    private const val KEY_QUERIES_LIST = "recent_queries"
    private const val MAX_HISTORY_ITEMS = 12

    private val gson = Gson()
    private var prefs: SharedPreferences? = null

    private val _historyState = MutableStateFlow<List<String>>(emptyList())
    val historyState: StateFlow<List<String>> = _historyState.asStateFlow()

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            loadFromDisk()
        }
    }

    private fun loadFromDisk() {
        val json = prefs?.getString(KEY_QUERIES_LIST, null) ?: return
        try {
            val type = object : TypeToken<List<String>>() {}.type
            val list: List<String>? = gson.fromJson(json, type)
            if (list != null) {
                _historyState.value = list
            }
        } catch (_: Exception) {}
    }

    private fun persistToDisk(list: List<String>) {
        try {
            val json = gson.toJson(list)
            prefs?.edit()?.putString(KEY_QUERIES_LIST, json)?.apply()
            _historyState.value = list
        } catch (_: Exception) {}
    }

    fun addQuery(context: Context, query: String) {
        init(context)
        val trimmed = query.trim()
        if (trimmed.isBlank()) return

        val current = _historyState.value.toMutableList()
        // Deduplicate case-insensitively
        current.removeAll { it.equals(trimmed, ignoreCase = true) }
        // Prepend to top
        current.add(0, trimmed)
        // Cap to max size
        val updated = current.take(MAX_HISTORY_ITEMS)
        persistToDisk(updated)
    }

    fun removeQuery(context: Context, query: String) {
        init(context)
        val current = _historyState.value.toMutableList()
        current.removeAll { it.equals(query, ignoreCase = true) }
        persistToDisk(current)
    }

    fun clearHistory(context: Context) {
        init(context)
        persistToDisk(emptyList())
    }

    fun getHistory(context: Context): List<String> {
        init(context)
        return _historyState.value
    }
}
