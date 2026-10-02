package com.sbaldasso.tmdbapp

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import com.sbaldasso.tmdbapp.domain.model.Movie
import com.sbaldasso.tmdbapp.domain.repository.MovieRepository
import com.sbaldasso.tmdbapp.domain.usecase.GetPopularMoviesUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetPopularMoviesUseCaseTest {
    @Test fun `invoke returns repository movies`() = runTest {
        val repository = mockk<MovieRepository>()
        val movies = listOf(Movie(1, "Film", "", null, null, 8.0, "", 1.0))
        every { repository.getPopularMoviesPaging() } returns flowOf(pagingData(movies))
        assertEquals(movies, GetPopularMoviesUseCase(repository)().asSnapshot())
        verify(exactly = 1) { repository.getPopularMoviesPaging() }
    }
}
