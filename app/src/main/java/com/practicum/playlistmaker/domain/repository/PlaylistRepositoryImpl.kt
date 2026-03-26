package com.practicum.playlistmaker.domain.repository

import com.practicum.playlistmaker.domain.models.Playlist
import com.practicum.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow

class PlaylistRepositoryImpl(
    private val dataSource: PlaylistDataSource
) : PlaylistRepository {

    override suspend fun createPlaylist(playlist: Playlist): Long =
        dataSource.insertPlaylist(playlist)

    override fun getAllPlaylists(): Flow<List<Playlist>> =
        dataSource.getAllPlaylists()

    override suspend fun addTrackToPlaylist(playlist: Playlist, track: Track) {
        dataSource.insertPlaylistTrack(track)
        val updatedPlaylist = playlist.copy(
            trackIds = playlist.trackIds + track.trackId,
            trackCount = playlist.trackCount + 1
        )
        dataSource.updatePlaylist(updatedPlaylist)
    }
}
