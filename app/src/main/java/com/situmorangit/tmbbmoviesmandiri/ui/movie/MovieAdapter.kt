package com.situmorangit.tmbbmoviesmandiri.ui.movie

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.situmorangit.tmbbmoviesmandiri.BuildConfig
import com.situmorangit.tmbbmoviesmandiri.data.models.Movie
import com.situmorangit.tmbbmoviesmandiri.databinding.ItemMovieBinding
import com.situmorangit.tmbbmoviesmandiri.databinding.ItemMovieGridBinding

class MovieAdapter(
    private val onClick: (Movie) -> Unit
) : ListAdapter<Movie, RecyclerView.ViewHolder>(MovieDiffCallback()) {

    companion object {
        const val VIEW_TYPE_LIST = 0
        const val VIEW_TYPE_GRID = 1
    }

    private var isGridMode: Boolean = false

    fun setGridMode(isGrid: Boolean) {
        if (isGridMode != isGrid) {
            isGridMode = isGrid
            notifyItemRangeChanged(0, itemCount)
        }
    }

    fun isGridMode(): Boolean = isGridMode

    override fun getItemViewType(position: Int): Int {
        return if (isGridMode) VIEW_TYPE_GRID else VIEW_TYPE_LIST
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_GRID -> {
                val binding = ItemMovieGridBinding.inflate(inflater, parent, false)
                GridMovieViewHolder(binding, onClick)
            }
            else -> {
                val binding = ItemMovieBinding.inflate(inflater, parent, false)
                ListMovieViewHolder(binding, onClick)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val movie = getItem(position)
        when (holder) {
            is ListMovieViewHolder -> holder.bind(movie)
            is GridMovieViewHolder -> holder.bind(movie)
        }
    }

    class ListMovieViewHolder(
        private val binding: ItemMovieBinding,
        private val onClick: (Movie) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(movie: Movie) {
            binding.tvMovieTitle.text = movie.title
            binding.tvMovieOverview.text = movie.overview
            binding.tvMovieRating.text = "⭐ ${movie.voteAverage}"
            binding.tvReleaseDate.text = movie.releaseDate ?: "-"

            val posterUrl = "${BuildConfig.IMAGE_BASE_URL}${movie.posterPath}"
            Glide.with(binding.ivPoster.context)
                .load(posterUrl)
                .centerCrop()
                .into(binding.ivPoster)

            binding.root.setOnClickListener { onClick(movie) }
        }
    }

    class GridMovieViewHolder(
        private val binding: ItemMovieGridBinding,
        private val onClick: (Movie) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(movie: Movie) {
            binding.tvMovieTitleGrid.text = movie.title
            binding.tvMovieRatingGrid.text = "⭐ ${movie.voteAverage}"

            val posterUrl = "${BuildConfig.IMAGE_BASE_URL}${movie.posterPath}"
            Glide.with(binding.ivPosterGrid.context)
                .load(posterUrl)
                .centerCrop()
                .into(binding.ivPosterGrid)

            binding.root.setOnClickListener { onClick(movie) }
        }
    }

    class MovieDiffCallback : DiffUtil.ItemCallback<Movie>() {
        override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean = oldItem == newItem
    }
}