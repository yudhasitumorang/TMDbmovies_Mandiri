package com.situmorangit.tmbbmoviesmandiri.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.situmorangit.tmbbmoviesmandiri.data.models.MovieDetail
import com.situmorangit.tmbbmoviesmandiri.data.models.Review
import com.situmorangit.tmbbmoviesmandiri.data.models.Video
import com.situmorangit.tmbbmoviesmandiri.data.repository.MovieRepository
import com.situmorangit.tmbbmoviesmandiri.data.repository.Resource
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MovieDetailViewModel(private val repository: MovieRepository) : ViewModel() {
    private val _detailState = MutableLiveData<Resource<MovieDetail>>()
    val detailState: LiveData<Resource<MovieDetail>> get() = _detailState

    private val _trailerState = MutableLiveData<Resource<Video?>>()
    val trailerState: LiveData<Resource<Video?>> get() = _trailerState

    private val _reviewsState = MutableLiveData<Resource<List<Review>>>()
    val reviewsState: LiveData<Resource<List<Review>>> get() = _reviewsState

    private val reviewList = mutableListOf<Review>()
    var currentReviewPage = 1
    var totalReviewPages = 1
    var isLoadingReviews = false

    fun fetchMovieDetails(movieId: Int) {
        _detailState.value = Resource.Loading()
        _trailerState.value = Resource.Loading()

        viewModelScope.launch {
            try {
                val detailDeferred = async { repository.getMovieDetails(movieId) }
                val videoDeferred = async { repository.getMovieVideos(movieId) }

                val detailResponse = detailDeferred.await()
                val videoResponse = videoDeferred.await()

                if (detailResponse.isSuccessful && detailResponse.body() != null) {
                    _detailState.value = Resource.Success(detailResponse.body()!!)
                } else {
                    _detailState.value = Resource.Error("Failed to load movie details")
                }

                if (videoResponse.isSuccessful && videoResponse.body() != null) {
                    val trailer = videoResponse.body()!!.results.firstOrNull {
                        it.site.equals("YouTube", true) && (it.type.equals("Trailer", true) || it.type.equals("Teaser", true))
                    } ?: videoResponse.body()!!.results.firstOrNull()
                    _trailerState.value = Resource.Success(trailer)
                } else {
                    _trailerState.value = Resource.Success(null)
                }
            } catch (e: Exception) {
                _detailState.value = Resource.Error(e.localizedMessage ?: "Error loading details")
            }
        }
    }

    fun fetchReviews(movieId: Int, isRefresh: Boolean = false) {
        if (isLoadingReviews) return
        if (isRefresh) {
            currentReviewPage = 1
            reviewList.clear()
        }
        if (currentReviewPage > totalReviewPages && !isRefresh) return

        isLoadingReviews = true
        _reviewsState.value = Resource.Loading(reviewList.toList())

        viewModelScope.launch {
            try {
                val response = repository.getMovieReviews(movieId, currentReviewPage)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    totalReviewPages = body.totalPages
                    reviewList.addAll(body.results)
                    _reviewsState.value = Resource.Success(reviewList.toList())
                    currentReviewPage++
                } else {
                    _reviewsState.value = Resource.Error("Failed to load reviews", reviewList.toList())
                }
            } catch (e: Exception) {
                _reviewsState.value = Resource.Error(e.localizedMessage ?: "Error loading reviews", reviewList.toList())
            } finally {
                isLoadingReviews = false
            }
        }
    }
}