package com.sbaldasso.tmdbapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sbaldasso.tmdbapp.data.local.entity.MovieRemoteKeysEntity

@Dao
interface MovieRemoteKeysDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKey: List<MovieRemoteKeysEntity>)

    @Query("SELECT * FROM movie_remote_keys WHERE movieId = :movieId")
    suspend fun getRemoteKeysForMovie(movieId: Int): MovieRemoteKeysEntity?

    @Query("DELETE FROM movie_remote_keys")
    suspend fun clearRemoteKeys()
}