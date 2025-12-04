package com.practicum.playlistmaker.domain.usecase

interface SettingsInteractor {
    fun isDarkTheme(): Boolean
    fun setDarkTheme(enabled: Boolean)
}