package com.practicum.playlistmaker.domain.usecase

import android.net.Uri
import com.practicum.playlistmaker.domain.models.Playlist
import com.practicum.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface CreatePlaylistInteractor {
    suspend fun createPlaylist(name: String, description: String?, coverUri: Uri?): Long
    fun getPlaylists(): Flow<List<Playlist>>
    suspend fun addTrackToPlaylist(playlist: Playlist, track: Track)
}
