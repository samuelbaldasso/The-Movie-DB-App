package com.sbaldasso.tmdbapp

import androidx.lifecycle.SavedStateHandle
import com.sbaldasso.tmdbapp.domain.model.Movie
import com.sbaldasso.tmdbapp.domain.repository.MovieRepository
import com.sbaldasso.tmdbapp.domain.usecase.*
import com.sbaldasso.tmdbapp.presentation.screen.details.DetailsViewModel
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class DetailsViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private val repository = mockk<MovieRepository>()
    private val movie = Movie(1, "Film", "", null, null, 8.0, "", 1.0)

    private fun create(): DetailsViewModel {
        every { repository.observeIsFavorite(1) } returns flowOf(false)
        return DetailsViewModel(
            GetMovieDetailsUseCase(repository, main.dispatcher),
            SavedStateHandle(mapOf("movieId" to 1)),
            ObserveIsFavoriteUseCase(repository), SetFavoriteUseCase(repository)
        )
    }

    @Test fun `details expose loading error and recover on retry`() = runTest {
        coEvery { repository.getMovieDetails(1) } returns Result.failure(IOException("offline"))
        val vm = create()
        assertTrue(vm.uiState.value.isLoading)
        runCurrent()
        assertEquals("offline", vm.uiState.value.error)
        assertFalse(vm.uiState.value.isLoading)
        coEvery { repository.getMovieDetails(1) } returns Result.success(movie)
        vm.retry()
        runCurrent()
        assertEquals(movie, vm.uiState.value.movie)
        assertNull(vm.uiState.value.error)
    }

    @Test fun `favorite failure leaves selection unchanged and retry succeeds`() = runTest {
        coEvery { repository.getMovieDetails(1) } returns Result.success(movie)
        coEvery { repository.setFavorite(movie, true) } throws IOException("disk")
        val vm = create()
        runCurrent()
        vm.toggleFavorite()
        runCurrent()
        assertFalse(vm.uiState.value.isFavorite)
        assertFalse(vm.uiState.value.isSavingFavorite)
        assertNotNull(vm.uiState.value.favoriteError)
        coEvery { repository.setFavorite(movie, true) } just Runs
        vm.toggleFavorite()
        runCurrent()
        assertTrue(vm.uiState.value.isFavorite)
        assertNull(vm.uiState.value.favoriteError)
        coEvery { repository.setFavorite(movie, false) } just Runs
        vm.toggleFavorite()
        runCurrent()
        assertFalse(vm.uiState.value.isFavorite)
        coVerify(exactly = 1) { repository.setFavorite(movie, false) }
    }
}
