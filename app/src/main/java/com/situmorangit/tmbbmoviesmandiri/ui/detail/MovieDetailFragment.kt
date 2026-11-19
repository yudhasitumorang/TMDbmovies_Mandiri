package com.situmorangit.tmbbmoviesmandiri.ui.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.situmorangit.tmbbmoviesmandiri.BuildConfig
import com.situmorangit.tmbbmoviesmandiri.R
import com.situmorangit.tmbbmoviesmandiri.data.network.RetrofitClient
import com.situmorangit.tmbbmoviesmandiri.data.repository.MovieRepository
import com.situmorangit.tmbbmoviesmandiri.data.repository.Resource
import com.situmorangit.tmbbmoviesmandiri.databinding.FragmentMovieDetailBinding
import com.situmorangit.tmbbmoviesmandiri.ui.MovieDetailViewModelFactory

class MovieDetailFragment : Fragment(R.layout.fragment_movie_detail) {

    private var _binding: FragmentMovieDetailBinding? = null
    private val binding get() = _binding!!

    private val args: MovieDetailFragmentArgs by navArgs()
    private val viewModel: MovieDetailViewModel by viewModels {
        MovieDetailViewModelFactory(MovieRepository(RetrofitClient.apiService))
    }

    private lateinit var reviewAdapter: ReviewAdapter
    private var trailerKey: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMovieDetailBinding.bind(view)

        setupReviewsRecyclerView()
        observeViewModel()

        binding.btnPlayTrailer.setOnClickListener {
            trailerKey?.let { key ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$key"))
                startActivity(intent)
            }
        }

        if (viewModel.detailState.value == null) {
            viewModel.fetchMovieDetails(args.movieId)
            viewModel.fetchReviews(args.movieId, isRefresh = true)
        }
    }

    private fun setupReviewsRecyclerView() {
        reviewAdapter = ReviewAdapter()
        val layoutManager = LinearLayoutManager(requireContext())
        binding.rvReviews.layoutManager = layoutManager
        binding.rvReviews.adapter = reviewAdapter
        binding.rvReviews.isNestedScrollingEnabled = false

        // Endless scrolling for reviews
        binding.nestedScrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            if (scrollY == binding.nestedScrollView.getChildAt(0).measuredHeight - binding.nestedScrollView.measuredHeight) {
                if (!viewModel.isLoadingReviews && viewModel.currentReviewPage <= viewModel.totalReviewPages) {
                    viewModel.fetchReviews(args.movieId)
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.detailState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> binding.progressBar.visibility = View.VISIBLE
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    resource.data?.let { movie ->
                        binding.tvTitle.text = movie.title
                        binding.tvOverview.text = movie.overview
                        binding.tvReleaseDate.text = "Release: ${movie.releaseDate}"
                        binding.tvRating.text = "Rating: ${movie.voteAverage} / 10"
                        binding.tvGenres.text = "Genres: ${movie.genres.joinToString { it.name }}"

                        val posterUrl = "${BuildConfig.IMAGE_BASE_URL}${movie.posterPath}"
                        Glide.with(this).load(posterUrl).into(binding.ivPoster)
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }

        viewModel.trailerState.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                trailerKey = resource.data?.key
                binding.btnPlayTrailer.isEnabled = trailerKey != null
                if (trailerKey == null) {
                    binding.btnPlayTrailer.text = "Trailer Not Available"
                }
            }
        }

        viewModel.reviewsState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    if (resource.data.isNullOrEmpty()) {
                        binding.progressReviews.visibility = View.VISIBLE
                    }
                }
                is Resource.Success -> {
                    binding.progressReviews.visibility = View.GONE
                    resource.data?.let {
                        reviewAdapter.submitList(it)
                        binding.tvNoReviews.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                is Resource.Error -> {
                    binding.progressReviews.visibility = View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}