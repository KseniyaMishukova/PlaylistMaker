package com.practicum.playlistmaker.util

import android.content.Context
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.models.Playlist
import com.practicum.playlistmaker.domain.models.Track

object PlaylistShareTextFormatter {

    fun build(context: Context, playlist: Playlist, tracks: List<Track>): String {
        val sb = StringBuilder()
        sb.append(playlist.name).append('\n')
        sb.append(playlist.description?.trim().orEmpty()).append('\n')
        val n = tracks.size
        sb.append(
            context.resources.getQuantityString(R.plurals.playlist_detail_tracks, n, n)
        ).append('\n')
        tracks.forEachIndexed { index, track ->
            sb.append("${index + 1}. ${track.artistName} - ${track.trackName} (${track.trackTime})")
                .append('\n')
        }
        return sb.toString().trimEnd()
    }
}
