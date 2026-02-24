package com.practicum.playlistmaker.presentation.audio

import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.domain.usecase.FavoritesInteractor
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.*
import androidx.lifecycle.viewModelScope

class AudioPlayerViewModel(
    private val favoritesInteractor: FavoritesInteractor
) : ViewModel() {

    private var mediaPlayer: MediaPlayer? = null
    private var playerState: PlayerState = PlayerState.IDLE
    private var startOnPrepared: Boolean = false
    private var track: Track? = null

    private val _isPlaying = MutableLiveData<Boolean>(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _progress = MutableLiveData<String>().apply {
        value = formatMsToMmSs(0L)
    }
    val progress: LiveData<String> = _progress

    private val _isFavorite = MutableLiveData<Boolean>(false)
    val isFavorite: LiveData<Boolean> = _isFavorite

    private var progressJob: Job? = null

    fun init(track: Track) {
        if (this.track != null) return
        this.track = track
        viewModelScope.launch {
            _isFavorite.value = favoritesInteractor.isFavorite(track.trackId)
        }
        _progress.value = formatMsToMmSs(0L)
        preparePlayer()
    }

    fun onPlayPauseClicked() {
        when (playerState) {
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
        val currentTrack = this.track ?: return
        viewModelScope.launch {
            val newValue = !currentTrack.isFavorite
            if (newValue) {
                favoritesInteractor.addTrack(currentTrack)
            } else {
                favoritesInteractor.removeTrack(currentTrack)
            }
            currentTrack.isFavorite = newValue
            _isFavorite.postValue(newValue)
        }
    }

    fun onViewPaused() {
        if (playerState == PlayerState.PLAYING) {
            pausePlayback()
        }
    }

    fun onViewStopped() {
        stopProgressUpdates()
        releasePlayer()
    }

    private fun preparePlayer() {
        val url = track?.previewUrl.orEmpty()
        if (url.isEmpty()) {
            playerState = PlayerState.ERROR
            return
        }
        releasePlayer()
        startOnPrepared = false
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(url)
                setOnPreparedListener {
                    playerState = PlayerState.PREPARED
                    _isPlaying.postValue(false)
                    if (startOnPrepared) {
                        startOnPrepared = false
                        startPlayback()
                    }
                }
                setOnCompletionListener {
                    playerState = PlayerState.COMPLETED
                    stopProgressUpdates()
                    _progress.postValue(formatMsToMmSs(0L))
                    _isPlaying.postValue(false)
                }
                setOnErrorListener { _, _, _ ->
                    playerState = PlayerState.ERROR
                    stopProgressUpdates()
                    _isPlaying.postValue(false)
                    true
                }
                playerState = PlayerState.PREPARING
                prepareAsync()
            } catch (_: Exception) {
                playerState = PlayerState.ERROR
            }
        }
    }

    private fun startPlayback() {
        val mp = mediaPlayer ?: return
        try {
            mp.start()
            playerState = PlayerState.PLAYING
            _isPlaying.postValue(true)
            startProgressUpdates()
        } catch (_: Exception) {
            playerState = PlayerState.ERROR
            _isPlaying.postValue(false)
        }
    }

    private fun pausePlayback() {
        val mp = mediaPlayer ?: return
        if (playerState == PlayerState.PLAYING) {
            try {
                mp.pause()
                playerState = PlayerState.PAUSED
                _isPlaying.postValue(false)
            } catch (_: Exception) {
                playerState = PlayerState.ERROR
            } finally {
                stopProgressUpdates()
            }
        }
    }

    private fun seekToStart() {
        mediaPlayer?.seekTo(0)
        _progress.postValue(formatMsToMmSs(0L))
    }

    private fun releasePlayer() {
        try {
            mediaPlayer?.release()
        } catch (_: Exception) {
        }
        mediaPlayer = null
        playerState = PlayerState.IDLE
        _isPlaying.postValue(false)
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()
        progressJob = viewModelScope.launch {
            while (playerState == PlayerState.PLAYING) {
                val mp = mediaPlayer
                if (mp != null) {
                    val posMs = mp.currentPosition.coerceAtLeast(0)
                    _progress.postValue(formatMsToMmSs(posMs.toLong()))
                }
                delay(PROGRESS_UPDATE_INTERVAL_MS)
            }

            if (playerState == PlayerState.COMPLETED) {
                _progress.postValue(formatMsToMmSs(0L))
            } else if (playerState != PlayerState.PLAYING) {

                val mp = mediaPlayer
                if (mp != null) {
                    val posMs = mp.currentPosition.coerceAtLeast(0)
                    _progress.postValue(formatMsToMmSs(posMs.toLong()))
                }
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
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