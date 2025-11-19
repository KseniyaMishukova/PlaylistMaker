package com.practicum.playlistmaker.presentation.audio

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.models.Track
import java.util.concurrent.TimeUnit

class AudioPlayerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TRACK = "extra_track"
        private const val PROGRESS_UPDATE_INTERVAL_MS = 500L
    }

    private var track: Track? = null

    private var mediaPlayer: MediaPlayer? = null
    private enum class PlayerState { IDLE, PREPARING, PREPARED, PLAYING, PAUSED, COMPLETED, ERROR }
    private var playerState: PlayerState = PlayerState.IDLE
    private var startOnPrepared: Boolean = false

    private lateinit var tvProgress: TextView
    private lateinit var ivPlayPause: ImageView

    private val progressHandler = Handler(Looper.getMainLooper())
    private val progressRunnable = object : Runnable {
        override fun run() {
            val mp = mediaPlayer
            if (mp != null && playerState == PlayerState.PLAYING) {
                val posMs = mp.currentPosition.coerceAtLeast(0)
                tvProgress.text = formatMsToMmSs(posMs.toLong())
                progressHandler.postDelayed(this, PROGRESS_UPDATE_INTERVAL_MS)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_audio_player)

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                stopProgressUpdates()
                releasePlayer()
                finish()
            }
        })

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.setPadding(view.paddingLeft, statusBar.top, view.paddingRight, view.paddingBottom)
            insets
        }

        findViewById<ImageView>(R.id.back_button).setOnClickListener {
            stopAndFinish()
        }

        tvProgress = findViewById(R.id.tvProgress)
        ivPlayPause = findViewById(R.id.ivPlayPause)

        findViewById<View>(R.id.btnPlay).setOnClickListener {
            when (playerState) {
                PlayerState.PLAYING -> pausePlayback()
                PlayerState.PAUSED, PlayerState.PREPARED -> startPlayback()
                PlayerState.COMPLETED -> {
                    seekToStart()
                    startPlayback()
                }
                PlayerState.IDLE -> preparePlayerAndPlay()
                PlayerState.PREPARING, PlayerState.ERROR -> { }
            }
        }

        track = intent?.getSerializableExtra(EXTRA_TRACK) as? Track
        bindTrack()

        preparePlayer()
        setProgressText(0L)
        updatePlayButton(isPlaying = false)
    }

    override fun onPause() {
        super.onPause()

        if (playerState == PlayerState.PLAYING) {
            pausePlayback()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopProgressUpdates()
        releasePlayer()
    }

    private fun stopAndFinish() {
        stopProgressUpdates()
        releasePlayer()
        finish()
    }

    private fun bindTrack() {
        val t = track ?: return

        val ivCover = findViewById<ImageView>(R.id.ivCoverLarge)
        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val tvArtist = findViewById<TextView>(R.id.tvArtist)

        val labelAlbum = findViewById<TextView>(R.id.labelAlbum)
        val valueAlbum = findViewById<TextView>(R.id.valueAlbum)
        val labelYear = findViewById<TextView>(R.id.labelYear)
        val valueYear = findViewById<TextView>(R.id.valueYear)
        val valueDuration = findViewById<TextView>(R.id.valueDuration)
        val valueGenre = findViewById<TextView>(R.id.valueGenre)
        val valueCountry = findViewById<TextView>(R.id.valueCountry)

        tvTitle.text = t.trackName
        tvArtist.text = t.artistName

        valueDuration.text = t.trackTime
        valueGenre.text = t.primaryGenreName
        valueCountry.text = t.country

        if (t.collectionName.isNullOrBlank()) {
            labelAlbum.visibility = View.GONE
            valueAlbum.visibility = View.GONE
        } else {
            valueAlbum.text = t.collectionName
            labelAlbum.visibility = View.VISIBLE
            valueAlbum.visibility = View.VISIBLE
        }

        if (t.releaseYear.isNullOrBlank()) {
            labelYear.visibility = View.GONE
            valueYear.visibility = View.GONE
        } else {
            valueYear.text = t.releaseYear
            labelYear.visibility = View.VISIBLE
            valueYear.visibility = View.VISIBLE
        }

        val radius = resources.getDimensionPixelSize(R.dimen.track_corner_radius)
        Glide.with(this)
            .load(t.getCoverArtwork())
            .placeholder(R.drawable.ic_zig)
            .error(R.drawable.ic_zig)
            .centerCrop()
            .transform(RoundedCorners(radius))
            .into(ivCover)
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
                    updatePlayButton(isPlaying = false)
                    if (startOnPrepared) {
                        startOnPrepared = false
                        startPlayback()
                    }
                }
                setOnCompletionListener {
                    playerState = PlayerState.COMPLETED
                    stopProgressUpdates()
                    setProgressText(0L)
                    updatePlayButton(isPlaying = false)
                }
                setOnErrorListener { _, _, _ ->
                    playerState = PlayerState.ERROR
                    stopProgressUpdates()
                    updatePlayButton(isPlaying = false)
                    true
                }
                playerState = PlayerState.PREPARING
                prepareAsync()
            } catch (_: Exception) {
                playerState = PlayerState.ERROR
            }
        }
    }

    private fun preparePlayerAndPlay() {
        when (playerState) {
            PlayerState.IDLE, PlayerState.ERROR -> {
                startOnPrepared = true
                preparePlayer()
            }
            PlayerState.PREPARED -> startPlayback()
            PlayerState.PREPARING -> {
                startOnPrepared = true
            }
            PlayerState.PAUSED, PlayerState.COMPLETED -> startPlayback()
            else -> { }
        }
    }

    private fun startPlayback() {
        val mp = mediaPlayer ?: return
        try {
            mp.start()
            playerState = PlayerState.PLAYING
            updatePlayButton(isPlaying = true)
            startProgressUpdates()
        } catch (_: Exception) {
            playerState = PlayerState.ERROR
            updatePlayButton(isPlaying = false)
        }
    }

    private fun pausePlayback() {
        val mp = mediaPlayer ?: return
        if (playerState == PlayerState.PLAYING) {
            try {
                mp.pause()
                playerState = PlayerState.PAUSED
                updatePlayButton(isPlaying = false)
            } catch (_: Exception) {
                playerState = PlayerState.ERROR
            } finally {
                stopProgressUpdates()
            }
        }
    }

    private fun seekToStart() {
        mediaPlayer?.seekTo(0)
        setProgressText(0L)
    }

    private fun releasePlayer() {
        try {
            mediaPlayer?.release()
        } catch (_: Exception) { }
        mediaPlayer = null
        playerState = PlayerState.IDLE
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()
        progressHandler.post(progressRunnable)
    }

    private fun stopProgressUpdates() {
        progressHandler.removeCallbacksAndMessages(null)
    }

    private fun setProgressText(ms: Long) {
        tvProgress.text = formatMsToMmSs(ms)
    }

    private fun formatMsToMmSs(ms: Long): String {
        val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(ms)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun updatePlayButton(isPlaying: Boolean) {
        ivPlayPause.setImageResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
    }
}