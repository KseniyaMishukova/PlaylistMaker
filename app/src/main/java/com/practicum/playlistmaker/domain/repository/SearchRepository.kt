package com.practicum.playlistmaker.domain.repository

import com.practicum.playlistmaker.domain.models.Track

interface SearchRepository {
    fun searchTracks(query: String, callback: (Result<List<Track>>) -> Unit)
}