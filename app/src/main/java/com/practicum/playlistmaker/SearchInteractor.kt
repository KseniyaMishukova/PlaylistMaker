package com.practicum.playlistmaker

import android.content.Context

class SearchInteractor(
    context: Context
) {
    private val history = SearchHistory(context)

    fun addToHistory(track: Track) {
        history.addTrack(track)
    }

    fun getHistory(): List<Track> = history.getHistory()

    fun clearHistory() {
        history.clear()
    }
}