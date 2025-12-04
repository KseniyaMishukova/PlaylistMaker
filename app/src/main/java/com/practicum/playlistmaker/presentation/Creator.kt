package com.practicum.playlistmaker.presentation

import com.practicum.playlistmaker.presentation.main.MainViewModel
import com.practicum.playlistmaker.data.network.ITunesApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.content.Context
import android.content.SharedPreferences
import com.practicum.playlistmaker.presentation.audio.AudioPlayerViewModel
import com.practicum.playlistmaker.presentation.search.SearchViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.practicum.playlistmaker.presentation.settings.SettingsViewModel
import com.practicum.playlistmaker.data.repository.HistoryRepositoryImpl
import com.practicum.playlistmaker.data.repository.SearchRepositoryImpl
import com.practicum.playlistmaker.data.repository.SettingsRepositoryImpl
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
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://itunes.apple.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api: ITunesApi = retrofit.create(ITunesApi::class.java)

        return SearchRepositoryImpl(api)
    }

    private fun provideHistoryRepository(context: Context): HistoryRepository {
        val prefs: SharedPreferences =
            context.getSharedPreferences(HistoryRepositoryImpl.PREFS_NAME, Context.MODE_PRIVATE)
        return HistoryRepositoryImpl(prefs)
    }

    private fun provideSettingsRepository(context: Context): SettingsRepository {
        val prefs: SharedPreferences =
            context.getSharedPreferences(SettingsRepositoryImpl.PREFS_NAME, Context.MODE_PRIVATE)
        return SettingsRepositoryImpl(prefs)
    }
    fun provideSearchInteractor(): SearchInteractor {
        val repository: SearchRepository = provideSearchRepository()
        return SearchInteractorImpl(repository)
    }

    fun provideHistoryInteractor(context: Context): HistoryInteractor {
        return HistoryInteractorImpl(provideHistoryRepository(context))
    }

    fun provideSettingsInteractor(context: Context): SettingsInteractor {
        return SettingsInteractorImpl(provideSettingsRepository(context))
    }
    fun provideSettingsViewModelFactory(context: Context): ViewModelProvider.Factory {
        val interactor = provideSettingsInteractor(context)
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                    return SettingsViewModel(interactor) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
    fun provideAudioPlayerViewModelFactory(): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AudioPlayerViewModel::class.java)) {
                    return AudioPlayerViewModel() as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
    fun provideSearchViewModelFactory(context: Context): ViewModelProvider.Factory {
        val searchInteractor = provideSearchInteractor()
        val historyInteractor = provideHistoryInteractor(context)
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
                    return SearchViewModel(searchInteractor, historyInteractor) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
    fun provideMainViewModelFactory(context: Context): ViewModelProvider.Factory {
        val interactor = provideSettingsInteractor(context)
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                    return MainViewModel(interactor) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}