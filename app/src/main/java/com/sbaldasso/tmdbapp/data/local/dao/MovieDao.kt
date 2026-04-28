package com.sbaldasso.tmdbapp.data.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.sbaldasso.tmdbapp.data.local.entity.MovieEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {

    @Query("SELECT * FROM movies ORDER BY popularity DESC")
    fun getAllMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE page = :page ORDER BY popularity DESC")
    suspend fun getMoviesByPage(page: Int): List<MovieEntity>

    @Query("SELECT * FROM movies WHERE id = :movieId")
    suspend fun getMovieById(movieId: Int): MovieEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<MovieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: MovieEntity)

    @Query("DELETE FROM movies WHERE page = :page")
    suspend fun deleteMoviesByPage(page: Int)

    @Query("DELETE FROM movies")
    suspend fun deleteAllMovies()

    @Query("DELETE FROM movies WHERE timestamp < :timestamp")
    suspend fun deleteOldMovies(timestamp: Long)

    @Query("SELECT * FROM movies ORDER BY popularity DESC")
    fun getMoviesPagingSource(): PagingSource<Int, MovieEntity>
}