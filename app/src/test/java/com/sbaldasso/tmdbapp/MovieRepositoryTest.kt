package com.sbaldasso.tmdbapp

import com.sbaldasso.tmdbapp.data.local.AppDatabase
import com.sbaldasso.tmdbapp.data.local.dao.*
import com.sbaldasso.tmdbapp.data.local.entity.*
import com.sbaldasso.tmdbapp.data.local.mapper.toEntity
import com.sbaldasso.tmdbapp.data.remote.api.TMDBApiService
import com.sbaldasso.tmdbapp.data.remote.dto.MovieDto
import com.sbaldasso.tmdbapp.data.repository.MovieRepositoryImpl
import com.sbaldasso.tmdbapp.domain.model.Movie
import io.mockk.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class MovieRepositoryTest {
    private val api = mockk<TMDBApiService>()
    private val popular = mockk<MovieDao>(relaxed = true)
    private val details = mockk<MovieDetailsDao>(relaxed = true)
    private val favorites = mockk<FavoriteMovieDao>(relaxed = true)
    private val db = mockk<AppDatabase> {
        every { movieDetailsDao() } returns details
        every { favoriteMovieDao() } returns favorites
    }
    private val repository = MovieRepositoryImpl(api, popular, db)
    private val movie = Movie(7, "Film", "Synopsis", null, null, 8.0, "2026-01-01", 10.0)

    @Test fun `network details are stored independently of popular feed`() = runTest {
        coEvery { api.getMovieDetails(7) } returns MovieDto(7, movie.title, movie.overview, null, null, 8.0, movie.releaseDate, 10.0)
        assertEquals(movie, repository.getMovieDetails(7).getOrThrow())
        coVerify { details.insert(match { it.movieId == 7 && it.movie.title == "Film" }) }
        coVerify(exactly = 0) { popular.insertMovie(any()) }
    }

    @Test fun `offline details prefer details cache then favorites then popular`() = runTest {
        coEvery { api.getMovieDetails(7) } throws IOException("offline")
        coEvery { details.getById(7) } returns MovieDetailsEntity(7, movie.toEntity(0))
        assertEquals(movie, repository.getMovieDetails(7).getOrThrow())
        coVerify(exactly = 0) { favorites.getById(any()) }
        coEvery { details.getById(7) } returns null
        coEvery { favorites.getById(7) } returns FavoriteMovieEntity(7, movie.toEntity(0))
        assertEquals(movie, repository.getMovieDetails(7).getOrThrow())
        coEvery { favorites.getById(7) } returns null
        coEvery { popular.getMovieById(7) } returns movie.toEntity(1)
        assertEquals(movie, repository.getMovieDetails(7).getOrThrow())
    }

    @Test fun `offline without cache returns original failure`() = runTest {
        val error = IOException("offline")
        coEvery { api.getMovieDetails(7) } throws error
        coEvery { details.getById(7) } returns null
        coEvery { favorites.getById(7) } returns null
        coEvery { popular.getMovieById(7) } returns null
        assertSame(error, repository.getMovieDetails(7).exceptionOrNull())
    }

    @Test fun `cancellation is propagated without reading cache`() = runTest {
        coEvery { api.getMovieDetails(7) } throws CancellationException("cancelled")
        try {
            repository.getMovieDetails(7)
            fail("Cancellation must propagate")
        } catch (_: CancellationException) { }
        coVerify(exactly = 0) { details.getById(any()) }
    }

    @Test fun `favorites can be saved observed and removed`() = runTest {
        repository.setFavorite(movie, true)
        coVerify { favorites.insert(match { it.movieId == 7 }) }
        every { favorites.observeAll() } returns flowOf(listOf(FavoriteMovieEntity(7, movie.toEntity(0))))
        every { favorites.observeIsFavorite(7) } returns flowOf(true)
        assertEquals(listOf(movie), repository.observeFavorites().first())
        assertTrue(repository.observeIsFavorite(7).first())
        repository.setFavorite(movie, false)
        coVerify { favorites.delete(7) }
    }
}
