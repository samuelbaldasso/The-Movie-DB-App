package com.sbaldasso.tmdbapp.domain.usecase

import androidx.paging.PagingData
import com.sbaldasso.tmdbapp.domain.model.Movie
import com.sbaldasso.tmdbapp.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

data class SearchMoviesParams(
    val query: String,
    val page: Int = 1
)
class SearchMoviesUseCase @Inject constructor(
    private val movieRepository: MovieRepository
) {
    operator fun invoke(query: String): Flow<PagingData<Movie>> {
        return movieRepository.searchMovies(query)
    }
}