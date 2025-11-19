package com.practicum.playlistmaker.presentation

import android.content.Context
import com.practicum.playlistmaker.data.HistoryRepositoryImpl
import com.practicum.playlistmaker.data.SearchRepositoryImpl
import com.practicum.playlistmaker.data.SettingsRepositoryImpl
import com.practicum.playlistmaker.data.RetrofitProvider
import com.practicum.playlistmaker.domain.HistoryInteractor
import com.practicum.playlistmaker.domain.HistoryInteractorImpl
import com.practicum.playlistmaker.domain.SearchInteractor
import com.practicum.playlistmaker.domain.SearchInteractorImpl
import com.practicum.playlistmaker.domain.SettingsInteractor
import com.practicum.playlistmaker.domain.SettingsInteractorImpl

object Creator {

    private fun provideSearchRepository(): com.practicum.playlistmaker.domain.SearchRepository {
        return SearchRepositoryImpl(RetrofitProvider.api)
    }

    private fun provideHistoryRepository(context: Context): com.practicum.playlistmaker.domain.HistoryRepository {
        return HistoryRepositoryImpl(context)
    }

    private fun provideSettingsRepository(context: Context): com.practicum.playlistmaker.domain.SettingsRepository {
        return SettingsRepositoryImpl(context)
    }

    fun provideSearchInteractor(): SearchInteractor {
        return SearchInteractorImpl(provideSearchRepository())
    }

    fun provideHistoryInteractor(context: Context): HistoryInteractor {
        return HistoryInteractorImpl(provideHistoryRepository(context))
    }

    fun provideSettingsInteractor(context: Context): SettingsInteractor {
        return SettingsInteractorImpl(provideSettingsRepository(context))
    }
}