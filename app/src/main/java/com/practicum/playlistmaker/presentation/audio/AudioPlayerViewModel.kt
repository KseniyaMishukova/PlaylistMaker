package com.practicum.playlistmaker.presentation.audio

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.domain.models.Playlist
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.domain.usecase.CreatePlaylistInteractor
import com.practicum.playlistmaker.domain.usecase.FavoritesInteractor
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import android.os.Looper

class AudioPlayerViewModel(
    private val track: Track,
    private val favoritesInteractor: FavoritesInteractor,
    private val createPlaylistInteractor: CreatePlaylistInteractor
) : ViewModel() {

    private var playerController: AudioPlayerServiceController? = null
    private var playerStateJob: Job? = null

    private var currentState = AudioPlayerScreenState(
        playerState = PlayerState.IDLE,
        progress = formatMsToMmSs(0L),
        isFavorite = false
    )
    private val _state = MutableLiveData(currentState)
    val state: LiveData<AudioPlayerScreenState> = _state

    init {
        viewModelScope.launch {
            val isFavorite = favoritesInteractor.isFavorite(track.trackId)
            updateState(progress = formatMsToMmSs(0L), isFavorite = isFavorite)
        }
    }

    fun onServiceBound(controller: AudioPlayerServiceController) {
        playerController = controller
        observePlayerState(controller.stateFlow())
    }

    fun onPlayPauseClicked() {
        playerController?.playPause()
    }

    fun onFavoriteClicked() {
        viewModelScope.launch {
            val newValue = !track.isFavorite
            if (newValue) {
                favoritesInteractor.addTrack(track)
            } else {
                favoritesInteractor.removeTrack(track)
            }
            track.isFavorite = newValue
            updateState(isFavorite = newValue)
        }
    }

    fun onScreenClosed() {
        playerController?.stopAndRelease()
    }

    fun onAppBackgrounded() {
        if (currentState.playerState == PlayerState.PLAYING) {
            playerController?.startForegroundNotification()
        }
    }

    fun onAppForegrounded() {
        playerController?.stopForegroundNotification()
    }

    fun loadPlaylists() {
        viewModelScope.launch {
            val list = createPlaylistInteractor.getPlaylists().first()
            updateState(playlists = list)
        }
    }

    fun onPlaylistSelected(playlist: Playlist) {
        if (track.trackId in playlist.trackIds) {
            updateState(addToPlaylistResult = AddToPlaylistResult.AlreadyInPlaylist(playlist.name))
            return
        }
        viewModelScope.launch {
            createPlaylistInteractor.addTrackToPlaylist(playlist, track)
            updateState(addToPlaylistResult = AddToPlaylistResult.Added(playlist.name))
        }
    }

    fun onNewPlaylistClicked() {
        updateState(navigateToCreatePlaylist = true)
    }

    fun clearAddToPlaylistResult() {
        currentState = currentState.copy(addToPlaylistResult = null)
        publishState()
    }

    fun clearNavigateToCreatePlaylist() {
        currentState = currentState.copy(navigateToCreatePlaylist = false)
        publishState()
    }

    private fun observePlayerState(flow: StateFlow<AudioPlayerServiceState>) {
        playerStateJob?.cancel()
        playerStateJob = viewModelScope.launch {
            flow.collectLatest { s ->
                updateState(
                    playerState = s.playerState,
                    progress = s.progress
                )
            }
        }
    }

    private fun updateState(
        playerState: PlayerState? = null,
        progress: String? = null,
        isFavorite: Boolean? = null,
        playlists: List<Playlist>? = null,
        addToPlaylistResult: AddToPlaylistResult? = null,
        navigateToCreatePlaylist: Boolean? = null
    ) {
        currentState = currentState.copy(
            playerState = playerState ?: currentState.playerState,
            progress = progress ?: currentState.progress,
            isFavorite = isFavorite ?: currentState.isFavorite,
            playlists = playlists ?: currentState.playlists,
            addToPlaylistResult = addToPlaylistResult ?: currentState.addToPlaylistResult,
            navigateToCreatePlaylist = navigateToCreatePlaylist ?: currentState.navigateToCreatePlaylist
        )
        publishState()
    }

    private fun publishState() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            _state.value = currentState
        } else {
            _state.postValue(currentState)
        }
    }

    private fun formatMsToMmSs(ms: Long): String {
        val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(ms)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onCleared() {
        super.onCleared()
        playerStateJob?.cancel()
        playerStateJob = null
    }
}
