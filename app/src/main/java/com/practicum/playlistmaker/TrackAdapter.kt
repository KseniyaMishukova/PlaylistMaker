package com.practicum.playlistmaker

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners


class TrackAdapter(
    private val items: List<Track>
) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    class TrackViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivCover: ImageView = view.findViewById(R.id.ivCover)
        private val tvTrackName: TextView = view.findViewById(R.id.tvTrackName)
        private val tvArtist: TextView = view.findViewById(R.id.tvArtist)
        private val tvTime: TextView = view.findViewById(R.id.tvTime)

        @SuppressLint("SetTextI18n")
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
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}