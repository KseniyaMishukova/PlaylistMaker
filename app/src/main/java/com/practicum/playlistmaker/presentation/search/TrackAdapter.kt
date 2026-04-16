package com.practicum.playlistmaker.presentation.search

import android.annotation.SuppressLint
import androidx.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.models.Track

class TrackAdapter(
    private val items: MutableList<Track>,
    @LayoutRes private val itemLayoutRes: Int = R.layout.item_track,
    private val onItemClick: (Track) -> Unit = {},
    private val onItemLongClick: ((Track) -> Unit)? = null,

    private val useRowClickListener: Boolean = true
) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(newItems: List<Track>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun getTrackAt(position: Int): Track? = items.getOrNull(position)

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
            val art = item.artworkUrl100?.trim().orEmpty()
            if (art.isEmpty()) {
                Glide.with(itemView).clear(ivCover)
                ivCover.setImageResource(R.drawable.ic_zig)
            } else {
                Glide.with(itemView)
                    .load(art)
                    .placeholder(R.drawable.ic_zig)
                    .error(R.drawable.ic_zig)
                    .centerCrop()
                    .transform(RoundedCorners(r))
                    .into(ivCover)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(itemLayoutRes, parent, false)
        return TrackViewHolder(v)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        if (useRowClickListener) {
            holder.itemView.setOnClickListener { onItemClick(item) }
            holder.itemView.isClickable = true
        } else {
            holder.itemView.setOnClickListener(null)
            holder.itemView.isClickable = false
        }
        if (onItemLongClick != null) {
            holder.itemView.setOnLongClickListener {
                onItemLongClick.invoke(item)
                true
            }
        } else {
            holder.itemView.setOnLongClickListener(null)
        }
    }

    override fun getItemCount(): Int = items.size
}