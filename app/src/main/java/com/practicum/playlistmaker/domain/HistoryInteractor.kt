package com.practicum.playlistmaker.domain

interface HistoryInteractor {
    fun addTrack(track: Track)
    fun getHistory(): List<Track>
    fun clearHistory()
}