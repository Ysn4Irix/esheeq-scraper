package com.esheeq.app.ui.series

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esheeq.app.data.model.SeriesDetails
import com.esheeq.app.data.repository.SeriesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SeriesUiState(
    val isLoading: Boolean = true,
    val series: SeriesDetails? = null,
    val error: String? = null
)

class SeriesViewModel(
    private val repository: SeriesRepository = SeriesRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeriesUiState())
    val uiState: StateFlow<SeriesUiState> = _uiState.asStateFlow()

    fun loadSeries(seriesId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.getSeriesDetails(seriesId)
            result.onSuccess { details ->
                _uiState.update { it.copy(series = details, isLoading = false) }
            }.onFailure { err ->
                _uiState.update { it.copy(error = err.message, isLoading = false) }
            }
        }
    }
}
