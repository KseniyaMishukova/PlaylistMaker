package com.practicum.playlistmaker.presentation.media

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.domain.usecase.FavoritesInteractor
import com.practicum.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

class FavoritesViewModel(
    private val favoritesInteractor: FavoritesInteractor
) : ViewModel() {

    private val _state = MutableLiveData<FavoritesState>(FavoritesState.Empty)
    val state: LiveData<FavoritesState> = _state

    init {
        viewModelScope.launch {
            favoritesInteractor.getFavoriteTracks().collectLatest { tracks ->
                _state.value = if (tracks.isEmpty()) {
                    FavoritesState.Empty
                } else {
                    FavoritesState.Content(tracks)
                }
            }
        }
    }
}