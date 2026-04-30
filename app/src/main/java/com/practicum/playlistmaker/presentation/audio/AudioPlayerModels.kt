package com.practicum.playlistmaker.presentation.audio

import com.practicum.playlistmaker.domain.models.Playlist

enum class PlayerState {
    IDLE,
    PREPARING,
    PREPARED,
    PLAYING,
    PAUSED,
    COMPLETED,
    ERROR
}

sealed class AddToPlaylistResult {
    data class Added(val playlistName: String) : AddToPlaylistResult()
    data class AlreadyInPlaylist(val playlistName: String) : AddToPlaylistResult()
}

data class AudioPlayerScreenState(
    val playerState: PlayerState,
    val progress: String,
    val isFavorite: Boolean,
    val playlists: List<Playlist> = emptyList(),
    val addToPlaylistResult: AddToPlaylistResult? = null,
    val navigateToCreatePlaylist: Boolean = false
) {
    val isPlaying: Boolean
        get() = playerState == PlayerState.PLAYING
}

