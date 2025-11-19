package com.practicum.playlistmaker.domain

class SettingsInteractorImpl(
    private val settingsRepository: SettingsRepository
) : SettingsInteractor {

    override fun isDarkTheme(): Boolean = settingsRepository.isDarkTheme()

    override fun setDarkTheme(enabled: Boolean) {
        settingsRepository.setDarkTheme(enabled)
    }
}