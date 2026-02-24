package com.practicum.playlistmaker.presentation.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.models.Track
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.practicum.playlistmaker.presentation.search.TrackAdapter

class FavoritesFragment : Fragment() {

    private lateinit var adapter: TrackAdapter

    private val viewModel: FavoritesViewModel by viewModel()

    companion object {
        fun newInstance(): FavoritesFragment {
            return FavoritesFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler: RecyclerView = view.findViewById(R.id.rvFavorites)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = TrackAdapter(mutableListOf()) { track ->
            findNavController().navigate(
                R.id.audioPlayerFragment,
                Bundle().apply {
                    putSerializable("arg_track", track)
                }
            )
        }
        recycler.adapter = adapter

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is FavoritesState.Empty -> {
                    recycler.visibility = View.GONE
                    view.findViewById<TextView>(R.id.tvFavoritesPlaceholder).visibility =
                        View.VISIBLE
                    view.findViewById<View>(R.id.ivFavoritesPlaceholder).visibility =
                        View.VISIBLE
                }

                is FavoritesState.Content -> {
                    recycler.visibility = View.VISIBLE
                    view.findViewById<TextView>(R.id.tvFavoritesPlaceholder).visibility =
                        View.GONE
                    view.findViewById<View>(R.id.ivFavoritesPlaceholder).visibility = View.GONE
                    adapter.setItems(state.tracks)
                }
            }
        }
    }
    }