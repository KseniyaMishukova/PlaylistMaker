package com.practicum.playlistmaker.presentation.search

import com.practicum.playlistmaker.domain.models.Track

sealed interface SearchScreenState {
    data object Idle : SearchScreenState
    data object Loading : SearchScreenState
    data class History(val tracks: List<Track>) : SearchScreenState
    data class Content(val tracks: List<Track>) : SearchScreenState
    data object Empty : SearchScreenState
    data class Error(val message: String) : SearchScreenState
}