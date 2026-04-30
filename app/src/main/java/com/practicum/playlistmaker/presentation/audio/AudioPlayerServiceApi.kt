package com.practicum.playlistmaker.presentation.audio

import kotlinx.coroutines.flow.StateFlow

data class AudioPlayerServiceState(
    val playerState: PlayerState,
    val progress: String
)

interface AudioPlayerServiceController {
    fun stateFlow(): StateFlow<AudioPlayerServiceState>
    fun playPause()
    fun stopAndRelease()
    fun startForegroundNotification()
    fun stopForegroundNotification()
}

