package com.practicum.playlistmaker.presentation.create_playlist

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.practicum.playlistmaker.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class EditPlaylistFragment : CreatePlaylistFragment() {

    override val editorViewModel: EditPlaylistViewModel by viewModel {
        parametersOf(requireArguments().getLong(ARG_PLAYLIST_ID))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.header_title).setText(R.string.edit_playlist_title)
        view.findViewById<Button>(R.id.create_button).setText(R.string.save_playlist_button)
    }

    companion object {
        const val ARG_PLAYLIST_ID = "playlist_id"
    }
}
