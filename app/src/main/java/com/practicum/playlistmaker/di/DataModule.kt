package com.practicum.playlistmaker.di

import android.content.Context
import androidx.room.Room
import com.practicum.playlistmaker.data.db.FavoritesDatabase
import com.google.gson.Gson
import com.practicum.playlistmaker.data.network.ITunesApi
import com.practicum.playlistmaker.data.repository.HistoryRepositoryImpl
import com.practicum.playlistmaker.data.repository.SettingsRepositoryImpl
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val dataModule = module {
    single {
        Room.databaseBuilder(
            get<Context>(),
            FavoritesDatabase::class.java,
            "favorites_database"
        ).fallbackToDestructiveMigration().build()
    }



    single { Gson() }

    single { get<FavoritesDatabase>().favoriteTracksDao() }

    single(named("history_prefs")) { get<Context>().getSharedPreferences(HistoryRepositoryImpl.PREFS_NAME, Context.MODE_PRIVATE) }

    single(named("settings_prefs")) { get<Context>().getSharedPreferences(SettingsRepositoryImpl.PREFS_NAME, Context.MODE_PRIVATE) }

    single<Retrofit> {
        Retrofit.Builder()
            .baseUrl("https://itunes.apple.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single<ITunesApi> {
        get<Retrofit>().create(ITunesApi::class.java)
    }
}