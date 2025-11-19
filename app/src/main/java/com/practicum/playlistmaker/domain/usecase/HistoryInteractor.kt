package com.practicum.playlistmaker.domain.usecase

import com.practicum.playlistmaker.domain.models.Track

interface HistoryInteractor {
    fun addTrack(track: Track)
    fun getHistory(): List<Track>
    fun clearHistory()
}