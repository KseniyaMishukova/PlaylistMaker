package com.practicum.playlistmaker.domain.usecase

import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.domain.repository.SearchRepository

class SearchInteractorImpl(
    private val searchRepository: SearchRepository
) : SearchInteractor {

    override fun searchTracks(query: String, callback: (Result<List<Track>>) -> Unit) {
        searchRepository.searchTracks(query, callback)
    }
}