package com.sbaldasso.tmdbapp.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.sbaldasso.tmdbapp.data.local.AppDatabase
import com.sbaldasso.tmdbapp.data.local.entity.MovieEntity
import com.sbaldasso.tmdbapp.data.local.entity.MovieRemoteKeysEntity
import com.sbaldasso.tmdbapp.data.local.mapper.toEntity
import com.sbaldasso.tmdbapp.data.remote.api.TMDBApiService

@OptIn(ExperimentalPagingApi::class)
class MovieRemoteMediator(
    private val apiService: TMDBApiService,
    private val database: AppDatabase
) : RemoteMediator<Int, MovieEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, MovieEntity>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> 1
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }

        return try {
            val response = apiService.getPopularMovies(page)
            val isEndOfList = response.results.isEmpty()

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.movieRemoteKeysDao().clearRemoteKeys()
                    database.movieDao().deleteAllMovies()
                }

                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (isEndOfList) null else page + 1

                val keys = response.results.map {
                    MovieRemoteKeysEntity(movieId = it.id, prevKey = prevKey, nextKey = nextKey)
                }

                val entities = response.results.map { it.toEntity(page) }

                database.movieRemoteKeysDao().insertAll(keys)
                database.movieDao().insertMovies(entities)
            }

            MediatorResult.Success(endOfPaginationReached = isEndOfList)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, MovieEntity>): MovieRemoteKeysEntity? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }
            ?.data?.lastOrNull()
            ?.let { movie ->
                database.movieRemoteKeysDao().getRemoteKeysForMovie(movie.id)
            }
    }
}