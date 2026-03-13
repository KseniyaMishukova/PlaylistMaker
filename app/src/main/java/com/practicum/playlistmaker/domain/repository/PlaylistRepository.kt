package com.practicum.playlistmaker.domain.repository

import com.practicum.playlistmaker.domain.models.Playlist
import com.practicum.playlistmaker.domain.models.Track

interface PlaylistRepository {
    suspend fun createPlaylist(playlist: Playlist): Long
    fun getAllPlaylists(): kotlinx.coroutines.flow.Flow<List<Playlist>>
    suspend fun addTrackToPlaylist(playlist: Playlist, track: Track)
}
