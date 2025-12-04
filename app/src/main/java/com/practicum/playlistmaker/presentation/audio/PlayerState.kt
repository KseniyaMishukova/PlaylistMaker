package com.practicum.playlistmaker.presentation.audio

enum class PlayerState {
    IDLE,
    PREPARING,
    PREPARED,
    PLAYING,
    PAUSED,
    COMPLETED,
    ERROR
}