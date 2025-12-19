package com.practicum.playlistmaker.presentation.main

import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.domain.usecase.SettingsInteractor

class MainViewModel(
    private val settingsInteractor: SettingsInteractor
) : ViewModel() {

    fun isDarkTheme(): Boolean = settingsInteractor.isDarkTheme()
}