package com.practicum.playlistmaker.domain

class HistoryInteractorImpl(
    private val historyRepository: HistoryRepository
) : HistoryInteractor {

    override fun addTrack(track: Track) {
        historyRepository.addTrack(track)
    }

    override fun getHistory(): List<Track> = historyRepository.getHistory()

    override fun clearHistory() {
        historyRepository.clearHistory()
    }
}