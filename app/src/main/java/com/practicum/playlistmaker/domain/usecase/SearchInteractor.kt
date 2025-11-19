package com.practicum.playlistmaker.domain.usecase

import com.practicum.playlistmaker.domain.models.Track

interface SearchInteractor {
    fun searchTracks(query: String, callback: (Result<List<Track>>) -> Unit)
}