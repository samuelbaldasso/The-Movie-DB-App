package com.sbaldasso.tmdbapp.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sbaldasso.tmdbapp.data.local.dao.MovieDetailsDao
import com.sbaldasso.tmdbapp.data.local.dao.FavoriteMovieDao
import com.sbaldasso.tmdbapp.data.local.entity.MovieDetailsEntity
import com.sbaldasso.tmdbapp.data.local.entity.FavoriteMovieEntity
import androidx.room.Database
import androidx.room.RoomDatabase
import com.sbaldasso.tmdbapp.data.local.dao.MovieDao
import com.sbaldasso.tmdbapp.data.local.dao.MovieRemoteKeysDao
import com.sbaldasso.tmdbapp.data.local.entity.MovieEntity
import com.sbaldasso.tmdbapp.data.local.entity.MovieRemoteKeysEntity

@Database(
    entities = [
        MovieEntity::class,
        MovieRemoteKeysEntity::class,
        MovieDetailsEntity::class,
        FavoriteMovieEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDetailsDao(): MovieDetailsDao
    abstract fun favoriteMovieDao(): FavoriteMovieDao
    abstract fun movieDao(): MovieDao
    abstract fun movieRemoteKeysDao(): MovieRemoteKeysDao
}

/** Version 2 popular cache is preserved; details and favorites have independent lifetimes. */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        for (table in listOf("movie_details", "favorite_movies")) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `$table` (
                    `movieId` INTEGER NOT NULL PRIMARY KEY,
                    `id` INTEGER NOT NULL, `title` TEXT NOT NULL, `overview` TEXT NOT NULL,
                    `posterPath` TEXT, `backdropPath` TEXT, `voteAverage` REAL NOT NULL,
                    `releaseDate` TEXT NOT NULL, `popularity` REAL NOT NULL,
                    `page` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL
                )
            """.trimIndent())
        }
    }
}
