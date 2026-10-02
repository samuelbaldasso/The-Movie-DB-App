package com.sbaldasso.tmdbapp

import androidx.paging.PagingSource
import com.sbaldasso.tmdbapp.data.paging.SearchMoviePagingSource
import com.sbaldasso.tmdbapp.data.remote.api.TMDBApiService
import com.sbaldasso.tmdbapp.data.remote.dto.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class SearchMoviePagingSourceTest {
    private val api = mockk<TMDBApiService>()
    private val source = SearchMoviePagingSource(api, "film")
    private val dto = MovieDto(1, "Film", "", voteAverage = 8.0, releaseDate = "", popularity = 1.0)

    @Test fun `last nonempty API page ends pagination`() = runTest {
        coEvery { api.searchMovies("film", 2) } returns MoviesResponse(2, listOf(dto), 2, 21)
        val result = source.load(PagingSource.LoadParams.Append(2, 20, false)) as PagingSource.LoadResult.Page
        assertEquals(1, result.data.single().id)
        assertEquals(1, result.prevKey)
        assertNull(result.nextKey)
    }

    @Test fun `network error is retryable and cancellation propagates`() = runTest {
        val params = PagingSource.LoadParams.Refresh<Int>(null, 20, false)
        val error = IOException("offline")
        coEvery { api.searchMovies("film", 1) } throws error
        assertSame(error, (source.load(params) as PagingSource.LoadResult.Error).throwable)
        coEvery { api.searchMovies("film", 1) } throws CancellationException()
        try { source.load(params); fail("Expected cancellation") } catch (_: CancellationException) { }
    }
}
