package com.practicum.playlistmaker.domain.repository

import com.practicum.playlistmaker.domain.models.Playlist
import com.practicum.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistDataSource {
    suspend fun insertPlaylist(playlist: Playlist): Long
    fun getAllPlaylists(): Flow<List<Playlist>>
    suspend fun insertPlaylistTrack(track: Track): Unit
    suspend fun getPlaylistById(id: Long): Playlist?
    suspend fun updatePlaylist(playlist: Playlist): Unit
}
