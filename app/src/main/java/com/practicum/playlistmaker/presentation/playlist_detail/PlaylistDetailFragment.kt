package com.practicum.playlistmaker.presentation.playlist_detail

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.presentation.create_playlist.EditPlaylistFragment
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.presentation.search.TrackAdapter
import com.practicum.playlistmaker.util.RussianPlurals
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class PlaylistDetailFragment : Fragment() {

    companion object {
        const val ARG_PLAYLIST_ID = "playlist_id"
    }

    private val viewModel: PlaylistDetailViewModel by viewModel {
        parametersOf(requireArguments().getLong(ARG_PLAYLIST_ID))
    }

    private lateinit var tracksAdapter: TrackAdapter

    private lateinit var bottomSheet: LinearLayout

    private var bottomSheetBehavior: BottomSheetBehavior<out View>? = null

    private var menuBottomSheetBehavior: BottomSheetBehavior<out View>? = null

    private var tracksSheetPeekApplied: Boolean = false
    private var tracksSheetPeekLastHasCover: Boolean? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_playlist_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheet = view.findViewById(R.id.buttom_shee)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet).apply {
            isFitToContents = false
            skipCollapsed = false
            state = BottomSheetBehavior.STATE_COLLAPSED
        }
        scheduleApplyBottomSheetPeek(view, hasCover = null)

        val menuSheet = view.findViewById<View>(R.id.playlist_menu_bottom_sheet)
        val menuOverlay = view.findViewById<View>(R.id.playlist_menu_overlay)
        menuBottomSheetBehavior = BottomSheetBehavior.from(menuSheet).apply {
            isFitToContents = false
            halfExpandedRatio = 0.5f
            state = BottomSheetBehavior.STATE_HIDDEN
            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    menuOverlay.visibility =
                        if (newState == BottomSheetBehavior.STATE_HIDDEN) View.GONE else View.VISIBLE
                    applyPlaylistMenuBottomOffset(menuSheet, newState == BottomSheetBehavior.STATE_HIDDEN)
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    val bh = menuBottomSheetBehavior
                    if (bh != null) {
                        applyPlaylistMenuBottomOffset(menuSheet, bh.state == BottomSheetBehavior.STATE_HIDDEN)
                    }
                }
            })
        }
        menuOverlay.setOnClickListener {
            menuBottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
        }

        val tracksRecycler = view.findViewById<RecyclerView>(R.id.playlist_tracks_recycler)
        tracksRecycler.layoutManager = LinearLayoutManager(requireContext())
        tracksAdapter = TrackAdapter(
            mutableListOf(),
            R.layout.item_track_playlist_detail,
            onItemClick = { track -> viewModel.onTrackClicked(track) },
            onItemLongClick = { track -> showDeleteTrackDialog(track) }
        )
        tracksRecycler.adapter = tracksAdapter
        tracksRecycler.setHasFixedSize(true)

        view.findViewById<ImageButton>(R.id.back_button).setOnClickListener { onPlaylistBackPressed() }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onPlaylistBackPressed()
                }
            }
        )

        view.findViewById<ImageButton>(R.id.share_button).setOnClickListener { trySharePlaylist() }
        view.findViewById<ImageButton>(R.id.menu_button).setOnClickListener {
            bindMenuSheetContent(view)
            menuBottomSheetBehavior?.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }

        view.findViewById<TextView>(R.id.playlist_menu_action_share).setOnClickListener {
            menuBottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
            trySharePlaylist()
        }
        view.findViewById<TextView>(R.id.playlist_menu_action_edit).setOnClickListener {
            val id = viewModel.state.value?.playlist?.id ?: return@setOnClickListener
            menuBottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
            findNavController().navigate(
                R.id.action_playlistDetail_to_editPlaylist,
                bundleOf(EditPlaylistFragment.ARG_PLAYLIST_ID to id)
            )
        }
        view.findViewById<TextView>(R.id.playlist_menu_action_delete).setOnClickListener {
            menuBottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
            showDeletePlaylistDialog()
        }

        viewModel.effect.observe(viewLifecycleOwner) { eff ->
            when (eff) {
                PlaylistDetailEffect.NavigateBackAfterPlaylistDeleted -> {
                    viewModel.consumeEffect()
                    findNavController().navigateUp()
                }
                is PlaylistDetailEffect.NavigateToPlayer -> {
                    viewModel.consumeEffect()
                    findNavController().navigate(
                        R.id.action_playlistDetail_to_audioPlayer,
                        bundleOf("arg_track" to eff.track)
                    )
                }
                null -> {}
            }
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            val playlist = state.playlist
            view.findViewById<TextView>(R.id.best_songs_).text = playlist?.name.orEmpty()

            val descView = view.findViewById<TextView>(R.id.playlist_description)
            val desc = playlist?.description?.trim().orEmpty()
            if (desc.isNotEmpty()) {
                descView.visibility = View.VISIBLE
                descView.text = desc
            } else {
                descView.visibility = View.GONE
            }

            val minutesView = view.findViewById<TextView>(R.id.playlist_meta_minutes)
            val tracksCountView = view.findViewById<TextView>(R.id.playlist_meta_tracks)
            if (playlist != null) {
                val minutes = state.totalMinutes
                val count = playlist.trackCount
                minutesView.text = RussianPlurals.pluralRussian(requireContext(), R.plurals.playlist_detail_minutes, minutes)
                tracksCountView.text = RussianPlurals.pluralRussian(requireContext(), R.plurals.playlist_detail_tracks, count)
            } else {
                minutesView.text = ""
                tracksCountView.text = ""
            }

            tracksAdapter.setItems(state.tracks)

            val emptyTracksView = view.findViewById<TextView>(R.id.playlist_empty_tracks)
            val hasPlaylist = playlist != null
            val emptyTracks = hasPlaylist && state.tracks.isEmpty()
            emptyTracksView.visibility = if (emptyTracks) View.VISIBLE else View.GONE

            val showTracksSheet = hasPlaylist && state.tracks.isNotEmpty()
            bottomSheet.visibility = if (showTracksSheet) View.VISIBLE else View.GONE

            val coverImage = view.findViewById<ImageView>(R.id.cover_image)
            val placeholder = view.findViewById<ImageView>(R.id.cover_placeholder)
            val path = playlist?.coverPath
            if (!path.isNullOrEmpty()) {
                placeholder.visibility = View.GONE
                coverImage.visibility = View.VISIBLE
                Glide.with(this)
                    .load(path)
                    .centerCrop()
                    .into(coverImage)
            } else {
                coverImage.visibility = View.GONE
                placeholder.visibility = View.VISIBLE
            }

            if (showTracksSheet) {
                val hasCover = !path.isNullOrEmpty()
                val needPeek =
                    !tracksSheetPeekApplied || tracksSheetPeekLastHasCover != hasCover
                if (needPeek) {
                    tracksSheetPeekApplied = true
                    tracksSheetPeekLastHasCover = hasCover
                    scheduleApplyBottomSheetPeek(view, hasCover = hasCover)
                }
            } else {
                tracksSheetPeekApplied = false
                tracksSheetPeekLastHasCover = null
            }

            bindMenuSheetContent(view)
        }
    }

    private fun bindMenuSheetContent(view: View) {
        val state = viewModel.state.value ?: return
        val playlist = state.playlist ?: return
        view.findViewById<TextView>(R.id.playlist_menu_title).text = playlist.name
        val count = playlist.trackCount
        val trStr = RussianPlurals.pluralRussian(requireContext(), R.plurals.playlist_detail_tracks, count)
        view.findViewById<TextView>(R.id.playlist_menu_subtitle).text = trStr

        val coverImage = view.findViewById<ImageView>(R.id.playlist_menu_cover_image)
        val placeholder = view.findViewById<ImageView>(R.id.playlist_menu_cover_placeholder)
        val path = playlist.coverPath
        if (!path.isNullOrEmpty()) {
            placeholder.visibility = View.GONE
            coverImage.visibility = View.VISIBLE
            Glide.with(this)
                .load(path)
                .centerCrop()
                .into(coverImage)
        } else {
            Glide.with(this).clear(coverImage)
            coverImage.visibility = View.GONE
            placeholder.visibility = View.VISIBLE
        }
    }

    private fun trySharePlaylist() {
        when (val result = viewModel.buildShareResult()) {
            PlaylistShareResult.NoTracks -> {
                Toast.makeText(requireContext(), R.string.playlist_share_no_tracks, Toast.LENGTH_SHORT).show()
            }
            is PlaylistShareResult.Text -> {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, result.body)
                }
                startActivity(Intent.createChooser(sendIntent, getString(R.string.playlist_share_chooser_title)))
            }
        }
    }

    private fun onPlaylistBackPressed() {
        if (closePlaylistMenuIfOpen()) return
        findNavController().navigateUp()
    }

    private fun closePlaylistMenuIfOpen(): Boolean {
        val menuBh = menuBottomSheetBehavior ?: return false
        if (menuBh.state != BottomSheetBehavior.STATE_HIDDEN) {
            menuBh.state = BottomSheetBehavior.STATE_HIDDEN
            return true
        }
        return false
    }

    private fun showDeletePlaylistDialog() {
        MaterialAlertDialogBuilder(
            requireContext(),
            R.style.ThemeOverlay_PlaylistDeleteTrackDialog
        )
            .setTitle(R.string.playlist_delete_playlist_title)
            .setMessage(R.string.playlist_delete_playlist_message)
            .setNegativeButton(R.string.playlist_delete_playlist_cancel) { d, _ -> d.dismiss() }
            .setPositiveButton(R.string.playlist_delete_playlist_confirm) { d, _ ->
                d.dismiss()
                viewModel.deletePlaylist()
            }
            .show()
    }

    private fun showDeleteTrackDialog(track: Track) {
        MaterialAlertDialogBuilder(
            requireContext(),
            R.style.ThemeOverlay_PlaylistDeleteTrackDialog
        )
            .setTitle(R.string.playlist_delete_track_title)
            .setNegativeButton(R.string.playlist_delete_track_no) { d, _ -> d.dismiss() }
            .setPositiveButton(R.string.playlist_delete_track_yes) { d, _ ->
                d.dismiss()
                viewModel.removeTrackFromPlaylist(track.trackId)
            }
            .show()
    }

    private fun applyPlaylistMenuBottomOffset(menuSheet: View, hidden: Boolean) {
        val offsetPx = resources.getDimensionPixelSize(R.dimen.playlist_menu_bottom_sheet_bottom_offset)
        menuSheet.translationY = if (hidden) 0f else offsetPx.toFloat()
    }

    private fun scheduleApplyBottomSheetPeek(root: View, hasCover: Boolean? = null) {
        val resolvedHasCover = hasCover ?: !viewModel.state.value?.playlist?.coverPath.isNullOrEmpty()
        root.post {
            if (!isAdded) return@post
            val behavior = bottomSheetBehavior ?: return@post
            if (root.height <= 0 && root.measuredHeight <= 0) {
                root.post {
                    if (!isAdded) return@post
                    bottomSheetBehavior?.let { applyBottomSheetPeek(it, root, resolvedHasCover) }
                }
                return@post
            }
            applyBottomSheetPeek(behavior, root, resolvedHasCover)
        }
    }

    private fun stableParentHeightForPeek(root: View): Int {
        val fromRootView = root.rootView.height
        if (fromRootView > 0) return fromRootView
        if (root.height > 0) return root.height
        if (root.measuredHeight > 0) return root.measuredHeight
        return resources.displayMetrics.heightPixels
    }

    private fun applyBottomSheetPeek(
        behavior: BottomSheetBehavior<out View>,
        root: View,
        hasCover: Boolean
    ) {
        val parentH = stableParentHeightForPeek(root)
        val dm = resources.displayMetrics
        val minPeek = (180 * dm.density).toInt()
        val extraRes = if (hasCover) {
            R.dimen.playlist_bottom_sheet_peek_extra_with_cover
        } else {
            R.dimen.playlist_bottom_sheet_peek_extra_placeholder
        }
        val extra = resources.getDimensionPixelSize(extraRes)
        behavior.setPeekHeight((parentH / 4).coerceAtLeast(minPeek) + extra)
    }

}
