package com.practicum.playlistmaker.presentation.media

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.models.Playlist

class PlaylistAdapter(
    private val items: MutableList<Playlist> = mutableListOf()
) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    fun setItems(newItems: List<Playlist>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    class PlaylistViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val coverImage: ImageView = view.findViewById(R.id.cover_image)
        private val coverPlaceholder: ImageView = view.findViewById(R.id.cover_placeholder)
        private val playlistName: TextView = view.findViewById(R.id.playlist_name)
        private val tracksCount: TextView = view.findViewById(R.id.tracks_count)
        fun bind(item: Playlist, position: Int) {
            val params = itemView.layoutParams as? ViewGroup.MarginLayoutParams
            params?.let {
                it.marginStart = if (position % 2 == 0) 0 else itemView.context.resources.getDimensionPixelSize(R.dimen.playlist_grid_spacing)
                it.marginEnd = if (position % 2 == 0) itemView.context.resources.getDimensionPixelSize(R.dimen.playlist_grid_spacing) else 0
            }
            (itemView as FrameLayout).getChildAt(0)?.let { content ->
                (content.layoutParams as? FrameLayout.LayoutParams)?.gravity =
                    if (position % 2 == 0) Gravity.TOP or Gravity.START else Gravity.TOP or Gravity.END
            }
            playlistName.text = item.name
            tracksCount.text = itemView.context.getString(R.string.tracks_count, item.trackCount)

            if (!item.coverPath.isNullOrEmpty()) {
                coverPlaceholder.visibility = View.GONE
                coverImage.visibility = View.VISIBLE
                val radius = itemView.resources.getDimensionPixelSize(R.dimen.playlist_cover_corner_radius)
                Glide.with(itemView)
                    .load(item.coverPath)
                    .centerCrop()
                    .transform(RoundedCorners(radius))
                    .into(coverImage)
            } else {
                coverPlaceholder.visibility = View.VISIBLE
                coverImage.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_playlist, parent, false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size
}
