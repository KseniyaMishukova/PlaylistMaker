package com.practicum.playlistmaker.domain

interface SearchInteractor {
    fun searchTracks(query: String, callback: (Result<List<Track>>) -> Unit)
}