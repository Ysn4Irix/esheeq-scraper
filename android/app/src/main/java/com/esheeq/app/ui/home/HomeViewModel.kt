package com.esheeq.app.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esheeq.app.data.model.LatestEpisode
import com.esheeq.app.data.model.SearchResult
import com.esheeq.app.data.repository.SearchHistoryManager
import com.esheeq.app.data.repository.SeriesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val latestEpisodes: List<LatestEpisode> = emptyList(),
    val isLatestLoading: Boolean = false,
    val latestError: String? = null,

    val searchQuery: String = "",
    val searchResults: List<SearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val searchError: String? = null
)

class HomeViewModel(
    private val repository: SeriesRepository = SeriesRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val searchHistory: StateFlow<List<String>> = SearchHistoryManager.historyState

    private var searchJob: Job? = null

    init {
        loadLatestEpisodes()
    }

    fun loadLatestEpisodes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLatestLoading = true, latestError = null) }
            val result = repository.getLatestEpisodes(count = 5)
            result.onSuccess { episodes ->
                _uiState.update { it.copy(latestEpisodes = episodes, isLatestLoading = false) }
            }.onFailure { err ->
                _uiState.update { it.copy(latestError = err.message, isLatestLoading = false) }
            }
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _uiState.update { it.copy(searchQuery = newQuery) }

        searchJob?.cancel()

        if (newQuery.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false, searchError = null) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(350) // Debounce search
            _uiState.update { it.copy(isSearching = true, searchError = null) }
            val result = repository.searchSeries(newQuery)
            result.onSuccess { results ->
                _uiState.update { it.copy(searchResults = results, isSearching = false) }
            }.onFailure { err ->
                _uiState.update { it.copy(searchError = err.message, isSearching = false) }
            }
        }
    }

    fun recordSearch(context: Context, query: String = _uiState.value.searchQuery) {
        val trimmed = query.trim()
        if (trimmed.isNotBlank()) {
            SearchHistoryManager.addQuery(context, trimmed)
        }
    }

    fun removeHistoryItem(context: Context, query: String) {
        SearchHistoryManager.removeQuery(context, query)
    }

    fun clearHistory(context: Context) {
        SearchHistoryManager.clearHistory(context)
    }
}
