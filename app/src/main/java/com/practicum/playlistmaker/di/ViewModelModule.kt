package com.practicum.playlistmaker.di

import com.practicum.playlistmaker.presentation.audio.AudioPlayerViewModel
import com.practicum.playlistmaker.presentation.media.FavoritesViewModel
import com.practicum.playlistmaker.presentation.media.PlaylistsViewModel
import com.practicum.playlistmaker.presentation.main.MainViewModel
import com.practicum.playlistmaker.presentation.search.SearchViewModel
import com.practicum.playlistmaker.presentation.settings.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MainViewModel(get()) }
    viewModel { SearchViewModel(get(), get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { AudioPlayerViewModel() }
    viewModel { FavoritesViewModel() }
    viewModel { PlaylistsViewModel() }
}