package com.sbaldasso.tmdbapp.domain.repository

import androidx.paging.PagingData
import com.sbaldasso.tmdbapp.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    fun observeFavorites(): Flow<List<Movie>>
    fun observeIsFavorite(movieId: Int): Flow<Boolean>
    suspend fun setFavorite(movie: Movie, favorite: Boolean)
    suspend fun getMovieDetails(movieId: Int): Result<Movie>
    fun searchMovies(query: String): Flow<PagingData<Movie>>
    fun getPopularMoviesPaging(): Flow<PagingData<Movie>>
}