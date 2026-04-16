package com.practicum.playlistmaker.presentation.audio

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.models.Playlist
import com.practicum.playlistmaker.util.RussianPlurals

class PlaylistBottomSheetAdapter(
    private val items: MutableList<Playlist> = mutableListOf(),
    private val onItemClick: (Playlist) -> Unit = {}
) : RecyclerView.Adapter<PlaylistBottomSheetAdapter.ViewHolder>() {

    fun setItems(newItems: List<Playlist>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val coverImage: ImageView = view.findViewById(R.id.cover_image)
        private val coverPlaceholder: ImageView = view.findViewById(R.id.cover_placeholder)
        private val playlistName: TextView = view.findViewById(R.id.playlist_name)
        private val tracksCount: TextView = view.findViewById(R.id.tracks_count)

        fun bind(item: Playlist) {
            playlistName.text = item.name
            tracksCount.text = RussianPlurals.pluralRussian(
                itemView.context,
                R.plurals.playlist_detail_tracks,
                item.trackCount
            )

            if (!item.coverPath.isNullOrEmpty()) {
                coverPlaceholder.visibility = View.GONE
                coverImage.visibility = View.VISIBLE
                val radius = itemView.resources.getDimensionPixelSize(R.dimen.playlist_cover_bottom_sheet_radius)
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_playlist_bottom_sheet, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = items.size
}
