package com.practicum.playlistmaker.presentation.audio

import android.media.MediaPlayer
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.data.player.MediaPlayerFactory
import com.practicum.playlistmaker.domain.models.Playlist
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.domain.usecase.CreatePlaylistInteractor
import com.practicum.playlistmaker.domain.usecase.FavoritesInteractor
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import androidx.lifecycle.viewModelScope

class AudioPlayerViewModel(
    private val track: Track,
    private val favoritesInteractor: FavoritesInteractor,
    private val createPlaylistInteractor: CreatePlaylistInteractor,
    private val mediaPlayerFactory: MediaPlayerFactory
) : ViewModel() {

    private var mediaPlayer: MediaPlayer? = null
    private var startOnPrepared: Boolean = false

    private var currentState = AudioPlayerScreenState(
        playerState = PlayerState.IDLE,
        progress = formatMsToMmSs(0L),
        isFavorite = false
    )
    private val _state = MutableLiveData(currentState)
    val state: LiveData<AudioPlayerScreenState> = _state

    private var progressJob: Job? = null

    init {
        viewModelScope.launch {
            val isFavorite = favoritesInteractor.isFavorite(track.trackId)
            updateState(progress = formatMsToMmSs(0L), isFavorite = isFavorite)
        }
        preparePlayer()
    }

    fun onPlayPauseClicked() {
        when (currentState.playerState) {
            PlayerState.PLAYING -> pausePlayback()
            PlayerState.PAUSED, PlayerState.PREPARED -> startPlayback()
            PlayerState.COMPLETED -> {
                seekToStart()
                startPlayback()
            }
            PlayerState.IDLE -> {
                startOnPrepared = true
                preparePlayer()
            }
            PlayerState.PREPARING, PlayerState.ERROR -> { }
        }
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

    fun onViewPaused() {
        if (currentState.playerState == PlayerState.PLAYING) {
            pausePlayback()
        }
    }

    fun onViewStopped() {
        stopProgressUpdates()
        releasePlayer()
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

    private fun preparePlayer() {
        val url = track.previewUrl
        if (url.isEmpty()) {
            updateState(playerState = PlayerState.ERROR)
            return
        }
        val playWhenPrepared = startOnPrepared
        startOnPrepared = false
        releasePlayer()
        mediaPlayer = mediaPlayerFactory.create().apply {
            try {
                setDataSource(url)
                setOnPreparedListener {
                    updateState(playerState = PlayerState.PREPARED)
                    if (playWhenPrepared) {
                        startPlayback()
                    }
                }
                setOnCompletionListener {
                    stopProgressUpdates()
                    updateState(playerState = PlayerState.COMPLETED, progress = formatMsToMmSs(0L))
                }
                setOnErrorListener { _, _, _ ->
                    stopProgressUpdates()
                    updateState(playerState = PlayerState.ERROR)
                    true
                }
                updateState(playerState = PlayerState.PREPARING)
                prepareAsync()
            } catch (_: Exception) {
                updateState(playerState = PlayerState.ERROR)
            }
        }
    }

    private fun startPlayback() {
        val mp = mediaPlayer ?: return
        try {
            mp.start()
            updateState(playerState = PlayerState.PLAYING)
            startProgressUpdates()
        } catch (_: Exception) {
            updateState(playerState = PlayerState.ERROR)
        }
    }

    private fun pausePlayback() {
        val mp = mediaPlayer ?: return
        if (currentState.playerState == PlayerState.PLAYING) {
            try {
                mp.pause()
                updateState(playerState = PlayerState.PAUSED)
            } catch (_: Exception) {
                updateState(playerState = PlayerState.ERROR)
            } finally {
                stopProgressUpdates()
            }
        }
    }

    private fun seekToStart() {
        mediaPlayer?.seekTo(0)
        updateState(progress = formatMsToMmSs(0L))
    }

    private fun releasePlayer() {
        try {
            mediaPlayer?.release()
        } catch (_: Exception) {
        }
        mediaPlayer = null
        updateState(playerState = PlayerState.IDLE)
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()
        progressJob = viewModelScope.launch {
            while (currentState.playerState == PlayerState.PLAYING) {
                val mp = mediaPlayer
                if (mp != null) {
                    val posMs = mp.currentPosition.coerceAtLeast(0)
                    updateState(progress = formatMsToMmSs(posMs.toLong()))
                }
                delay(PROGRESS_UPDATE_INTERVAL_MS)
            }

            if (currentState.playerState == PlayerState.COMPLETED) {
                updateState(progress = formatMsToMmSs(0L))
            } else if (currentState.playerState != PlayerState.PLAYING) {
                val mp = mediaPlayer
                if (mp != null) {
                    val posMs = mp.currentPosition.coerceAtLeast(0)
                    updateState(progress = formatMsToMmSs(posMs.toLong()))
                }
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
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
        stopProgressUpdates()
        releasePlayer()
    }

    companion object {
        private const val PROGRESS_UPDATE_INTERVAL_MS = 300L
    }
}

enum class PlayerState {
    IDLE,
    PREPARING,
    PREPARED,
    PLAYING,
    PAUSED,
    COMPLETED,
    ERROR
}

sealed class AddToPlaylistResult {
    data class Added(val playlistName: String) : AddToPlaylistResult()
    data class AlreadyInPlaylist(val playlistName: String) : AddToPlaylistResult()
}

data class AudioPlayerScreenState(
    val playerState: PlayerState,
    val progress: String,
    val isFavorite: Boolean,
    val playlists: List<Playlist> = emptyList(),
    val addToPlaylistResult: AddToPlaylistResult? = null,
    val navigateToCreatePlaylist: Boolean = false
) {
    val isPlaying: Boolean
        get() = playerState == PlayerState.PLAYING
}
