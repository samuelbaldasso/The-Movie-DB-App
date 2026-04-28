package com.sbaldasso.tmdbapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sbaldasso.tmdbapp.data.local.dao.MovieDao
import com.sbaldasso.tmdbapp.data.local.dao.MovieRemoteKeysDao
import com.sbaldasso.tmdbapp.data.local.entity.MovieEntity
import com.sbaldasso.tmdbapp.data.local.entity.MovieRemoteKeysEntity

@Database(
    entities = [
        MovieEntity::class,
        MovieRemoteKeysEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun movieRemoteKeysDao(): MovieRemoteKeysDao
}