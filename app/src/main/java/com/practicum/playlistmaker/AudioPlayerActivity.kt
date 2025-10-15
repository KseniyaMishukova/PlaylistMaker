package com.practicum.playlistmaker

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class AudioPlayerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TRACK = "extra_track"
    }

    private var track: Track? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_audio_player)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.setPadding(view.paddingLeft, statusBar.top, view.paddingRight, view.paddingBottom)
            insets
        }

        findViewById<ImageView>(R.id.back_button).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        track = intent?.getSerializableExtra(EXTRA_TRACK) as? Track
        bindTrack()
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
}