package com.situmorangit.tmbbmoviesmandiri.ui.movie

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.situmorangit.tmbbmoviesmandiri.R
import com.situmorangit.tmbbmoviesmandiri.data.network.RetrofitClient
import com.situmorangit.tmbbmoviesmandiri.data.repository.MovieRepository
import com.situmorangit.tmbbmoviesmandiri.data.repository.Resource
import com.situmorangit.tmbbmoviesmandiri.databinding.FragmentMovieListBinding
import com.situmorangit.tmbbmoviesmandiri.ui.MovieListViewModelFactory

class MovieListFragment : Fragment(R.layout.fragment_movie_list) {

    private var _binding: FragmentMovieListBinding? = null
    private val binding get() = _binding!!

    private val args: MovieListFragmentArgs by navArgs()
    private val viewModel: MovieListViewModel by viewModels {
        MovieListViewModelFactory(MovieRepository(RetrofitClient.apiService))
    }

    private lateinit var adapter: MovieAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMovieListBinding.bind(view)

        requireActivity().title = args.genreName

        setupRecyclerView()
        setupToggleButton()
        observeViewModel()

        if (viewModel.moviesState.value == null) {
            viewModel.loadMoviesByGenre(args.genreId, isRefresh = true)
        }
    }

    private fun setupToggleButton() {
        binding.btnToggleLayout.setOnClickListener {
            viewModel.toggleLayoutMode()
        }
    }

    private fun applyLayoutManager(isGrid: Boolean) {
        val currentPosition = when (val currentLayout = binding.rvMovies.layoutManager) {
            is GridLayoutManager -> currentLayout.findFirstVisibleItemPosition()
            is LinearLayoutManager -> currentLayout.findFirstVisibleItemPosition()
            else -> 0
        }

        val newLayoutManager: LinearLayoutManager = if (isGrid) {
            GridLayoutManager(requireContext(), 2)
        } else {
            LinearLayoutManager(requireContext())
        }

        adapter.setGridMode(isGrid)
        binding.rvMovies.layoutManager = newLayoutManager
        binding.btnToggleLayout.text = if (isGrid) "List View" else "Grid View"

        if (currentPosition != RecyclerView.NO_POSITION && currentPosition > 0) {
            binding.rvMovies.scrollToPosition(currentPosition)
        }
    }

    private fun setupRecyclerView() {
        adapter = MovieAdapter { movie ->
            val action = MovieListFragmentDirections
                .actionMovieListFragmentToMovieDetailFragment(movie.id)
            findNavController().navigate(action)
        }

        binding.rvMovies.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMovies.adapter = adapter

        // Endless scrolling mendukung LinearLayoutManager maupun GridLayoutManager
        binding.rvMovies.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!viewModel.isLoading && viewModel.currentPage <= viewModel.totalPages) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                        viewModel.loadMoviesByGenre(args.genreId)
                    }
                }
            }
        })
    }

    private fun observeViewModel() {
        viewModel.isGridMode.observe(viewLifecycleOwner) { isGrid ->
            applyLayoutManager(isGrid)
        }

        viewModel.moviesState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    if (resource.data.isNullOrEmpty()) {
                        binding.progressBar.visibility = View.VISIBLE
                    } else {
                        binding.progressBarPagination.visibility = View.VISIBLE
                    }
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.progressBarPagination.visibility = View.GONE
                    resource.data?.let {
                        adapter.submitList(it)
                        binding.tvEmptyState.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.progressBarPagination.visibility = View.GONE
                    if (resource.data.isNullOrEmpty()) {
                        binding.layoutError.visibility = View.VISIBLE
                        binding.tvErrorMessage.text = resource.message
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}