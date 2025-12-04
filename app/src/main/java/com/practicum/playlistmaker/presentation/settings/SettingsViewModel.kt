package com.practicum.playlistmaker.presentation.settings

import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.domain.usecase.SettingsInteractor

class SettingsViewModel(
    private val settingsInteractor: SettingsInteractor
) : ViewModel() {

    fun isDarkTheme(): Boolean = settingsInteractor.isDarkTheme()

    fun setDarkTheme(enabled: Boolean) {
        settingsInteractor.setDarkTheme(enabled)
    }
}