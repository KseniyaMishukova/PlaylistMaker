package com.practicum.playlistmaker.presentation.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.Track

class TrackAdapter(
    private val items: MutableList<Track>,
    private val onItemClick: (Track) -> Unit = {}
) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(newItems: List<Track>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    class TrackViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivCover: ImageView = view.findViewById(R.id.ivCover)
        private val tvTrackName: TextView = view.findViewById(R.id.tvTrackName)
        private val tvArtist: TextView = view.findViewById(R.id.tvArtist)
        private val tvTime: TextView = view.findViewById(R.id.tvTime)

        fun bind(item: Track) {
            tvTrackName.text = item.trackName
            tvArtist.text = item.artistName
            tvTime.text = item.trackTime

            val r = itemView.resources.getDimensionPixelSize(R.dimen.track_corner_radius)
            Glide.with(itemView)
                .load(item.artworkUrl100)
                .placeholder(R.drawable.ic_zig)
                .error(R.drawable.ic_zig)
                .centerCrop()
                .transform(RoundedCorners(r))
                .into(ivCover)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_track, parent, false)
        return TrackViewHolder(v)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = items.size
}