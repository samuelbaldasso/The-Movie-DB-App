package com.sbaldasso.tmdbapp

import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import com.sbaldasso.tmdbapp.domain.model.Movie
import com.sbaldasso.tmdbapp.domain.repository.MovieRepository
import com.sbaldasso.tmdbapp.domain.usecase.SearchMoviesUseCase
import com.sbaldasso.tmdbapp.presentation.screen.search.SearchViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private val repository = mockk<MovieRepository>()
    private val movie = Movie(1, "Batman", "", null, null, 8.0, "", 1.0)

    @Test fun `debounce only sends the latest normalized query`() = runTest {
        every { repository.searchMovies(any()) } returns flowOf(pagingData(listOf(movie)))
        val vm = SearchViewModel(SearchMoviesUseCase(repository), SavedStateHandle())
        vm.onQueryChange(" Bat ")
        val result = backgroundScope.async { vm.searchResults.asSnapshot() }
        runCurrent()
        advanceTimeBy(400)
        vm.onQueryChange(" Batman ")
        runCurrent()
        advanceTimeBy(499)
        runCurrent()
        verify(exactly = 0) { repository.searchMovies(any()) }
        advanceTimeBy(1)
        runCurrent()
        assertEquals(listOf(movie), result.await())
        verify(exactly = 1) { repository.searchMovies("Batman") }
    }

    @Test fun `short query clears old results without another request`() = runTest {
        every { repository.searchMovies("Batman") } returns flowOf(pagingData(listOf(movie)))
        val vm = SearchViewModel(SearchMoviesUseCase(repository), SavedStateHandle(mapOf("query" to "Batman")))
        assertEquals(listOf(movie), vm.searchResults.asSnapshot())
        vm.onQueryChange("Ba")
        assertTrue(vm.searchResults.asSnapshot().isEmpty())
        verify(exactly = 0) { repository.searchMovies("Ba") }
    }

    @Test fun `shortening query cancels pending debounce and persists query`() = runTest {
        val handle = SavedStateHandle()
        val vm = SearchViewModel(SearchMoviesUseCase(repository), handle)
        vm.onQueryChange("Batman")
        val result = backgroundScope.async { vm.searchResults.asSnapshot() }
        runCurrent()
        advanceTimeBy(100)
        vm.onQueryChange("B")
        runCurrent()
        advanceTimeBy(600)
        runCurrent()
        assertTrue(result.await().isEmpty())
        assertEquals("B", handle.get<String>("query"))
        verify(exactly = 0) { repository.searchMovies(any()) }
    }
}
