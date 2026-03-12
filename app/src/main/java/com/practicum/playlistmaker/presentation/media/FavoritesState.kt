package com.practicum.playlistmaker.presentation.media

sealed class FavoritesState {
    object Empty : FavoritesState()
    data class Content(val tracks: List<com.practicum.playlistmaker.domain.models.Track>) : FavoritesState()
}