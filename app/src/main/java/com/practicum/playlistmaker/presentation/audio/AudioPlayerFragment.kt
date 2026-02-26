package com.practicum.playlistmaker.presentation.audio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.models.Track
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class AudioPlayerFragment : Fragment() {

    companion object {
        const val ARG_TRACK = "arg_track"

        fun newInstance(track: Track): AudioPlayerFragment {
            return AudioPlayerFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_TRACK, track)
                }
            }
        }
    }

    private var track: Track? = null
    private lateinit var tvProgress: android.widget.TextView
    private lateinit var ivPlayPause: android.widget.ImageView

    private val viewModel: AudioPlayerViewModel by viewModel {
        parametersOf(requireNotNull(track) { "Track is required for AudioPlayer" })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        track = arguments?.getSerializable(ARG_TRACK) as? Track
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_audio_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvProgress = view.findViewById(R.id.tvProgress)
        ivPlayPause = view.findViewById(R.id.ivPlayPause)

        view.findViewById<android.widget.ImageView>(R.id.back_button).setOnClickListener {
            viewModel.onViewStopped()
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        bindTrack(view)

        view.findViewById<View>(R.id.btnPlay).setOnClickListener {
            viewModel.onPlayPauseClicked()
        }

        view.findViewById<View>(R.id.ivLike)?.setOnClickListener {
            viewModel.onFavoriteClicked()
        }

        observeViewModel()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onViewPaused()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.onViewStopped()
    }

    private fun bindTrack(view: View) {
        val t = track ?: return

        val ivCover = view.findViewById<android.widget.ImageView>(R.id.ivCoverLarge)
        val tvTitle = view.findViewById<android.widget.TextView>(R.id.tvTitle)
        val tvArtist = view.findViewById<android.widget.TextView>(R.id.tvArtist)

        val labelAlbum = view.findViewById<android.widget.TextView>(R.id.labelAlbum)
        val valueAlbum = view.findViewById<android.widget.TextView>(R.id.valueAlbum)
        val labelYear = view.findViewById<android.widget.TextView>(R.id.labelYear)
        val valueYear = view.findViewById<android.widget.TextView>(R.id.valueYear)
        val valueDuration = view.findViewById<android.widget.TextView>(R.id.valueDuration)
        val valueGenre = view.findViewById<android.widget.TextView>(R.id.valueGenre)
        val valueCountry = view.findViewById<android.widget.TextView>(R.id.valueCountry)

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

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            ivPlayPause.setImageResource(
                if (state.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
            )
            tvProgress.text = state.progress
            view?.findViewById<android.widget.ImageView>(R.id.ivLike)?.setImageResource(
                if (state.isFavorite) R.drawable.ic_like_love else R.drawable.ic_like
            )
        }
    }
}