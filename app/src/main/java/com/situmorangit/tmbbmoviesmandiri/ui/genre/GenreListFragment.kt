package com.situmorangit.tmbbmoviesmandiri.ui.genre

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.situmorangit.tmbbmoviesmandiri.R
import com.situmorangit.tmbbmoviesmandiri.data.network.RetrofitClient
import com.situmorangit.tmbbmoviesmandiri.data.repository.MovieRepository
import com.situmorangit.tmbbmoviesmandiri.data.repository.Resource
import com.situmorangit.tmbbmoviesmandiri.databinding.FragmentGenreListBinding
import com.situmorangit.tmbbmoviesmandiri.ui.GenreViewModelFactory

class GenreListFragment : Fragment(R.layout.fragment_genre_list) {

    private var _binding: FragmentGenreListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GenreViewModel by viewModels {
        GenreViewModelFactory(MovieRepository(RetrofitClient.apiService))
    }

    private lateinit var adapter: GenreAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGenreListBinding.bind(view)

        setupRecyclerView()
        observeViewModel()

        binding.btnRetry.setOnClickListener {
            viewModel.fetchGenres()
        }

        if (viewModel.genresState.value == null) {
            viewModel.fetchGenres()
        }
    }

    private fun setupRecyclerView() {
        adapter = GenreAdapter { genre ->
            val action = GenreListFragmentDirections.actionGenreListFragmentToMovieListFragment(genre.id, genre.name)
            findNavController().navigate(action)
        }
        binding.rvGenres.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvGenres.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.genresState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.layoutError.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.layoutError.visibility = View.GONE
                    resource.data?.let { adapter.submitList(it) }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.layoutError.visibility = View.VISIBLE
                    binding.tvErrorMessage.text = resource.message ?: "Unknown Error"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}