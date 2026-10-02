package com.sbaldasso.tmdbapp

import android.app.Application
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.paging.*
import androidx.room.Room
import org.robolectric.RuntimeEnvironment
import com.sbaldasso.tmdbapp.data.local.*
import com.sbaldasso.tmdbapp.data.local.entity.*
import com.sbaldasso.tmdbapp.data.local.mapper.toEntity
import com.sbaldasso.tmdbapp.data.paging.MovieRemoteMediator
import com.sbaldasso.tmdbapp.data.remote.api.TMDBApiService
import com.sbaldasso.tmdbapp.data.remote.dto.*
import com.sbaldasso.tmdbapp.domain.model.Movie
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
@OptIn(ExperimentalPagingApi::class)
class MovieDatabaseTest {
    private val context: Context get() = RuntimeEnvironment.getApplication()
    private val movie = Movie(7, "Favorite", "Synopsis", null, null, 8.0, "", 10.0)
    private inline fun <T> AppDatabase.use(block: (AppDatabase) -> T): T =
        try { block(this) } finally { close() }

    private fun open(name: String) = Room.databaseBuilder(context, AppDatabase::class.java, name)
        .addMigrations(MIGRATION_2_3).allowMainThreadQueries().build()

    @Test fun `favorites persist after database closes and can be removed`() = runBlocking {
        val name = "favorites-test.db"
        context.deleteDatabase(name)
        open(name).use { db ->
            db.favoriteMovieDao().insert(FavoriteMovieEntity(7, movie.toEntity(0)))
            assertTrue(db.favoriteMovieDao().observeIsFavorite(7).first())
        }
        open(name).use { db ->
            assertEquals("Favorite", db.favoriteMovieDao().observeAll().first().single().movie.title)
            db.favoriteMovieDao().delete(7)
            assertTrue(db.favoriteMovieDao().observeAll().first().isEmpty())
        }
        Unit
    }

    @Test fun `refresh replaces popular cache and preserves details and favorites`() = runBlocking {
        Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build().use { db ->
            db.movieDao().insertMovie(movie.toEntity(1))
            db.movieDetailsDao().insert(MovieDetailsEntity(7, movie.toEntity(0)))
            db.favoriteMovieDao().insert(FavoriteMovieEntity(7, movie.toEntity(0)))
            val api = mockk<TMDBApiService>()
            val dto = MovieDto(9, "Popular", "", voteAverage = 6.0, releaseDate = "", popularity = 20.0)
            coEvery { api.getPopularMovies(1) } returns MoviesResponse(1, listOf(dto), 1, 1)
            val mediator = MovieRemoteMediator(api, db)
            val state = PagingState<Int, MovieEntity>(emptyList(), null, PagingConfig(20), 0)
            val result = mediator.load(LoadType.REFRESH, state) as RemoteMediator.MediatorResult.Success
            assertTrue(result.endOfPaginationReached)
            assertEquals(listOf(9), db.movieDao().getAllMovies().first().map { it.id })
            assertNotNull(db.movieDetailsDao().getById(7))
            assertTrue(db.favoriteMovieDao().observeIsFavorite(7).first())
            coEvery { api.getPopularMovies(1) } throws IOException("offline")
            assertTrue(mediator.load(LoadType.REFRESH, state) is RemoteMediator.MediatorResult.Error)
            assertEquals(listOf(9), db.movieDao().getAllMovies().first().map { it.id })
        }
        Unit
    }

    @Test fun `migration from version 2 preserves popular cache and validates new tables`() = runBlocking {
        val name = "migration-test.db"
        context.deleteDatabase(name)
        val file = context.getDatabasePath(name)
        file.parentFile!!.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(file, null).use { old ->
            old.execSQL("""CREATE TABLE movies (
                id INTEGER NOT NULL PRIMARY KEY, title TEXT NOT NULL, overview TEXT NOT NULL,
                posterPath TEXT, backdropPath TEXT, voteAverage REAL NOT NULL,
                releaseDate TEXT NOT NULL, popularity REAL NOT NULL, page INTEGER NOT NULL,
                timestamp INTEGER NOT NULL)""")
            old.execSQL("CREATE TABLE movie_remote_keys (movieId INTEGER NOT NULL PRIMARY KEY, prevKey INTEGER, nextKey INTEGER)")
            old.execSQL("INSERT INTO movies VALUES (7, 'Favorite', 'Synopsis', NULL, NULL, 8.0, '', 10.0, 1, 0)")
            old.version = 2
        }
        open(name).use { db ->
            assertEquals("Favorite", db.movieDao().getMovieById(7)?.title)
            assertTrue(db.favoriteMovieDao().observeAll().first().isEmpty())
            db.favoriteMovieDao().insert(FavoriteMovieEntity(7, movie.toEntity(0)))
            db.movieDetailsDao().insert(MovieDetailsEntity(7, movie.toEntity(0)))
            assertNotNull(db.movieDetailsDao().getById(7))
        }
        Unit
    }
}
