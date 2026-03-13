package com.practicum.playlistmaker.presentation.media

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.domain.models.Playlist
import com.practicum.playlistmaker.domain.usecase.CreatePlaylistInteractor
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

class PlaylistsViewModel(
    private val createPlaylistInteractor: CreatePlaylistInteractor
) : ViewModel() {

    private val _playlists = MutableLiveData<List<Playlist>>(emptyList())
    val playlists: LiveData<List<Playlist>> = _playlists

    init {
        loadPlaylists()
    }

    private fun loadPlaylists() {
        viewModelScope.launch {
            createPlaylistInteractor.getPlaylists().collectLatest { list ->
                _playlists.value = list
            }
        }
    }
}
