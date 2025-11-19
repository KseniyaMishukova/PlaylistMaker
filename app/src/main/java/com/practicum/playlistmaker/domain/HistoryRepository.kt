package com.practicum.playlistmaker.domain

interface HistoryRepository {
    fun addTrack(track: Track)
    fun getHistory(): List<Track>
    fun clearHistory()
}