package com.practicum.playlistmaker.presentation.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.domain.usecase.HistoryInteractor
import com.practicum.playlistmaker.domain.usecase.SearchInteractor
import kotlinx.coroutines.*

import androidx.lifecycle.viewModelScope


class SearchViewModel(
    private val searchInteractor: SearchInteractor,
    private val historyInteractor: HistoryInteractor
) : ViewModel() {

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }

    private var searchJob: Job? = null

    private val _state = MutableLiveData<SearchScreenState>(SearchScreenState.Idle)
    val state: LiveData<SearchScreenState> = _state

    private var lastQuery: String = ""
    private var isLoading: Boolean = false

    init {
        showHistory()
    }

    fun onQueryChanged(query: String) {
        debounceSearch(query.trim())
    }

    fun onSearchButtonClicked(query: String) {
        performSearch(query.trim())
    }

    fun onClearClicked() {
        lastQuery = ""
        cancelDebounce()
        showHistory()
    }

    fun onRetryClicked() {
        if (lastQuery.isNotEmpty()) {
            performSearch(lastQuery)
        }
    }

    fun onClearHistoryClicked() {
        historyInteractor.clearHistory()
        _state.value = SearchScreenState.Idle
    }

    fun onTrackClicked(track: Track) {
        historyInteractor.addTrack(track)
    }

    private fun showHistory() {
        val history = historyInteractor.getHistory()
        _state.value = if (history.isEmpty()) {
            SearchScreenState.Idle
        } else {
            SearchScreenState.History(history)
        }
    }

    private fun debounceSearch(query: String) {
        cancelDebounce()
        if (query.isEmpty()) {
            lastQuery = ""
            showHistory()
            return
        }
        if (query == lastQuery && !isLoading) return
        
        searchJob = CoroutineScope(Dispatchers.Main).launch {
            delay(SEARCH_DEBOUNCE_DELAY)
            performSearch(query)
        }
    }

    private fun cancelDebounce() {
        searchJob?.cancel()
        searchJob = null
    }

    private fun performSearch(query: String) {
        if (query.isEmpty()) return
        lastQuery = query
        isLoading = true
        _state.value = SearchScreenState.Loading
        
        viewModelScope.launch {
            searchInteractor.searchTracks(query)
                .collect { result ->
                    isLoading = false
                    result.onSuccess { tracks ->
                        if (tracks.isEmpty()) {
                            _state.postValue(SearchScreenState.Empty)
                        } else {
                            _state.postValue(SearchScreenState.Content(tracks))
                        }
                    }.onFailure { error ->
                        _state.postValue(SearchScreenState.Error(error.message.orEmpty()))
                    }
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        cancelDebounce()
    }
}