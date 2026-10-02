package com.sbaldasso.tmdbapp.presentation.screen.details

import com.sbaldasso.tmdbapp.domain.usecase.ObserveIsFavoriteUseCase
import com.sbaldasso.tmdbapp.domain.usecase.SetFavoriteUseCase
import kotlinx.coroutines.CancellationException
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sbaldasso.tmdbapp.domain.usecase.GetMovieDetailsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val getMovieDetailsUseCase: GetMovieDetailsUseCase,
    savedStateHandle: SavedStateHandle,
    observeIsFavoriteUseCase: ObserveIsFavoriteUseCase,
    private val setFavoriteUseCase: SetFavoriteUseCase
) : ViewModel() {

    private val movieId: Int = checkNotNull(savedStateHandle["movieId"])

    private val _uiState = MutableStateFlow(DetailsUiState())
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    init {
        loadMovieDetails()
        viewModelScope.launch {
            try {
                observeIsFavoriteUseCase(movieId).collect { favorite ->
                    _uiState.update { it.copy(isFavorite = favorite) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(favoriteError = "Não foi possível carregar os favoritos.") }
            }
        }
    }

    private fun loadMovieDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getMovieDetailsUseCase(movieId)
                .onSuccess { movie ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            movie = movie
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Erro ao carregar detalhes"
                        )
                    }
                }
        }
    }

    fun toggleFavorite() {
        val state = _uiState.value
        val movie = state.movie ?: return
        if (state.isSavingFavorite) return
        _uiState.update { it.copy(isSavingFavorite = true, favoriteError = null) }
        viewModelScope.launch {
            try {
                setFavoriteUseCase(movie, !state.isFavorite)
                _uiState.update { it.copy(isFavorite = !state.isFavorite) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(favoriteError = "Não foi possível salvar. Tente novamente.") }
            } finally {
                _uiState.update { it.copy(isSavingFavorite = false) }
            }
        }
    }

    fun retry() {
        loadMovieDetails()
    }
}