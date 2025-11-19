package com.practicum.playlistmaker.domain

class SearchInteractorImpl(
    private val searchRepository: SearchRepository
) : SearchInteractor {

    override fun searchTracks(query: String, callback: (Result<List<Track>>) -> Unit) {
        searchRepository.searchTracks(query, callback)
    }
}