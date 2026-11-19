package com.situmorangit.tmbbmoviesmandiri.ui.genre

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.situmorangit.tmbbmoviesmandiri.data.models.Genre
import com.situmorangit.tmbbmoviesmandiri.data.repository.MovieRepository
import com.situmorangit.tmbbmoviesmandiri.data.repository.Resource
import kotlinx.coroutines.launch

class GenreViewModel(private val repository: MovieRepository) : ViewModel() {
    private val _genresState = MutableLiveData<Resource<List<Genre>>>()
    val genresState: LiveData<Resource<List<Genre>>> get() = _genresState

    fun fetchGenres() {
        _genresState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                val response = repository.getGenres()
                if (response.isSuccessful && response.body() != null) {
                    _genresState.value = Resource.Success(response.body()!!.genres)
                } else {
                    _genresState.value = Resource.Error("Failed to fetch genres: ${response.message()}")
                }
            } catch (e: Exception) {
                _genresState.value = Resource.Error(e.localizedMessage ?: "An unexpected error occurred")
            }
        }
    }
}