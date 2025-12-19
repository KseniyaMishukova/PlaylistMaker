package com.practicum.playlistmaker.di

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.practicum.playlistmaker.data.network.ITunesApi
import com.practicum.playlistmaker.data.repository.HistoryRepositoryImpl
import com.practicum.playlistmaker.data.repository.SearchRepositoryImpl
import com.practicum.playlistmaker.data.repository.SettingsRepositoryImpl
import com.practicum.playlistmaker.domain.repository.HistoryRepository
import com.practicum.playlistmaker.domain.repository.SearchRepository
import com.practicum.playlistmaker.domain.repository.SettingsRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val historyPrefsQualifier = named("history_prefs")
val settingsPrefsQualifier = named("settings_prefs")

val dataModule = module {

    single<Gson> {
        Gson()
    }

    single<Retrofit> {
        Retrofit.Builder()
            .baseUrl("https://itunes.apple.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single<ITunesApi> {
        get<Retrofit>().create(ITunesApi::class.java)
    }

    single<SharedPreferences>(qualifier = historyPrefsQualifier) {
        androidContext().getSharedPreferences(
            HistoryRepositoryImpl.PREFS_NAME,
            Context.MODE_PRIVATE
        )
    }

    single<SharedPreferences>(qualifier = settingsPrefsQualifier) {
        androidContext().getSharedPreferences(
            SettingsRepositoryImpl.PREFS_NAME,
            Context.MODE_PRIVATE
        )
    }

    single<SearchRepository> {
        SearchRepositoryImpl(get<ITunesApi>())
    }

    single<HistoryRepository> {
        HistoryRepositoryImpl(
            get(qualifier = historyPrefsQualifier),
            get<Gson>()
        )
    }

    single<SettingsRepository> {
        SettingsRepositoryImpl(get(qualifier = settingsPrefsQualifier))
    }
}