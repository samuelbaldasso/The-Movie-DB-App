package com.sbaldasso.tmdbapp.presentation.screen.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sbaldasso.tmdbapp.domain.model.Movie
import com.sbaldasso.tmdbapp.domain.usecase.ObserveFavoritesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val movies: List<Movie> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val observeFavoritesUseCase: ObserveFavoritesUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState = _uiState.asStateFlow()
    private var observeJob: Job? = null

    init { retry() }

    fun retry() {
        observeJob?.cancel()
        _uiState.value = FavoritesUiState()
        observeJob = viewModelScope.launch {
            try {
                observeFavoritesUseCase().collect {
                    _uiState.value = FavoritesUiState(movies = it, loading = false)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = FavoritesUiState(loading = false, error = "Não foi possível carregar os favoritos.")
            }
        }
    }
}
