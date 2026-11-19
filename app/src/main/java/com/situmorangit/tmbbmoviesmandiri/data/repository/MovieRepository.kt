package com.situmorangit.tmbbmoviesmandiri.data.repository

import com.situmorangit.tmbbmoviesmandiri.BuildConfig
import com.situmorangit.tmbbmoviesmandiri.data.network.ApiService

class MovieRepository(private val apiService: ApiService) {
    private val apiKey = BuildConfig.TMDB_API_KEY

    suspend fun getGenres() = apiService.getGenres(apiKey)
    suspend fun discoverMovies(genreId: Int, page: Int) = apiService.discoverMoviesByGenre(apiKey, genreId, page)
    suspend fun getMovieDetails(movieId: Int) = apiService.getMovieDetails(movieId, apiKey)
    suspend fun getMovieReviews(movieId: Int, page: Int) = apiService.getMovieReviews(movieId, apiKey, page)
    suspend fun getMovieVideos(movieId: Int) = apiService.getMovieVideos(movieId, apiKey)
}