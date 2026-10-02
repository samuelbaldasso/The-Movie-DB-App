package com.sbaldasso.tmdbapp.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movie_details")
data class MovieDetailsEntity(
    @PrimaryKey val movieId: Int,
    @Embedded val movie: MovieEntity
)
