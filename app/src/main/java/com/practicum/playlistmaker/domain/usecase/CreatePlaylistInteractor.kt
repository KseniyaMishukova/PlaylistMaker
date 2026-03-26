package com.practicum.playlistmaker.domain.usecase

import android.net.Uri
import com.practicum.playlistmaker.domain.models.Playlist
import com.practicum.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface CreatePlaylistInteractor {
    suspend fun createPlaylist(name: String, description: String?, coverUri: Uri?): Long
    suspend fun getPlaylistById(playlistId: Long): Playlist?
    suspend fun updatePlaylist(playlistId: Long, name: String, description: String?, coverUri: Uri?)
    fun getPlaylists(): Flow<List<Playlist>>
    suspend fun addTrackToPlaylist(playlist: Playlist, track: Track)
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long)
    fun observePlaylistById(playlistId: Long): Flow<Playlist?>
    fun observeTracksByIds(trackIds: List<Long>): Flow<List<Track>>
    suspend fun deletePlaylist(playlistId: Long)
}
