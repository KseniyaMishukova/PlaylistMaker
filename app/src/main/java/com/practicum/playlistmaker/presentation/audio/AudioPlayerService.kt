package com.practicum.playlistmaker.presentation.audio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.practicum.playlistmaker.R
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AudioPlayerService : Service(), AudioPlayerServiceController {

    companion object {
        const val EXTRA_PREVIEW_URL = "extra_preview_url"
        const val EXTRA_ARTIST_NAME = "extra_artist_name"
        const val EXTRA_TRACK_NAME = "extra_track_name"

        private const val NOTIFICATION_CHANNEL_ID = "playlist_maker_player"
        private const val NOTIFICATION_CHANNEL_NAME = "Playlist Maker"
        private const val NOTIFICATION_ID = 101

        private const val PROGRESS_UPDATE_INTERVAL_MS = 300L
    }

    private val binder = PlayerBinder()

    private val serviceJob = SupervisorJob()
    private val scope = CoroutineScope(serviceJob + Dispatchers.Main.immediate)

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null

    private var previewUrl: String = ""
    private var artistName: String = ""
    private var trackName: String = ""

    private val _state = MutableStateFlow(
        AudioPlayerServiceState(
            playerState = PlayerState.IDLE,
            progress = formatMsToMmSs(0L)
        )
    )
    private val state: StateFlow<AudioPlayerServiceState> = _state.asStateFlow()

    private var isForeground: Boolean = false
    private var hasClients: Boolean = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder {
        hasClients = true
        val url = intent?.getStringExtra(EXTRA_PREVIEW_URL).orEmpty()
        val artist = intent?.getStringExtra(EXTRA_ARTIST_NAME).orEmpty()
        val title = intent?.getStringExtra(EXTRA_TRACK_NAME).orEmpty()

        previewUrl = url
        artistName = artist
        trackName = title

        preparePlayer()

        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        hasClients = false
        if (!isForeground) stopAndRelease()
        return super.onUnbind(intent)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopForegroundNotification()
        stopAndRelease()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        stopForegroundNotification()
        stopAndRelease()
        serviceJob.cancel()
        super.onDestroy()
    }

    inner class PlayerBinder : Binder() {
        fun getService(): AudioPlayerService = this@AudioPlayerService
    }

    override fun stateFlow(): StateFlow<AudioPlayerServiceState> = state

    override fun playPause() {
        when (state.value.playerState) {
            PlayerState.PLAYING -> pausePlayback()
            PlayerState.PAUSED, PlayerState.PREPARED -> startPlayback()
            PlayerState.COMPLETED -> {
                seekToStart()
                startPlayback()
            }
            PlayerState.IDLE -> {
                preparePlayer(startWhenPrepared = true)
            }
            PlayerState.PREPARING, PlayerState.ERROR -> Unit
        }
    }

    override fun stopAndRelease() {
        stopProgressUpdates()
        releasePlayer()
    }

    override fun startForegroundNotification() {
        if (state.value.playerState != PlayerState.PLAYING) return
        if (isForeground) return

        val notification = buildNotification()
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        } else {
            0
        }
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            notification,
            type
        )
        isForeground = true
    }

    override fun stopForegroundNotification() {
        if (!isForeground) return
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        isForeground = false
    }

    private fun preparePlayer(startWhenPrepared: Boolean = false) {
        val url = previewUrl
        if (url.isBlank()) {
            updateState(playerState = PlayerState.ERROR)
            return
        }

        releasePlayer()
        updateState(playerState = PlayerState.PREPARING, progress = formatMsToMmSs(0L))

        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(url)
                setOnPreparedListener {
                    updateState(playerState = PlayerState.PREPARED)
                    if (startWhenPrepared) startPlayback()
                }
                setOnCompletionListener {
                    stopProgressUpdates()
                    updateState(playerState = PlayerState.COMPLETED, progress = formatMsToMmSs(0L))
                    stopForegroundNotification()
                }
                setOnErrorListener { _, _, _ ->
                    stopProgressUpdates()
                    updateState(playerState = PlayerState.ERROR)
                    stopForegroundNotification()
                    true
                }
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
        if (state.value.playerState != PlayerState.PLAYING) return
        try {
            mp.pause()
            updateState(playerState = PlayerState.PAUSED)
        } catch (_: Exception) {
            updateState(playerState = PlayerState.ERROR)
        } finally {
            stopProgressUpdates()
            stopForegroundNotification()
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
        updateState(playerState = PlayerState.IDLE, progress = formatMsToMmSs(0L))
        stopForegroundNotification()
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()
        progressJob = scope.launch {
            while (state.value.playerState == PlayerState.PLAYING) {
                val posMs = mediaPlayer?.currentPosition?.coerceAtLeast(0) ?: 0
                updateState(progress = formatMsToMmSs(posMs.toLong()))
                delay(PROGRESS_UPDATE_INTERVAL_MS)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun updateState(
        playerState: PlayerState? = null,
        progress: String? = null
    ) {
        val cur = _state.value
        _state.value = cur.copy(
            playerState = playerState ?: cur.playerState,
            progress = progress ?: cur.progress
        )
    }

    private fun buildNotification(): Notification {
        val text = listOf(artistName, trackName)
            .filter { it.isNotBlank() }
            .joinToString(" - ")
            .ifBlank { getString(R.string.app_name) }

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Playlist Maker")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        nm.createNotificationChannel(channel)
    }

    private fun formatMsToMmSs(ms: Long): String {
        val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(ms)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}
