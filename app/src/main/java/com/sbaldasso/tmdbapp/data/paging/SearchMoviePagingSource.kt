package com.sbaldasso.tmdbapp.data.paging

import androidx.compose.ui.input.key.key
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.sbaldasso.tmdbapp.data.remote.api.TMDBApiService
import com.sbaldasso.tmdbapp.data.remote.mapper.toDomain
import com.sbaldasso.tmdbapp.domain.model.Movie

class SearchMoviePagingSource(
    private val apiService: TMDBApiService, private val query: String
) : PagingSource<Int, Movie>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        val position = params.key ?: 1
        return try {
            val response = apiService.searchMovies(query, position)
            val movies = response.results.map { it.toDomain() }
            LoadResult.Page(
                data = movies,
                prevKey = if (position == 1) null else position - 1,
                nextKey = if (movies.isEmpty()) null else position + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}