package com.practicum.playlistmaker.presentation.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.presentation.playlist_detail.PlaylistDetailFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : Fragment() {

    private val viewModel: PlaylistsViewModel by viewModel()
    private lateinit var adapter: PlaylistAdapter

    companion object {
        fun newInstance(): PlaylistsFragment {
            return PlaylistsFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_playlists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.button).setOnClickListener {
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                .navigate(R.id.action_mediaLibrary_to_createPlaylist)
        }

        adapter = PlaylistAdapter { playlist ->
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(
                R.id.action_mediaLibrary_to_playlistDetail,
                bundleOf(PlaylistDetailFragment.ARG_PLAYLIST_ID to playlist.id)
            )
        }
        val recycler = view.findViewById<RecyclerView>(R.id.playlists_recycler)
        recycler.layoutManager = GridLayoutManager(requireContext(), 2)
        recycler.adapter = adapter

        viewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            adapter.setItems(playlists)
            val hasPlaylists = playlists.isNotEmpty()
            view.findViewById<View>(R.id.playlists_recycler).visibility =
                if (hasPlaylists) View.VISIBLE else View.GONE
            view.findViewById<View>(R.id.empty_state).visibility =
                if (hasPlaylists) View.GONE else View.VISIBLE
        }
    }
}
