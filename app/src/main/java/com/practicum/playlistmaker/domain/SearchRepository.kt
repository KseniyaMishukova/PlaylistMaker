package com.practicum.playlistmaker.domain

interface SearchRepository {
    fun searchTracks(query: String, callback: (Result<List<Track>>) -> Unit)
}