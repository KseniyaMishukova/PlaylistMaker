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

    override fun getPlaylists(): Flow<List<Playlist>> = playlistRepository.getAllPlaylists()

    override suspend fun addTrackToPlaylist(playlist: Playlist, track: Track) {
        playlistRepository.addTrackToPlaylist(playlist, track)
    }
}
