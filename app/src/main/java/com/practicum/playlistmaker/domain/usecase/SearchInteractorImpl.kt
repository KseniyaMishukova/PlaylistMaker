package com.practicum.playlistmaker.domain.usecase

import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow

class SearchInteractorImpl(
    private val searchRepository: SearchRepository
) : SearchInteractor {

    override fun searchTracks(query: String): Flow<Result<List<Track>>> {
        return searchRepository.searchTracks(query)
    }
}