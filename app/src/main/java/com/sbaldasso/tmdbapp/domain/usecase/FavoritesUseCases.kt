package com.sbaldasso.tmdbapp.domain.usecase

import com.sbaldasso.tmdbapp.domain.model.Movie
import com.sbaldasso.tmdbapp.domain.repository.MovieRepository
import javax.inject.Inject

class ObserveFavoritesUseCase @Inject constructor(private val repository: MovieRepository) {
    operator fun invoke() = repository.observeFavorites()
}

class ObserveIsFavoriteUseCase @Inject constructor(private val repository: MovieRepository) {
    operator fun invoke(movieId: Int) = repository.observeIsFavorite(movieId)
}

class SetFavoriteUseCase @Inject constructor(private val repository: MovieRepository) {
    suspend operator fun invoke(movie: Movie, favorite: Boolean) = repository.setFavorite(movie, favorite)
}
