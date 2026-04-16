package com.practicum.playlistmaker.presentation.audio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.bottomsheet.BottomSheetBehavior
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
    private lateinit var playbackButton: PlaybackButtonView
    private lateinit var playlistAdapter: PlaylistBottomSheetAdapter

    private val viewModel: AudioPlayerViewModel by viewModel {
        parametersOf(requireNotNull(track) { "Track is required for AudioPlayer" })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        track = arguments?.readTrackArg()
        super.onCreate(savedInstanceState)
    }

    private fun Bundle?.readTrackArg(): Track? {
        if (this == null) return null
        classLoader = Track::class.java.classLoader
        return BundleCompat.getSerializable(this, ARG_TRACK, Track::class.java)
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
        playbackButton = view.findViewById(R.id.playbackButton)

        view.findViewById<android.widget.ImageView>(R.id.back_button).setOnClickListener {
            viewModel.onViewStopped()
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        bindTrack(view)

        playbackButton.setOnToggleListener {
            viewModel.onPlayPauseClicked()
        }

        view.findViewById<View>(R.id.ivLike)?.setOnClickListener {
            viewModel.onFavoriteClicked()
        }

        setupAddToPlaylistButton(view)
        setupBottomSheet(view)
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

    private fun setupAddToPlaylistButton(view: View) {
        view.findViewById<View>(R.id.btnAddToPlaylist).setOnClickListener {
            viewModel.loadPlaylists()
            val bottomSheet = view.findViewById<View>(R.id.playlists_bottom_sheet)
            BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }
    }

    private fun setupBottomSheet(view: View) {
        val bottomSheet = view.findViewById<android.widget.LinearLayout>(R.id.playlists_bottom_sheet)
        val overlay = view.findViewById<View>(R.id.overlay)

        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
        }

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> overlay.visibility = View.GONE
                    else -> overlay.visibility = View.VISIBLE
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        overlay.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        playlistAdapter = PlaylistBottomSheetAdapter { playlist ->
            viewModel.onPlaylistSelected(playlist)
        }
        view.findViewById<RecyclerView>(R.id.playlists_recycler).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = playlistAdapter
        }

        view.findViewById<View>(R.id.bottom_sheet_new_playlist).setOnClickListener {
            viewModel.onNewPlaylistClicked()
        }
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
            state ?: return@observe
            playbackButton.setPlaying(state.isPlaying)
            tvProgress.text = state.progress
            view?.findViewById<android.widget.ImageView>(R.id.ivLike)?.setImageResource(
                if (state.isFavorite) R.drawable.ic_like_love else R.drawable.ic_like
            )
            playlistAdapter.setItems(state.playlists)

            state.addToPlaylistResult?.let { result ->
                viewModel.clearAddToPlaylistResult()
                val message = when (result) {
                    is AddToPlaylistResult.Added -> {
                        view?.findViewById<View>(R.id.playlists_bottom_sheet)?.let { sheet ->
                            BottomSheetBehavior.from(sheet).state = BottomSheetBehavior.STATE_HIDDEN
                        }
                        getString(R.string.added_to_playlist, result.playlistName)
                    }
                    is AddToPlaylistResult.AlreadyInPlaylist -> getString(R.string.track_already_in_playlist, result.playlistName)
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }

            if (state.navigateToCreatePlaylist) {
                viewModel.clearNavigateToCreatePlaylist()
                view?.findViewById<View>(R.id.playlists_bottom_sheet)?.let {
                    BottomSheetBehavior.from(it).state = BottomSheetBehavior.STATE_HIDDEN
                }
                findNavController().navigate(R.id.action_audioPlayer_to_createPlaylist)
            }
        }
    }
}
