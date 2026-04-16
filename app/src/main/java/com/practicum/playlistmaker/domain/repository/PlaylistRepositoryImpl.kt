package com.practicum.playlistmaker.domain.repository

import com.practicum.playlistmaker.domain.models.Playlist
import com.practicum.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow

class PlaylistRepositoryImpl(
    private val dataSource: PlaylistDataSource
) : PlaylistRepository {

    override suspend fun createPlaylist(playlist: Playlist): Long =
        dataSource.insertPlaylist(playlist)

    override suspend fun getPlaylistById(id: Long): Playlist? =
        dataSource.getPlaylistById(id)

    override suspend fun updatePlaylist(playlist: Playlist) {
        dataSource.updatePlaylist(playlist)
    }

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

    override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        val playlist = dataSource.getPlaylistById(playlistId) ?: return
        if (!playlist.trackIds.contains(trackId)) return
        val newIds = playlist.trackIds.filter { it != trackId }
        val updated = playlist.copy(
            trackIds = newIds,
            trackCount = newIds.size
        )
        dataSource.updatePlaylist(updated)
        removeOrphanPlaylistTrackIfNeeded(trackId)
    }

    private suspend fun removeOrphanPlaylistTrackIfNeeded(trackId: Long) {
        val stillUsed = dataSource.getAllPlaylistsSync().any { trackId in it.trackIds }
        if (!stillUsed) {
            dataSource.deletePlaylistTrackRow(trackId)
        }
    }

    override fun observePlaylistById(id: Long): Flow<Playlist?> =
        dataSource.observePlaylistById(id)

    override fun observeTracksByIds(trackIds: List<Long>): Flow<List<Track>> =
        dataSource.observeTracksByIds(trackIds)

    override suspend fun deletePlaylist(playlistId: Long) {
        val playlist = dataSource.getPlaylistById(playlistId) ?: return
        dataSource.deletePlaylistRow(playlistId)
        playlist.trackIds.forEach { trackId ->
            removeOrphanPlaylistTrackIfNeeded(trackId)
        }
    }
}
