package com.sbaldasso.tmdbapp.presentation.screen.details

import com.sbaldasso.tmdbapp.domain.model.Movie

data class DetailsUiState(
    val isLoading: Boolean = true,
    val movie: Movie? = null,
    val error: String? = null,
    val isFavorite: Boolean = false,
    val isSavingFavorite: Boolean = false,
    val favoriteError: String? = null
)