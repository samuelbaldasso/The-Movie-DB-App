package com.sbaldasso.tmdbapp

import androidx.paging.PagingData
import app.cash.turbine.test
import com.sbaldasso.tmdbapp.domain.model.Movie
import com.sbaldasso.tmdbapp.domain.repository.MovieRepository
import com.sbaldasso.tmdbapp.domain.usecase.GetPopularMoviesUseCase
import com.sbaldasso.tmdbapp.presentation.screen.home.HomeViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetPopularMoviesUseCaseTest {

    private lateinit var movieRepository: MovieRepository
    private lateinit var useCase: GetPopularMoviesUseCase

    @Before
    fun setup() {
        movieRepository = mockk()
        useCase = GetPopularMoviesUseCase(movieRepository)
}

@Test
fun `invoke should return flow of paging data from repository`() = runTest {
    // Given
    val movies = listOf(
        Movie(1, "Movie 1", "Overview",  null, null, 8.0, "2024-01-01", 100.0)
    )
    val pagingData = PagingData.from(movies)
    every {
        movieRepository.getPopularMoviesPaging()
    } returns flowOf(pagingData)

    // When
    val result = useCase()

    // Then
    result.test {
        val emittedPagingData = awaitItem()
        assertNotNull(emittedPagingData)
        awaitComplete()
    }
}
}