package com.practicum.playlistmaker.presentation.audio

data class AudioPlayerScreenState(
    val playerState: PlayerState,
    val progress: String,
    val isFavorite: Boolean
) {
    val isPlaying: Boolean
        get() = playerState == PlayerState.PLAYING
}
