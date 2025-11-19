package com.practicum.playlistmaker.presentation

import android.content.Context
import com.practicum.playlistmaker.data.repository.HistoryRepositoryImpl
import com.practicum.playlistmaker.data.repository.SearchRepositoryImpl
import com.practicum.playlistmaker.data.repository.SettingsRepositoryImpl
import com.practicum.playlistmaker.data.network.RetrofitProvider
import com.practicum.playlistmaker.domain.usecase.HistoryInteractor
import com.practicum.playlistmaker.domain.usecase.HistoryInteractorImpl
import com.practicum.playlistmaker.domain.usecase.SearchInteractor
import com.practicum.playlistmaker.domain.usecase.SearchInteractorImpl
import com.practicum.playlistmaker.domain.usecase.SettingsInteractor
import com.practicum.playlistmaker.domain.usecase.SettingsInteractorImpl
import com.practicum.playlistmaker.domain.repository.HistoryRepository
import com.practicum.playlistmaker.domain.repository.SearchRepository
import com.practicum.playlistmaker.domain.repository.SettingsRepository

object Creator {

    private fun provideSearchRepository(): SearchRepository {
        return SearchRepositoryImpl(RetrofitProvider.api)
    }

    private fun provideHistoryRepository(context: Context): HistoryRepository {
        return HistoryRepositoryImpl(context)
    }

    private fun provideSettingsRepository(context: Context): SettingsRepository {
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