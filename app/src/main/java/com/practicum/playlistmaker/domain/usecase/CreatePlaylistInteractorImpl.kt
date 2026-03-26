package com.practicum.playlistmaker.domain.usecase

import android.net.Uri
import com.practicum.playlistmaker.domain.models.Playlist
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.domain.repository.ImageStorage
import com.practicum.playlistmaker.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow

class CreatePlaylistInteractorImpl(
    private val playlistRepository: PlaylistRepository,
    private val imageStorage: ImageStorage
) : CreatePlaylistInteractor {

    override suspend fun createPlaylist(name: String, description: String?, coverUri: Uri?): Long {
        val coverPath = coverUri?.let { imageStorage.copyToAppStorage(it) }?.takeIf { it.isNotEmpty() }
        val playlist = Playlist(
            id = 0,
            name = name.trim(),
            description = description?.trim()?.takeIf { it.isNotEmpty() },
            coverPath = coverPath,
            trackIds = emptyList(),
            trackCount = 0
        )
        return playlistRepository.createPlaylist(playlist)
    }

    override suspend fun getPlaylistById(playlistId: Long): Playlist? =
        playlistRepository.getPlaylistById(playlistId)

    override suspend fun updatePlaylist(
        playlistId: Long,
        name: String,
        description: String?,
        coverUri: Uri?
    ) {
        val current = playlistRepository.getPlaylistById(playlistId) ?: return
        val newCoverPath = resolveCoverPathForUpdate(current.coverPath, coverUri)
        val updated = current.copy(
            name = name.trim(),
            description = description?.trim()?.takeIf { it.isNotEmpty() },
            coverPath = newCoverPath
        )
        playlistRepository.updatePlaylist(updated)
    }

    private fun resolveCoverPathForUpdate(oldPath: String?, coverUri: Uri?): String? {
        if (coverUri == null) return null
        if (oldPath != null && "file" == coverUri.scheme) {
            val path = coverUri.path
            if (path != null && path == oldPath) return oldPath
        }
        return imageStorage.copyToAppStorage(coverUri)?.takeIf { it.isNotEmpty() }
    }

    override fun getPlaylists(): Flow<List<Playlist>> = playlistRepository.getAllPlaylists()

    override suspend fun addTrackToPlaylist(playlist: Playlist, track: Track) {
        playlistRepository.addTrackToPlaylist(playlist, track)
    }

    override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        playlistRepository.removeTrackFromPlaylist(playlistId, trackId)
    }

    override fun observePlaylistById(playlistId: Long): Flow<Playlist?> =
        playlistRepository.observePlaylistById(playlistId)

    override fun observeTracksByIds(trackIds: List<Long>): Flow<List<Track>> =
        playlistRepository.observeTracksByIds(trackIds)

    override suspend fun deletePlaylist(playlistId: Long) {
        playlistRepository.deletePlaylist(playlistId)
    }
}
