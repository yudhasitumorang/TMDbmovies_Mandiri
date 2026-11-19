package com.situmorangit.tmbbmoviesmandiri.ui.movie

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.situmorangit.tmbbmoviesmandiri.data.models.Movie
import com.situmorangit.tmbbmoviesmandiri.data.repository.MovieRepository
import com.situmorangit.tmbbmoviesmandiri.data.repository.Resource
import kotlinx.coroutines.launch

class MovieListViewModel(private val repository: MovieRepository) : ViewModel() {
    private val _moviesState = MutableLiveData<Resource<List<Movie>>>()
    val moviesState: LiveData<Resource<List<Movie>>> get() = _moviesState

    private val movieList = mutableListOf<Movie>()
    var currentPage = 1
    var totalPages = 1
    var isLoading = false
    private val _isGridMode = MutableLiveData<Boolean>(false)
    val isGridMode: LiveData<Boolean> get() = _isGridMode

    fun toggleLayoutMode() {
        _isGridMode.value = !(_isGridMode.value ?: false)
    }

    fun loadMoviesByGenre(genreId: Int, isRefresh: Boolean = false) {
        if (isLoading) return
        if (isRefresh) {
            currentPage = 1
            movieList.clear()
        }

        if (currentPage > totalPages && !isRefresh) return

        isLoading = true
        _moviesState.value = Resource.Loading(movieList.toList())

        viewModelScope.launch {
            try {
                val response = repository.discoverMovies(genreId, currentPage)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    totalPages = body.totalPages
                    movieList.addAll(body.results)
                    _moviesState.value = Resource.Success(movieList.toList())
                    currentPage++
                } else {
                    _moviesState.value = Resource.Error("Failed to load movies", movieList.toList())
                }
            } catch (e: Exception) {
                _moviesState.value = Resource.Error(e.localizedMessage ?: "Network error", movieList.toList())
            } finally {
                isLoading = false
            }
        }
    }
}