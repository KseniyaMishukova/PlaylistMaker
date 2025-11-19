package com.practicum.playlistmaker.domain

interface SettingsInteractor {
    fun isDarkTheme(): Boolean
    fun setDarkTheme(enabled: Boolean)
}