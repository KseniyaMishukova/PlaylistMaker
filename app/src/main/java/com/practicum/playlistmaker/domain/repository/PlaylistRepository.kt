package com.practicum.playlistmaker.domain.repository

import com.practicum.playlistmaker.domain.models.Playlist
import com.practicum.playlistmaker.domain.models.Track

interface PlaylistRepository {
    suspend fun createPlaylist(playlist: Playlist): Long
    suspend fun getPlaylistById(id: Long): Playlist?
    suspend fun updatePlaylist(playlist: Playlist)
    fun getAllPlaylists(): kotlinx.coroutines.flow.Flow<List<Playlist>>
    suspend fun addTrackToPlaylist(playlist: Playlist, track: Track)
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long)
    fun observePlaylistById(id: Long): kotlinx.coroutines.flow.Flow<Playlist?>
    fun observeTracksByIds(trackIds: List<Long>): kotlinx.coroutines.flow.Flow<List<Track>>
    suspend fun deletePlaylist(playlistId: Long)
}
