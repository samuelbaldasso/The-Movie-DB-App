package com.sbaldasso.tmdbapp.domain.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.sbaldasso.tmdbapp.data.local.AppDatabase
import com.sbaldasso.tmdbapp.data.local.dao.MovieDao
import com.sbaldasso.tmdbapp.data.local.mapper.toDomain
import com.sbaldasso.tmdbapp.data.local.mapper.toEntity
import com.sbaldasso.tmdbapp.data.paging.MovieRemoteMediator
import com.sbaldasso.tmdbapp.data.paging.SearchMoviePagingSource
import com.sbaldasso.tmdbapp.data.remote.api.TMDBApiService
import com.sbaldasso.tmdbapp.data.remote.mapper.toDomain
import com.sbaldasso.tmdbapp.domain.model.Movie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MovieRepositoryImpl @Inject constructor(
    private val apiService: TMDBApiService,
    private val movieDao: MovieDao,
    private val database: AppDatabase
) : MovieRepository {

    override suspend fun getMovieDetails(movieId: Int): Result<Movie> {
        return try {
            val movieDto = apiService.getMovieDetails(movieId)
            val movie = movieDto.toDomain()

            movieDao.insertMovie(movie.toEntity(page = 0))

            Result.success(movie)
        } catch (e: Exception) {
            try {
                val cachedMovie = movieDao.getMovieById(movieId)
                if (cachedMovie != null) {
                    Result.success(cachedMovie.toDomain())
                } else {
                    Result.failure(e)
                }
            } catch (cacheException: Exception) {
                Result.failure(e)
            }
        }
    }

    override fun searchMovies(query: String): Flow<PagingData<Movie>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                prefetchDistance = 2,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                SearchMoviePagingSource(apiService, query)
            }
        ).flow
    }

    @OptIn(ExperimentalPagingApi::class)
    override fun getPopularMoviesPaging(): Flow<PagingData<Movie>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                prefetchDistance = 2,
                enablePlaceholders = false
            ),
            remoteMediator = MovieRemoteMediator(
                apiService = apiService,
                database = database
            ),
            pagingSourceFactory = {
                database.movieDao().getMoviesPagingSource()
            }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }
}
