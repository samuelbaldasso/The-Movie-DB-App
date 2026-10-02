package com.sbaldasso.tmdbapp.domain.model

data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val voteAverage: Double,
    val releaseDate: String,
    val popularity: Double
) {
    fun getPosterUrl(): String? = posterPath?.takeIf { it.isNotBlank() }
        ?.let { "https://image.tmdb.org/t/p/w500$it" }

    fun getBackdropUrl(): String? = backdropPath?.takeIf { it.isNotBlank() }
        ?.let { "https://image.tmdb.org/t/p/w780$it" }
}
