package com.sbaldasso.tmdbapp.data.local.mapper

import com.sbaldasso.tmdbapp.data.local.entity.MovieEntity
import com.sbaldasso.tmdbapp.data.remote.dto.MovieDto
import com.sbaldasso.tmdbapp.domain.model.Movie

fun MovieEntity.toDomain(): Movie {
    return Movie(
        id = id,
        title = title,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        voteAverage = voteAverage,
        releaseDate = releaseDate,
        popularity = popularity
    )
}

fun Movie.toEntity(page: Int): MovieEntity {
    return MovieEntity(
        id = id,
        title = title,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        voteAverage = voteAverage,
        releaseDate = releaseDate,
        popularity = popularity,
        page = page
    )
}

fun MovieDto.toEntity(page: Int): MovieEntity {
    return MovieEntity(
        id = id,
        title = title,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        voteAverage = voteAverage,
        releaseDate = releaseDate,
        popularity = popularity,
        page = page
    )
}