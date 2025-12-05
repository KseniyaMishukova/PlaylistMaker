package com.practicum.playlistmaker.presentation.audio

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.domain.models.Track
import java.util.concurrent.TimeUnit

class AudioPlayerViewModel : ViewModel() {

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

    private val progressHandler = Handler(Looper.getMainLooper())
    private val progressRunnable = object : Runnable {
        override fun run() {
            val mp = mediaPlayer
            if (mp != null && playerState == PlayerState.PLAYING) {
                val posMs = mp.currentPosition.coerceAtLeast(0)
                _progress.postValue(formatMsToMmSs(posMs.toLong()))
                progressHandler.postDelayed(this, PROGRESS_UPDATE_INTERVAL_MS)
            }
        }
    }

    fun init(track: Track) {
        if (this.track != null) return
        this.track = track
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
        progressHandler.post(progressRunnable)
    }

    private fun stopProgressUpdates() {
        progressHandler.removeCallbacksAndMessages(null)
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
        private const val PROGRESS_UPDATE_INTERVAL_MS = 500L
    }
}