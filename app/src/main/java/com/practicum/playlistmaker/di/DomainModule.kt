package com.practicum.playlistmaker.di

import com.practicum.playlistmaker.data.repository.FavoritesRepositoryImpl
import com.practicum.playlistmaker.data.repository.SearchRepositoryImpl
import com.practicum.playlistmaker.data.repository.HistoryRepositoryImpl
import com.practicum.playlistmaker.domain.repository.PlaylistRepositoryImpl
import com.practicum.playlistmaker.data.repository.SettingsRepositoryImpl
import com.practicum.playlistmaker.domain.repository.FavoritesRepository
import com.practicum.playlistmaker.domain.repository.SearchRepository
import com.practicum.playlistmaker.domain.repository.HistoryRepository
import com.practicum.playlistmaker.domain.repository.PlaylistRepository
import com.practicum.playlistmaker.domain.repository.SettingsRepository
import com.practicum.playlistmaker.domain.usecase.CreatePlaylistInteractor
import com.practicum.playlistmaker.domain.usecase.CreatePlaylistInteractorImpl
import com.practicum.playlistmaker.domain.usecase.FavoritesInteractor
import com.practicum.playlistmaker.domain.usecase.FavoritesInteractorImpl
import com.practicum.playlistmaker.domain.usecase.HistoryInteractor
import com.practicum.playlistmaker.domain.usecase.HistoryInteractorImpl
import com.practicum.playlistmaker.domain.usecase.SearchInteractor
import com.practicum.playlistmaker.domain.usecase.SearchInteractorImpl
import com.practicum.playlistmaker.domain.usecase.SettingsInteractor
import com.practicum.playlistmaker.domain.usecase.SettingsInteractorImpl
import org.koin.core.qualifier.named
import org.koin.dsl.module

val domainModule = module {

    single<SearchInteractor> {
        SearchInteractorImpl(get())
    }

    single<HistoryInteractor> {
        HistoryInteractorImpl(get())
    }

    single<SettingsInteractor> {
        SettingsInteractorImpl(get())
    }
    
    single<FavoritesInteractor> {
        FavoritesInteractorImpl(get())
    }
    
    single<FavoritesRepository> {
        FavoritesRepositoryImpl(get())
    }
    
    single<SearchRepository> {
        SearchRepositoryImpl(get(), get())
    }
    
    single<HistoryRepository> {
        HistoryRepositoryImpl(get(named("history_prefs")), get())
    }
    
    single<SettingsRepository> {
        SettingsRepositoryImpl(get(named("settings_prefs")))
    }

    single<PlaylistRepository> {
        PlaylistRepositoryImpl(get())
    }

    single<CreatePlaylistInteractor> {
        CreatePlaylistInteractorImpl(get(), get())
    }
}