package com.practicum.playlistmaker.di

import com.practicum.playlistmaker.domain.usecase.HistoryInteractor
import com.practicum.playlistmaker.domain.usecase.HistoryInteractorImpl
import com.practicum.playlistmaker.domain.usecase.SearchInteractor
import com.practicum.playlistmaker.domain.usecase.SearchInteractorImpl
import com.practicum.playlistmaker.domain.usecase.SettingsInteractor
import com.practicum.playlistmaker.domain.usecase.SettingsInteractorImpl
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
}