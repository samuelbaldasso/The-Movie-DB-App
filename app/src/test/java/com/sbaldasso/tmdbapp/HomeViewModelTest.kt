package com.sbaldasso.tmdbapp

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import com.sbaldasso.tmdbapp.domain.model.Movie
import com.sbaldasso.tmdbapp.domain.usecase.GetPopularMoviesUseCase
import com.sbaldasso.tmdbapp.presentation.screen.home.HomeViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    @Test fun `moviesFlow emits use case movies`() = runTest {
        val useCase = mockk<GetPopularMoviesUseCase>()
        val movies = listOf(Movie(1, "Film", "", null, null, 8.0, "", 1.0))
        every { useCase() } returns flowOf(pagingData(movies))
        assertEquals(movies, HomeViewModel(useCase).moviesFlow.asSnapshot())
    }
}
