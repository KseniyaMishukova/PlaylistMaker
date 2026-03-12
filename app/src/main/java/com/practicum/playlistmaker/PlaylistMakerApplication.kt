package com.practicum.playlistmaker

import android.app.Application
import android.content.Context
import com.practicum.playlistmaker.di.dataModule
import com.practicum.playlistmaker.di.domainModule
import com.practicum.playlistmaker.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class PlaylistMakerApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@PlaylistMakerApplication)
            modules(dataModule, domainModule, viewModelModule)
        }
    }
}