package com.practicum.playlistmaker.di

import com.practicum.playlistmaker.presentation.audio.AudioPlayerViewModel
import com.practicum.playlistmaker.presentation.media.FavoritesViewModel
import com.practicum.playlistmaker.presentation.create_playlist.CreatePlaylistViewModel
import com.practicum.playlistmaker.presentation.create_playlist.EditPlaylistViewModel
import com.practicum.playlistmaker.presentation.media.PlaylistsViewModel
import com.practicum.playlistmaker.presentation.playlist_detail.PlaylistDetailViewModel
import com.practicum.playlistmaker.presentation.main.MainViewModel
import com.practicum.playlistmaker.presentation.search.SearchViewModel
import com.practicum.playlistmaker.presentation.settings.SettingsViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { (playlistId: Long) ->
        PlaylistDetailViewModel(androidApplication(), playlistId, get())
    }
    viewModel { MainViewModel(get()) }
    viewModel { SearchViewModel(get(), get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { (track: com.practicum.playlistmaker.domain.models.Track) ->
        AudioPlayerViewModel(track, get(), get())
    }
    viewModel { FavoritesViewModel(get()) }
    viewModel { PlaylistsViewModel(get()) }
    viewModel { CreatePlaylistViewModel(get()) }
    viewModel { (playlistId: Long) ->
        EditPlaylistViewModel(playlistId, get())
    }
}