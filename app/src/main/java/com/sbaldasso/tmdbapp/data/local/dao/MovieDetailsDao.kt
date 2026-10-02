package com.sbaldasso.tmdbapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sbaldasso.tmdbapp.data.local.entity.MovieDetailsEntity

@Dao
interface MovieDetailsDao {
    @Query("SELECT * FROM movie_details WHERE movieId = :movieId")
    suspend fun getById(movieId: Int): MovieDetailsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(details: MovieDetailsEntity)
}
